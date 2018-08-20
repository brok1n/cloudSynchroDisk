package com.brok1n.app.android.cloudsynchrodisk.data

import com.brok1n.app.android.cloudsynchrodisk.base64
import com.brok1n.app.android.cloudsynchrodisk.md516
import java.util.*

class DataCenter {

    var serverIp = "192.168.1.4"
    var serverUdpPort = 22888
    var serverTcpPort = 22889
    var serverUdpLocalPort = 22890
    var serverTcpLocalPort = 22891
    var heartBeatPeriodic:Long = 1000 * 5

    var userId = ""

    val p2pDevice = Machine()

    init {
//        serverIp = "192.168.1.4"
        serverIp = "140.143.205.244"
    }

    fun setUser( userid: String ) {
        userId = userid.md516().base64().md516()
    }

    companion object {
        val instance = DataCenter()
    }
}