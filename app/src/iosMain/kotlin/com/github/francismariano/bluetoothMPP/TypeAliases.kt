package com.github.francismariano.bluetoothMPP

import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBService

actual typealias BG = CBService
actual typealias BGC = CBCharacteristic
actual typealias BGD = CBDescriptor