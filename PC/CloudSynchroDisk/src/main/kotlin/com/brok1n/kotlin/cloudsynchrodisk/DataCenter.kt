package com.brok1n.kotlin.cloudsynchrodisk

import com.brok1n.kotlin.cloudsynchrodisk.Constant.PC_USER_ID
import com.brok1n.kotlin.cloudsynchrodisk.Constant.SERVER_USER_ID
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class DataCenter {

    var serverIp = "192.168.1.4"
    var serverUdpPort = 22888
    var serverTcpPort = 22889
    var serverUdpLocalPort = 22890
    var serverTcpLocalPort = 22891
    var heartBeatPeriodic:Long = 1000 * 10

    val userId = PC_USER_ID.md516().base64().md516()
    val serverUserId = SERVER_USER_ID.md516().base64().md516()

    val p2pClientList = ConcurrentLinkedQueue<Machine>()

    fun initData() {

//        serverIp = "192.168.1.4"
        serverIp = "140.143.205.244"
    }

    companion object {
        val instance = DataCenter()
    }
}