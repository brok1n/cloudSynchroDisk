package com.brok1n.kotlin.cloudsynchrodiskserver

import com.brok1n.kotlin.cloudsynchrodiskserver.Constant.PC_USER_ID
import com.brok1n.kotlin.cloudsynchrodiskserver.Constant.SERVER_USER_ID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class DataCenter{

    var server_ip = "0.0.0.0"
    var server_udp_port = 22888
    var server_tcp_port = 22889

    val machineQueue = ConcurrentLinkedQueue<Machine>()
    val machineMap = ConcurrentHashMap<String, Machine>()

    val pcUserId = PC_USER_ID.md516().base64().md516()
    val serverUserId = SERVER_USER_ID.md516().base64().md516()

    companion object {
        val instance = DataCenter()
    }

}