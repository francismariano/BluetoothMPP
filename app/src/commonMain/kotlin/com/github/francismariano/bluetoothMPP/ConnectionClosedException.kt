package com.github.francismariano.bluetoothMPP

import kotlinx.coroutines.CancellationException

@ExperimentalBleGattCoroutinesCoroutinesApi
class ConnectionClosedException internal constructor(
    cause: Throwable? = null,
    messageSuffix: String = ""
) : CancellationException("The connection has been irrevocably closed $messageSuffix. Cause - $cause")