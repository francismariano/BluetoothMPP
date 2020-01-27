@file:Suppress("MemberVisibilityCanPrivate")

package com.github.francismariano.bluetoothMPP

@ExperimentalBleGattCoroutinesCoroutinesApi
expect sealed class GattException actual constructor(message: String?) : Exception {
    companion object {
        fun humanReadableStatusCode(statusCode: Int) : String
    }
}

@ExperimentalBleGattCoroutinesCoroutinesApi
class OperationInitiationFailedException : GattException(null)

/** @see BluetoothGatt */
@ExperimentalBleGattCoroutinesCoroutinesApi
class OperationFailedException(
    val statusCode: Int
) : GattException(
    "status: ${humanReadableStatusCode(
        statusCode
    )}"
)
