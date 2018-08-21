package com.brok1n.kotlin.cloudsynchrodisk


class MessageHandler {


    fun hanleMessage( msg: ServerMessage ):Boolean {

        log(msg.toString())

        val handleStatus = handleCmd(msg)
        println("handle cmd status:" + handleStatus)

        return handleStatus
    }

    private fun handleCmd(msg: ServerMessage): Boolean {
        var status = false
        try {
            when(msg.getCmd()){
                CMD_TYPE.CMD_TYPE_HEARTBEAT_BACK -> {
                    status = handleHeartBeatBack(msg)
                }
                CMD_TYPE.CMD_TYPE_REGISTER -> {
                    status = handleRegister(msg)
                }
                CMD_TYPE.CMD_TYPE_DATA -> {
                    log("PC 收到了一条数据${msg.ip}:${msg.port}: ${msg.getDataMsg()}")
                }
                else -> {
                    log("PC 收到一条未知数据${msg.ip}:${msg.port}: ${msg.getDataMsg()}")
                }
            }
            return status
        }catch (e:Exception){
            e.printStackTrace()
        }
        return false
    }

    private fun handleRegister(msg: ServerMessage): Boolean {
        try {
            //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
            val registerData = msg.getDataMsg()

            log("PC 收到一条P2P注册包${msg.ip}:${msg.port}:$registerData")
            val registerDataSp = registerData.split("|")
            val registerIp = registerDataSp[0]
            val registerPort = registerDataSp[1].toInt()

            DataCenter.instance.pendingSendDataQueue.add(PendingSendMessage(CMD_TYPE.CMD_TYPE_DATA,"P2P DATA TEST 1 I am an PC I want go to Android",registerIp, registerPort))
            DataCenter.instance.pendingSendDataQueue.add(PendingSendMessage(CMD_TYPE.CMD_TYPE_DATA,"P2P DATA TEST 2 I am an PC I want go to Android",registerIp, registerPort))
            DataCenter.instance.pendingSendDataQueue.add(PendingSendMessage(CMD_TYPE.CMD_TYPE_DATA,"P2P DATA TEST 3 I am an PC I want go to Android",registerIp, registerPort))
            DataCenter.instance.pendingSendDataQueue.add(PendingSendMessage(CMD_TYPE.CMD_TYPE_DATA,"P2P DATA TEST 4 I am an PC I want go to Android",registerIp, registerPort))
            DataCenter.instance.pendingSendDataQueue.add(PendingSendMessage(CMD_TYPE.CMD_TYPE_DATA,"P2P DATA TEST 5 I am an PC I want go to Android",registerIp, registerPort))

        }catch (e:Exception){
            e.printStackTrace()
        }
        return false;
    }

    private fun handleHeartBeatBack(msg: ServerMessage):Boolean {
        try {
            val dataSp = msg.getDataMsg().split("|")
            val checkStr = dataSp[0]
            val sendedMsg = DataCenter.instance.sendedDataMap[checkStr.toUpperCase()]
            if ( sendedMsg != null ) {
                log("PC 收到一条心跳响应包${msg.ip}:${msg.port}: ${msg.getDataMsg()}")
                DataCenter.instance.sendedDataMap.remove(checkStr.toUpperCase())
            } else {
                log("PC 收到一条未知心跳响应包${msg.ip}:${msg.port}: ${msg.getDataMsg()}")
            }
            return true
        }catch (e:Exception ){
            e.printStackTrace()
        }
        return false
    }

    companion object {
        val instance = MessageHandler()
    }
}