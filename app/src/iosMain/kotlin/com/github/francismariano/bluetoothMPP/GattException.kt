package com.github.francismariano.bluetoothMPP

@ExperimentalBleGattCoroutinesCoroutinesApi
actual sealed class GattException actual constructor(message: String?) : Exception(message) {
    actual companion object {
        actual fun humanReadableStatusCode(statusCode: Int): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}