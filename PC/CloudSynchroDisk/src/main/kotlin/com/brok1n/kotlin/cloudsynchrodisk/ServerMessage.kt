package com.brok1n.kotlin.cloudsynchrodisk

import java.util.*

class ServerMessage(var ip:String, var port:Int, var data:String, var recvTime:Long) {

    private var uid = ""
    private var msgCmd = ""
    private var dataMsg = ""
    private var sendTime = 0L

    fun getUserId():String {
        if ( uid.length < 10 && data.length > 16)
            uid = data.substring(0, 16)
        return uid
    }

    fun getCmd():String {
        if ( msgCmd.length < 4 && data.length > 31)
            msgCmd = data.substring(25, 31)
        return msgCmd
    }

    fun getDataMsg():String {
        if ( dataMsg.length < 1 && data.length > 34)
            dataMsg = data.substring(34)
        return dataMsg
    }

    fun getSendTime(): Long {
        if ( sendTime < 1 && data.length > 25 ) {
            val sTime = data.substring(16, 25)
            val sendTimeStr = recvTime.toString().substring(0, 4) + sTime
            sendTime = sendTimeStr.toLong()
        }
        return sendTime
    }

    override fun toString(): String {
        return "${Date(getSendTime()).dateTimeMillisecondString()}-${Date(recvTime).dateTimeMillisecondString()}  ${ (recvTime - getSendTime())}ms $ip:$port  ${getCmd()}  ${getDataMsg()}"
    }
}