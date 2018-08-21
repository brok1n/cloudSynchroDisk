package com.brok1n.kotlin.cloudsynchrodiskserver

class MessageHandler {


    fun hanleMessage( msg: ClientMessage ):Boolean {

        handleMachine(msg)

        val handleStatus = handleCmd(msg)
        println("handle cmd status:" + handleStatus)

        return handleStatus
    }

    private fun handleCmd(msg: ClientMessage): Boolean {
        var status = false
        try {
            when( msg.getCmd() ){
                CMD_TYPE.CMD_TYPE_HEARTBEAT -> {
                    status = handleHeartBeat(msg)
                }
                CMD_TYPE.CMD_TYPE_DATA -> {
                    log("receive a data $msg")
                }
                else -> {
                    log("receive a Unknown data $msg")
                }
            }
            return status
        }catch (e:Exception){
            e.printStackTrace()
        }
        return false
    }

    private fun handleHeartBeat(msg: ClientMessage):Boolean {
        var status = false
        try {
            log("receive a heart beat data $msg")
            var pcData = ""
            val pcMachine = DataCenter.instance.machineMap[DataCenter.instance.pcUserId]
            if ( pcMachine != null )
                pcData = "${pcMachine.ip}|${pcMachine.port}"
            val dd = "${msg.data.md516()}|$pcData"
            status = UdpServer.instance.sendUdpData(dd, CMD_TYPE.CMD_TYPE_HEARTBEAT_BACK, msg.ip, msg.port)
            if ( status ) {
                if (msg.getUserId() != DataCenter.instance.pcUserId) {
                    var machine = DataCenter.instance.machineMap[DataCenter.instance.pcUserId]
                    machine?.let {
                        val register = "${msg.ip}|${msg.port}"
                        status = UdpServer.instance.sendUdpData( register, CMD_TYPE.CMD_TYPE_REGISTER, machine.ip, machine.port)
                    }
                }
            }
            return status
        }catch (e:Exception ){
            e.printStackTrace()
        }
        return false
    }

    private fun handleMachine(msg: ClientMessage) {
        var machine = DataCenter.instance.machineMap[msg.getUserId()]
        if (machine == null) {
            machine = Machine()
            machine.ip = msg.ip
            machine.port = msg.port
            machine.lastHeartBeatTime = msg.recvTime
            DataCenter.instance.machineMap[msg.getUserId()] = machine
        } else {
            machine.ip = msg.ip
            machine.port = msg.port
            machine.lastHeartBeatTime = msg.recvTime
            machine.lastHeartBeatTime = msg.recvTime
        }
    }

    companion object {
        val instance = MessageHandler()
    }
}