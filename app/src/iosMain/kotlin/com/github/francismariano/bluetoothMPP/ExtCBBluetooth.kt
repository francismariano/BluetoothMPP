package com.github.francismariano.bluetoothMPP

import platform.CoreBluetooth.CBPeripheral

fun CBPeripheral.extReadRSSI(): Boolean {
    this.readRSSI()
    return true
}

fun CBPeripheral.discoverServices(): Boolean {
    this.discoverServices(null)
    return true
}

fun CBPeripheral.readCharacteristic(characteristc: BGC): Boolean {
    this.readValueForCharacteristic(characteristc)
    return true
}

fun CBPeripheral.writeCharacteristic(characteristc: BGC): Boolean {
    this.writeCharacteristic(characteristc)
    return true
}