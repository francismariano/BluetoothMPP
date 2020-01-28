@file:Suppress("MemberVisibilityCanPrivate")

package com.github.francismariano.bluetoothMPP

@ExperimentalBleGattCoroutinesCoroutinesApi
expect sealed class GattException actual constructor(message: String?) : Exception {
    companion object {
        fun humanReadableStatusCode(statusCode: Int) : String
    }
}

@ExperimentalBleGattCoroutinesCoroutinesApi
expect class OperationInitiationFailedException : GattException

@ExperimentalBleGattCoroutinesCoroutinesApi
expect class OperationFailedException (statusCode: Int) : GattException
