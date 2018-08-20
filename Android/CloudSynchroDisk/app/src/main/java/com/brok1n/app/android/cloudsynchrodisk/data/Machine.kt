package com.brok1n.app.android.cloudsynchrodisk.data

class Machine(var ip:String = "", var port:Int = 0, var userId:String = "", var lastHeartBeatTime:Long = 0L) {

    override fun toString(): String {
        return "$ip:$port  $userId   $lastHeartBeatTime "
    }

}