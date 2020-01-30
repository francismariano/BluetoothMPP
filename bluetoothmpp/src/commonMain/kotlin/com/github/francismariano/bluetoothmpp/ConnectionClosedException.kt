package com.github.francismariano.bluetoothmpp

import kotlinx.coroutines.CancellationException

@ExperimentalBleGattCoroutinesCoroutinesApi
class ConnectionClosedException constructor(
    cause: Throwable? = null,
    messageSuffix: String = ""
) : CancellationException("The connection has been irrevocably closed $messageSuffix. Cause - $cause")