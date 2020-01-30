package com.github.francismariano.bluetoothmpp

import kotlinx.coroutines.channels.ReceiveChannel

@ExperimentalBleGattCoroutinesCoroutinesApi
expect interface GattConnection {
    companion object {
        // TODO: Implementation of invoke function
//        @ExperimentalCoroutinesApi
//        @ObsoleteCoroutinesApi
//        operator fun invoke(
//            bluetoothDevice: CMBluetoothDevice,
//            connectionSettings: ConnectionSettings // = ConnectionSettings()
//        ): GattConnection

        val clientCharacteristicConfiguration: CMUUID
    }

    val bluetoothDevice: CMBluetoothDevice
    val isConnected: Boolean

    suspend fun connect()
    suspend fun disconnect()
    fun close(notifyStateChangeChannel: Boolean = false)
    suspend fun readRemoteRssi(): Int
    fun requestPriority(priority: Int)
    suspend fun discoverServices(): List<CMBluetoothGattService>
    fun setCharacteristicNotificationsEnabled(characteristic: BGC, enable: Boolean)
    fun openNotificationSubscription(
        characteristic: BGC,
        disableNotificationsOnChannelClose: Boolean = true
    ): ReceiveChannel<BGC>

    suspend fun setCharacteristicNotificationsEnabledOnRemoteDevice(
        characteristic: BGC,
        enable: Boolean
    )

    fun getService(uuid: CMUUID): CMBluetoothGattService?
    suspend fun readCharacteristic(characteristic: BGC): BGC
    suspend fun writeCharacteristic(characteristic: BGC): BGC
    suspend fun reliableWrite(writeOperations: suspend GattConnection.() -> Unit)
    suspend fun readDescriptor(desc: BGD): BGD
    suspend fun writeDescriptor(desc: BGD): BGD
    suspend fun readPhy(): Phy
    suspend fun requestMtu(mtu: Int): Int
    val stateChangeChannel: ReceiveChannel<StateChange>
    val notifyChannel: ReceiveChannel<BGC>

}

// TODO: Implementation of ConnectionSettings inside GattConnection interface
//expect class ConnectionSettings(
//    autoConnect: Boolean = false,
//    allowAutoConnect: Boolean = autoConnect,
//    disconnectOnClose: Boolean = true,
//    transport: Int = 0, //CMBluetoothDevice.TRANSPORT_AUTO
//    phy: Int = 1 // CMBluetoothDevice.PHY_LE_1M_MASK
//)

expect class CMUUID
expect class CMBluetoothDevice
expect class CMBluetoothGattService

data class StateChange internal constructor(val status: Int, val newState: Int)
data class Phy(val tx: Int, val rx: Int)