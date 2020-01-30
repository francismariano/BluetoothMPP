package com.github.francismariano.bluetoothMPP

import com.github.francismariano.bluetoothMPP.extensions.offerCatching
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject
import kotlin.coroutines.CoroutineContext

actual val STATUS_SUCCESS: Int = 0 // CBATTError.CBATTErrorSuccess

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@ExperimentalBleGattCoroutinesCoroutinesApi
actual class GattConnectionImpl actual constructor(
    override val bluetoothDevice: CMBluetoothDevice
) : GattConnection, CoroutineScope {

    private val dispatcher = MainDispatcher()
    private val job = Job()

    private var connectionSettings: ConnectionSettings = ConnectionSettings(
        autoConnect = false,
        allowAutoConnect = false,
        disconnectOnClose = true,
        transport = 0,
        phy = 1
    )

    override val coroutineContext: CoroutineContext = dispatcher + job

//    init {
//        // TODO: Implementation of require
////        require(bluetoothDevice.type != CMBluetoothDevice.DEVICE_TYPE_CLASSIC) {
////            "Can't connect GATT to Bluetooth Classic device!"
////        }
//
//    }

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
        // TODO: Implement connectionSettings
        // TODO: auto connect
        // TODO: if gatt == null what to do?????
        checkNotClosed()
        val gatt = bluetoothGatt
        gatt?.let {
            bluetoothDevice.connectPeripheral(gatt, null)
            isConnectedChannel.first { connected -> connected }
        }
    }

    override suspend fun disconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        require(connectionSettings.allowAutoConnect) {
//            "Disconnect is not supported when auto connect is not allowed. Use close() instead."
//        }
//        checkNotClosed()
//        requireGatt().disconnect()
//        isConnectedChannel.first { connected -> !connected }
    }

    override fun close(notifyStateChangeChannel: Boolean) {
        closeInternal(
            notifyStateChangeChannel,
            ConnectionClosedException()
        )
    }

    actual fun closeInternal(
        notifyStateChangeChannel: Boolean,
        cause: ConnectionClosedException
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        closedException = cause
//        if (connectionSettings.disconnectOnClose) bluetoothGatt?.disconnect()
//        bluetoothGatt?.close()
//        isClosed = true
//        isConnected = false
//        if (notifyStateChangeChannel) stateChangeBroadcastChannel.offerCatching(
//            element = StateChange(
//                status = STATUS_SUCCESS,
//                newState = BluetoothProfile.STATE_DISCONNECTED
//            )
//        )
//        isConnectedBroadcastChannel.close(cause)
//        rssiChannel.close(cause)
//        servicesDiscoveryChannel.close(cause)
//        readChannel.close(cause)
//        writeChannel.close(cause)
//        reliableWriteChannel.close(cause)
//        characteristicChangedChannel.close(cause)
//        readDescChannel.close(cause)
//        writeDescChannel.close(cause)
//        stateChangeBroadcastChannel.close(cause)
//        job.cancel()
    }

    override suspend fun readRemoteRssi() =
        gattRequest(rssiChannel) {
            extReadRSSI()
        }

    override fun requestPriority(priority: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        requireGatt().requestConnectionPriority(priority).checkOperationInitiationSucceeded()
    }

    override suspend fun discoverServices(): List<CMBluetoothGattService> =
        gattRequest(servicesDiscoveryChannel) {
            discoverServices()
        }

    override fun setCharacteristicNotificationsEnabled(characteristic: BGC, enable: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        requireGatt().setCharacteristicNotification(characteristic, enable)
//            .checkOperationInitiationSucceeded()
    }

    override suspend fun setCharacteristicNotificationsEnabledOnRemoteDevice(
        characteristic: BGC,
        enable: Boolean
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        require(characteristic.properties.hasFlag(BGC.PROPERTY_NOTIFY)) {
//            "This characteristic doesn't support notification or doesn't come from discoverServices()."
//        }
//        val descriptor: BGD? =
//            characteristic.getDescriptor(GattConnection.clientCharacteristicConfiguration)
//        requireNotNull(descriptor) {
//            "This characteristic misses the client characteristic configuration descriptor."
//        }
//        descriptor.value = if (enable) {
//            BGD.ENABLE_NOTIFICATION_VALUE
//        } else BGD.DISABLE_NOTIFICATION_VALUE
//        writeDescriptor(descriptor)
    }

    override fun openNotificationSubscription(
        characteristic: BGC,
        disableNotificationsOnChannelClose: Boolean
    ): ReceiveChannel<BGC> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        require(characteristic.properties.hasFlag(BGC.PROPERTY_NOTIFY)) {
//            "This characteristic doesn't support notification or doesn't come from discoverServices()."
//        }
//        setCharacteristicNotificationsEnabled(characteristic, enable = true)
//        val notificationChannel = characteristicChangedChannel.openSubscription().filter {
//            it.uuid == characteristic.uuid
//        }
//        return if (disableNotificationsOnChannelClose) notificationChannel.withCloseHandler {
//            setCharacteristicNotificationsEnabled(characteristic, enable = false)
//        } else notificationChannel
    }

    override fun getService(uuid: CMUUID): CMBluetoothGattService? =
        requireGatt().services?.first { it == uuid } as CMBluetoothGattService

    override suspend fun readCharacteristic(characteristic: BGC) = gattRequest(readChannel) {
        readCharacteristic(characteristic)
    }

    override suspend fun writeCharacteristic(characteristic: BGC) = gattRequest(writeChannel) {
        writeCharacteristic(characteristic)
    }

    override suspend fun reliableWrite(writeOperations: suspend GattConnection.() -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.}
//            =
//        gattRequest(reliableWriteChannel) {
//            try {
//                reliableWriteOngoing = true
//                requireGatt().beginReliableWrite().checkOperationInitiationSucceeded()
//                writeOperations()
//                requireGatt().executeReliableWrite()
//            } catch (e: Throwable) {
//                requireGatt().abortReliableWrite()
//                throw e
//            } finally {
//                reliableWriteOngoing = false
//            }
//        }
    }

    override suspend fun readDescriptor(desc: BGD) = gattRequest(readDescChannel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        readDescriptor(desc)
    }

    override suspend fun writeDescriptor(desc: BGD) = gattRequest(writeDescChannel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        writeDescriptor(desc)
    }

    override suspend fun readPhy() = gattRequest(phyReadChannel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        readPhy().let { true }
    }

    override suspend fun requestMtu(mtu: Int) = gattRequest(mtuChannel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        requestMtu(mtu)
    }

    actual val callback: CMBluetoothGattCallback
        get() = TODO("see callback below") //To change initializer of created properties use File | Settings | File Templates.

    // Because the callback above had problem with his typealias has been created the following peripheralDelegate
    @Suppress("CONFLICTING_OVERLOADS")
    private val peripheralDelegate: CBPeripheralDelegateProtocol = object : NSObject(),
        CBPeripheralDelegateProtocol {

        override fun peripheral(peripheral: CMBluetoothGatt, didDiscoverServices: NSError?) {
            servicesDiscoveryChannel.launchAndSendResponse(
                peripheral.services as List<CMBluetoothGattService>,
                didDiscoverServices.hashCode()
            )
        }

        override fun peripheral(
            peripheral: CMBluetoothGatt,
            didReadRSSI: NSNumber,
            error: NSError?
        ) {
            rssiChannel.launchAndSendResponse(didReadRSSI.intValue, error.hashCode())
        }

        override fun peripheral(
            peripheral: CMBluetoothGatt,
            didUpdateValueForCharacteristic: BGC,
            error: NSError?
        ) {
            readChannel.launchAndSendResponse(didUpdateValueForCharacteristic, error.hashCode())
        }

        override fun peripheral(
            peripheral: CMBluetoothGatt,
            didWriteValueForCharacteristic: BGC,
            error: NSError?
        ) {
            writeChannel.launchAndSendResponse(didWriteValueForCharacteristic, error.hashCode())
        }

    }

    actual fun Boolean.checkOperationInitiationSucceeded() {
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
        inline val isSuccess get() = status == null
    }

    init {
        val closeOnDisconnect = connectionSettings.allowAutoConnect.not()
        if (closeOnDisconnect) launch {
            stateChangeChannel.consumeEach { stateChange ->
                if (stateChange.newState == 0) { //CBPeripheralState.disconnected
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

actual abstract class CMBluetoothGattCallback
actual typealias CMBluetoothGatt = platform.CoreBluetooth.CBPeripheral