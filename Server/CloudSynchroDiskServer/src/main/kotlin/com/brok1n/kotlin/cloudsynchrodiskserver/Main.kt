package com.brok1n.kotlin.cloudsynchrodiskserver

import com.brok1n.kotlin.cloudsynchrodiskserver.Constant.MAX_HEART_BEAT_PERIODIC
import java.util.*
import kotlin.concurrent.thread


fun main(args:Array<String>) {

    log("cloud synchro disk server running")


    UdpServer.instance.port = DataCenter.instance.server_udp_port

    UdpServer.instance.start()


    Timer().schedule(object : TimerTask(){
        override fun run() {

            log("SERVER STATUS:")
            val machineCount = DataCenter.instance.machineMap.size
            var onlineCount = 0
            DataCenter.instance.machineMap.values.forEach {
                if ( (System.currentTimeMillis() - it.lastHeartBeatTime) < MAX_HEART_BEAT_PERIODIC ) {
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


