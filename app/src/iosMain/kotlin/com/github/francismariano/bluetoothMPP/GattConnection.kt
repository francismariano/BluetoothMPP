package com.github.francismariano.bluetoothMPP

import kotlinx.coroutines.channels.ReceiveChannel

actual class CMBluetoothGattService
@ExperimentalBleGattCoroutinesCoroutinesApi
actual interface GattConnection {
    actual companion object {
//        @ObsoleteCoroutinesApi
//        @ExperimentalCoroutinesApi
//        actual operator fun invoke(
//            bluetoothDevice: CMBluetoothDevice,
//            connectionSettings: ConnectionSettings
//        ): GattConnection {
//
//        }

        actual val clientCharacteristicConfiguration: CMUUID
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    }

    actual val bluetoothDevice: CMBluetoothDevice
    actual val isConnected: Boolean
    actual suspend fun connect()
    actual suspend fun disconnect()
    actual fun close(notifyStateChangeChannel: Boolean)
    actual suspend fun readRemoteRssi(): Int
    actual fun requestPriority(priority: Int)
    actual suspend fun discoverServices(): List<CMBluetoothGattService>
    actual fun setCharacteristicNotificationsEnabled(
        characteristic: BGC,
        enable: Boolean
    )

    actual fun openNotificationSubscription(
        characteristic: BGC,
        disableNotificationsOnChannelClose: Boolean
    ): ReceiveChannel<BGC>

    actual suspend fun setCharacteristicNotificationsEnabledOnRemoteDevice(
        characteristic: BGC,
        enable: Boolean
    )

    actual fun getService(uuid: CMUUID): CMBluetoothGattService?
    actual suspend fun readCharacteristic(characteristic: BGC): BGC
    actual suspend fun writeCharacteristic(characteristic: BGC): BGC
    actual suspend fun reliableWrite(writeOperations: suspend GattConnection.() -> Unit)
    actual suspend fun readDescriptor(desc: BGD): BGD
    actual suspend fun writeDescriptor(desc: BGD): BGD
    actual suspend fun readPhy(): Phy
    actual suspend fun requestMtu(mtu: Int): Int
    actual val stateChangeChannel: ReceiveChannel<StateChange>
    actual val notifyChannel: ReceiveChannel<BGC>

}

actual class CMUUID
actual class CMBluetoothDevice