package com.brok1n.kotlin.cloudsynchrodiskserver

import com.brok1n.kotlin.cloudsynchrodiskserver.Constant.PC_USER_ID
import com.brok1n.kotlin.cloudsynchrodiskserver.Constant.SERVER_USER_ID
import java.util.*
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

    fun printMachineStatus(){
        Timer().schedule(object : TimerTask(){
            override fun run() {

                log("SERVER STATUS:")
                val machineCount = machineMap.size
                var onlineCount = 0
                machineMap.values.forEach {
                    if ( (System.currentTimeMillis() - it.lastHeartBeatTime) < Constant.MAX_HEART_BEAT_PERIODIC) {
                        onlineCount ++
                    } else {
                        println("离线: ${System.currentTimeMillis()}" + it.toString())
                    }

                }

                log("machine count: $machineCount")
                log("machine online: $onlineCount\n")
            }
        }, 0, 1000 * 15)
    }

    companion object {
        val instance = DataCenter()
    }

}