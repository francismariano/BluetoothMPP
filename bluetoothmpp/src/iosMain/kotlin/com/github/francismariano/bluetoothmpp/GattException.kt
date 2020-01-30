package com.github.francismariano.bluetoothmpp

@ExperimentalBleGattCoroutinesCoroutinesApi
actual sealed class GattException actual constructor(message: String?) : Exception(message) {
    actual companion object {
        actual fun humanReadableStatusCode(statusCode: Int): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

@ExperimentalBleGattCoroutinesCoroutinesApi
actual class OperationInitiationFailedException : GattException(null)

@ExperimentalBleGattCoroutinesCoroutinesApi
actual class OperationFailedException actual constructor(statusCode: Int) : GattException(null)