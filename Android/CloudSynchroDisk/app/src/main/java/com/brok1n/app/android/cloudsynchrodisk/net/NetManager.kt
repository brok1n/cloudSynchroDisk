package com.brok1n.app.android.cloudsynchrodisk.net

class NetManager {

    val udpSender = UdpSender()

    fun start() {

        udpSender.start()

    }

    companion object {
        val instance = NetManager()
    }
}