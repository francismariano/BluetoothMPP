package com.github.francismariano.bluetoothMPP

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex

expect val STATUS_SUCCESS: Int

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@ExperimentalBleGattCoroutinesCoroutinesApi
expect class GattConnectionImpl(
    bluetoothDevice: CMBluetoothDevice
) {
    val rssiChannel: Channel<GattResponse<Int>>
    val servicesDiscoveryChannel: Channel<GattResponse<List<CMBluetoothGattService>>>
    val readChannel: Channel<GattResponse<BGC>>
    val writeChannel: Channel<GattResponse<BGC>>
    val reliableWriteChannel: Channel<GattResponse<Unit>>
    val characteristicChangedChannel: BroadcastChannel<BGC>
    val readDescChannel: Channel<GattResponse<BGD>>
    val writeDescChannel: Channel<GattResponse<BGD>>
    val mtuChannel: Channel<GattResponse<Int>>
    val phyReadChannel: Channel<GattResponse<Phy>>

    val isConnectedBroadcastChannel: ConflatedBroadcastChannel<Boolean>
    val isConnectedChannel: ReceiveChannel<Boolean> //get() = isConnectedBroadcastChannel.openSubscription()

    var isClosed: Boolean
    var closedException: ConnectionClosedException?
    val stateChangeBroadcastChannel: ConflatedBroadcastChannel<StateChange>

    var bluetoothGatt: BG?
    fun requireGatt(): BG

    fun closeInternal(notifyStateChangeChannel: Boolean, cause: ConnectionClosedException)

    val callback: CMBluetoothGattCallback

    fun Boolean.checkOperationInitiationSucceeded()

    /** @see gattRequest */
    val bleOperationMutex: Mutex
    val reliableWritesMutex: Mutex
    var reliableWriteOngoing: Boolean

    suspend inline fun <E> gattRequest(
        ch: ReceiveChannel<GattResponse<E>>,
        operation: CMBluetoothGatt.() -> Boolean
    ): E

    fun <E> SendChannel<GattResponse<E>>.launchAndSendResponse(e: E, status: Int)

    inline fun checkNotClosed()

    class GattResponse<out E>(e: E, status: Int)
}

expect abstract class CMBluetoothGattCallback
expect class CMBluetoothGatt
