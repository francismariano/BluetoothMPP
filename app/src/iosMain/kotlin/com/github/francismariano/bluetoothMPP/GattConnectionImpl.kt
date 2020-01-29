package com.github.francismariano.bluetoothMPP

import CMBluetoothDevice
import CMBluetoothGattService
import Phy
import StateChange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.sync.Mutex

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@ExperimentalBleGattCoroutinesCoroutinesApi
actual class GattConnectionImpl actual constructor(
    bluetoothDevice: CMBluetoothDevice
) {
    actual val rssiChannel: Channel<GattResponse<Int>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val servicesDiscoveryChannel: Channel<GattResponse<List<CMBluetoothGattService>>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val readChannel: Channel<GattResponse<BGC>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val writeChannel: Channel<GattResponse<BGC>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val reliableWriteChannel: Channel<GattResponse<Unit>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val characteristicChangedChannel: BroadcastChannel<BGC>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val readDescChannel: Channel<GattResponse<BGD>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val writeDescChannel: Channel<GattResponse<BGD>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val mtuChannel: Channel<GattResponse<Int>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val phyReadChannel: Channel<GattResponse<Phy>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isConnectedBroadcastChannel: ConflatedBroadcastChannel<Boolean>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val isConnectedChannel: ReceiveChannel<Boolean>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual var isClosed: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual var closedException: ConnectionClosedException?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    actual val stateChangeBroadcastChannel: ConflatedBroadcastChannel<StateChange>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual var bluetoothGatt: BG?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual fun requireGatt(): BG {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun closeInternal(
        notifyStateChangeChannel: Boolean,
        cause: ConnectionClosedException
    ) {
    }

    actual val callback: CMBluetoothGattCallback
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun Boolean.checkOperationInitiationSucceeded() {
    }

    /** @see gattRequest */
    actual val bleOperationMutex: Mutex
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val reliableWritesMutex: Mutex
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual var reliableWriteOngoing: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    actual suspend inline fun <E> gattRequest(
        ch: ReceiveChannel<GattResponse<E>>,
        operation: CMBluetoothGatt.() -> Boolean
    ): E {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun <E> SendChannel<GattResponse<E>>.launchAndSendResponse(
        e: E,
        status: Int
    ) {
    }

    actual inline fun checkNotClosed() {
    }

    actual class GattResponse<out E> actual constructor(e: E, status: Int)

}

actual val STATUS_SUCCESS: Int
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual abstract class CMBluetoothGattCallback
actual class CMBluetoothGatt