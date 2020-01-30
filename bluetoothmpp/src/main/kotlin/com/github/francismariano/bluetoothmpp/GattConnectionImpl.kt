package com.github.francismariano.bluetoothmpp

import android.bluetooth.BluetoothProfile
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.RequiresApi
import com.github.francismariano.bluetoothmpp.GattConnection.Companion.clientCharacteristicConfiguration
import com.github.francismariano.bluetoothmpp.extensions.offerCatching
import com.github.francismariano.bluetoothmpp.extensions.withCloseHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import splitties.bitflags.hasFlag
import splitties.init.appCtx
import splitties.lifecycle.coroutines.MainAndroid
import splitties.lifecycle.coroutines.MainDispatcherPerformanceIssueWorkaround
import kotlin.coroutines.CoroutineContext

@RequiresApi(18)
actual val STATUS_SUCCESS = CMBluetoothGatt.GATT_SUCCESS

@RequiresApi(18)
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@ExperimentalBleGattCoroutinesCoroutinesApi
actual class GattConnectionImpl actual constructor(
    override val bluetoothDevice: CMBluetoothDevice
) : GattConnection, CoroutineScope {
    private val job = Job()
    @UseExperimental(MainDispatcherPerformanceIssueWorkaround::class)
    override val coroutineContext: CoroutineContext = Dispatchers.MainAndroid + job

    private var connectionSettings: ConnectionSettings

    init {
        require(bluetoothDevice.type != CMBluetoothDevice.DEVICE_TYPE_CLASSIC) {
            "Can't connect GATT to Bluetooth Classic device!"
        }

        connectionSettings = ConnectionSettings(
            autoConnect = false,
            allowAutoConnect = false,
            disconnectOnClose = true,
            transport = 0,
            phy = 1
        )

    }

    actual val rssiChannel = Channel<GattResponse<Int>>()
    actual val servicesDiscoveryChannel = Channel<GattResponse<List<CMBluetoothGattService>>>()
    actual val readChannel = Channel<GattResponse<BGC>>()
    actual val writeChannel = Channel<GattResponse<BGC>>()
    actual val reliableWriteChannel = Channel<GattResponse<Unit>>()
    actual val characteristicChangedChannel = BroadcastChannel<BGC>(1)
    actual val readDescChannel = Channel<GattResponse<BGD>>()
    actual val writeDescChannel = Channel<GattResponse<BGD>>()
    actual val mtuChannel = Channel<GattResponse<Int>>()
    actual val phyReadChannel = Channel<GattResponse<Phy>>()

    actual val isConnectedBroadcastChannel = ConflatedBroadcastChannel(false)
    actual val isConnectedChannel = isConnectedBroadcastChannel.openSubscription()
    override var isConnected: Boolean
        get() = runCatching { !isClosed && isConnectedBroadcastChannel.value }.getOrDefault(false)
        private set(value) = isConnectedBroadcastChannel.offerCatching(value).let { Unit }
    actual var isClosed = false
    actual var closedException: ConnectionClosedException? = null
    actual val stateChangeBroadcastChannel = ConflatedBroadcastChannel<StateChange>()

    override val stateChangeChannel get() = stateChangeBroadcastChannel.openSubscription()

    override val notifyChannel: ReceiveChannel<BGC>
        get() = characteristicChangedChannel.openSubscription()

    actual var bluetoothGatt: BG? = null
    actual fun requireGatt(): BG = bluetoothGatt ?: error("Call connect() first!")

    override suspend fun connect() {
        checkNotClosed()
        val gatt = bluetoothGatt
        if (gatt == null) {
            val device = bluetoothDevice
            bluetoothGatt = with(connectionSettings) {
                when {
                    SDK_INT >= 26 -> device.connectGatt(
                        appCtx,
                        autoConnect,
                        callback,
                        transport,
                        phy
                    )
                    SDK_INT >= 23 -> device.connectGatt(appCtx, autoConnect, callback, transport)
                    else -> device.connectGatt(appCtx, autoConnect, callback)
                }
            } ?: error("No BluetoothGatt instance returned. Is Bluetooth supported and enabled?")
        } else {
            require(connectionSettings.allowAutoConnect) {
                "Connecting more than once would implicitly enable auto connect, which is not" +
                        "allowed with current connection settings."
            }
            gatt.connect().checkOperationInitiationSucceeded()
        }
        isConnectedChannel.first { connected -> connected }
    }

    override suspend fun disconnect() {
        require(connectionSettings.allowAutoConnect) {
            "Disconnect is not supported when auto connect is not allowed. Use close() instead."
        }
        checkNotClosed()
        requireGatt().disconnect()
        isConnectedChannel.first { connected -> !connected }
    }

    override fun close(notifyStateChangeChannel: Boolean) {
        closeInternal(
            notifyStateChangeChannel,
            ConnectionClosedException()
        )
    }

    actual fun closeInternal(notifyStateChangeChannel: Boolean, cause: ConnectionClosedException) {
        closedException = cause
        if (connectionSettings.disconnectOnClose) bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        isClosed = true
        isConnected = false
        if (notifyStateChangeChannel) stateChangeBroadcastChannel.offerCatching(
            element = StateChange(
                status = STATUS_SUCCESS,
                newState = BluetoothProfile.STATE_DISCONNECTED
            )
        )
        isConnectedBroadcastChannel.close(cause)
        rssiChannel.close(cause)
        servicesDiscoveryChannel.close(cause)
        readChannel.close(cause)
        writeChannel.close(cause)
        reliableWriteChannel.close(cause)
        characteristicChangedChannel.close(cause)
        readDescChannel.close(cause)
        writeDescChannel.close(cause)
        stateChangeBroadcastChannel.close(cause)
        job.cancel()
    }

    override suspend fun readRemoteRssi() = gattRequest(rssiChannel) {
        readRemoteRssi()
    }

    @RequiresApi(21)
    override fun requestPriority(priority: Int) {
        requireGatt().requestConnectionPriority(priority).checkOperationInitiationSucceeded()
    }

    override suspend fun discoverServices(): List<CMBluetoothGattService> =
        gattRequest(servicesDiscoveryChannel) {
            discoverServices()
        }

    override fun setCharacteristicNotificationsEnabled(characteristic: BGC, enable: Boolean) {
        requireGatt().setCharacteristicNotification(characteristic, enable)
            .checkOperationInitiationSucceeded()
    }

    override suspend fun setCharacteristicNotificationsEnabledOnRemoteDevice(
        characteristic: BGC,
        enable: Boolean
    ) {
        require(characteristic.properties.hasFlag(BGC.PROPERTY_NOTIFY)) {
            "This characteristic doesn't support notification or doesn't come from discoverServices()."
        }
        val descriptor: BGD? = characteristic.getDescriptor(clientCharacteristicConfiguration)
        requireNotNull(descriptor) {
            "This characteristic misses the client characteristic configuration descriptor."
        }
        descriptor.value = if (enable) {
            BGD.ENABLE_NOTIFICATION_VALUE
        } else BGD.DISABLE_NOTIFICATION_VALUE
        writeDescriptor(descriptor)
    }

    override fun openNotificationSubscription(
        characteristic: BGC,
        disableNotificationsOnChannelClose: Boolean
    ): ReceiveChannel<BGC> {
        require(characteristic.properties.hasFlag(BGC.PROPERTY_NOTIFY)) {
            "This characteristic doesn't support notification or doesn't come from discoverServices()."
        }
        setCharacteristicNotificationsEnabled(characteristic, enable = true)
        val notificationChannel = characteristicChangedChannel.openSubscription().filter {
            it.uuid == characteristic.uuid
        }
        return if (disableNotificationsOnChannelClose) notificationChannel.withCloseHandler {
            setCharacteristicNotificationsEnabled(characteristic, enable = false)
        } else notificationChannel
    }

    override fun getService(uuid: CMUUID): CMBluetoothGattService? = requireGatt().getService(uuid)

    override suspend fun readCharacteristic(characteristic: BGC) = gattRequest(readChannel) {
        readCharacteristic(characteristic)
    }

    override suspend fun writeCharacteristic(characteristic: BGC) = gattRequest(writeChannel) {
        writeCharacteristic(characteristic)
    }

    @RequiresApi(19)
    override suspend fun reliableWrite(writeOperations: suspend GattConnection.() -> Unit) =
        gattRequest(reliableWriteChannel) {
            try {
                reliableWriteOngoing = true
                requireGatt().beginReliableWrite().checkOperationInitiationSucceeded()
                writeOperations()
                requireGatt().executeReliableWrite()
            } catch (e: Throwable) {
                requireGatt().abortReliableWrite()
                throw e
            } finally {
                reliableWriteOngoing = false
            }
        }

    override suspend fun readDescriptor(desc: BGD) = gattRequest(readDescChannel) {
        readDescriptor(desc)
    }

    override suspend fun writeDescriptor(desc: BGD) = gattRequest(writeDescChannel) {
        writeDescriptor(desc)
    }

    @RequiresApi(26)
    override suspend fun readPhy() = gattRequest(phyReadChannel) {
        readPhy().let { true }
    }

    @RequiresApi(21)
    override suspend fun requestMtu(mtu: Int) = gattRequest(mtuChannel) {
        requestMtu(mtu)
    }

    actual val callback = object : CMBluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BG, status: Int, newState: Int) {
            when (status) {
                STATUS_SUCCESS -> isConnected = newState == BluetoothProfile.STATE_CONNECTED
            }
            stateChangeBroadcastChannel.offerCatching(
                StateChange(
                    status = status,
                    newState = newState
                )
            )
        }

        override fun onReadRemoteRssi(gatt: BG, rssi: Int, status: Int) {
            rssiChannel.launchAndSendResponse(rssi, status)
        }

        override fun onServicesDiscovered(gatt: BG, status: Int) {
            servicesDiscoveryChannel.launchAndSendResponse(gatt.services, status)
        }

        override fun onCharacteristicRead(gatt: BG, characteristic: BGC, status: Int) {
            readChannel.launchAndSendResponse(characteristic, status)
        }

        override fun onCharacteristicWrite(gatt: BG, characteristic: BGC, status: Int) {
            writeChannel.launchAndSendResponse(characteristic, status)
        }

        override fun onReliableWriteCompleted(gatt: BG, status: Int) {
            reliableWriteChannel.launchAndSendResponse(Unit, status)
        }

        override fun onCharacteristicChanged(gatt: BG, characteristic: BGC) {
            launch { characteristicChangedChannel.send(characteristic) }
        }

        override fun onDescriptorRead(gatt: BG, descriptor: BGD, status: Int) {
            readDescChannel.launchAndSendResponse(descriptor, status)
        }

        override fun onDescriptorWrite(gatt: BG, descriptor: BGD, status: Int) {
            writeDescChannel.launchAndSendResponse(descriptor, status)
        }

        override fun onMtuChanged(gatt: BG, mtu: Int, status: Int) {
            mtuChannel.launchAndSendResponse(mtu, status)
        }

        override fun onPhyRead(gatt: BG, txPhy: Int, rxPhy: Int, status: Int) {
            phyReadChannel.launchAndSendResponse(
                Phy(
                    tx = txPhy,
                    rx = rxPhy
                ), status
            )
        }
    }

    actual fun Boolean.checkOperationInitiationSucceeded() {
        if (!this) throw OperationInitiationFailedException()
    }

    actual val bleOperationMutex = Mutex()
    actual val reliableWritesMutex = Mutex()
    actual var reliableWriteOngoing = false

    actual suspend inline fun <E> gattRequest(
        ch: ReceiveChannel<GattResponse<E>>,
        operation: CMBluetoothGatt.() -> Boolean
    ): E {
        checkNotClosed()
        val mutex = when {
            writeChannel === ch && reliableWriteOngoing -> reliableWritesMutex
            else -> bleOperationMutex
        }
        return mutex.withLock {
            checkNotClosed()
            requireGatt().operation().checkOperationInitiationSucceeded()
            val response = ch.receive()
            if (response.isSuccess) response.e else throw OperationFailedException(
                response.status
            )
        }
    }

    /**
     * This code is currently not fault tolerant. The channel is irrevocably closed if the GATT
     * status is not success.
     */
    actual fun <E> SendChannel<GattResponse<E>>.launchAndSendResponse(e: E, status: Int) {
        launch {
            send(
                GattResponse(
                    e,
                    status
                )
            )
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun checkNotClosed() {
        if (isClosed) throw ConnectionClosedException(
            closedException
        )
    }

    actual class GattResponse<out E> actual constructor(val e: E, val status: Int) {
        inline val isSuccess get() = status == STATUS_SUCCESS
    }

    init {
        val closeOnDisconnect = connectionSettings.allowAutoConnect.not()
        if (closeOnDisconnect) launch {
            stateChangeChannel.consumeEach { stateChange ->
                if (stateChange.newState == BluetoothProfile.STATE_DISCONNECTED) {
                    val cause =
                        ConnectionClosedException(
                            messageSuffix = " because of disconnection"
                        )
                    closeInternal(notifyStateChangeChannel = false, cause = cause)
                }
            }
        }
    }
}

actual typealias CMBluetoothGattCallback = android.bluetooth.BluetoothGattCallback
actual typealias CMBluetoothGatt = android.bluetooth.BluetoothGatt