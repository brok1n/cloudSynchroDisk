package com.brok1n.kotlin.cloudsynchrodiskserver

import com.brok1n.kotlin.cloudsynchrodiskserver.Constant.MAX_HEART_BEAT_PERIODIC
import java.util.*
import kotlin.concurrent.thread


fun main(args:Array<String>) {

    log("cloud synchro disk server running")


    UdpServer.instance.start()

}


