package com.brok1n.app.android.cloudsynchrodisk.net

import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_DATA
import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_HEARTBEAT
import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_HEARTBEAT_BACK
import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_REGISTER
import com.brok1n.app.android.cloudsynchrodisk.SendStatus.SEND_STATUS_TYPE_FAILED
import com.brok1n.app.android.cloudsynchrodisk.SendStatus.SEND_STATUS_TYPE_SENDED
import com.brok1n.app.android.cloudsynchrodisk.SendStatus.SEND_STATUS_TYPE_SUCCESS
import com.brok1n.app.android.cloudsynchrodisk.log
import com.brok1n.app.android.cloudsynchrodisk.loge
import com.brok1n.app.android.cloudsynchrodisk.md516
import com.brok1n.app.android.cloudsynchrodisk.data.DataCenter
import com.brok1n.app.android.cloudsynchrodisk.data.Machine
import com.brok1n.app.android.cloudsynchrodisk.data.Message
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

class UdpSender {

    //心跳定时器
    var heartBeatTimer = Timer()

    //1，创建udp服务。通过DatagramSocket对象。
    var ds = DatagramSocket(DataCenter.instance.localUdpPort)


    fun sendUdpData( msg:String ): Int {
        var sendStatus:Int = SEND_STATUS_TYPE_FAILED
        try {
            if ( ds.isClosed ) {
                loge("ds 被关闭 重新创建")
                ds = DatagramSocket(DataCenter.instance.localUdpPort)
            }
            //2，确定数据，并封装成数据包。DatagramPacket(byte[] buf, int length, InetAddress address, int port)
            //数据封包
            //用户ID(16字节)   数据包ID(unix时间戳13字节)  数据长度(3字节)  数据
            val msgLen = msg.length
            var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
//            val dataStr = "${DataCenter.instance.userId}|${System.currentTimeMillis().toString().substring(4)}|$dataLen|$msg"
            val dataStr = "${DataCenter.instance.userId}${System.currentTimeMillis().toString().substring(4)}$dataLen$msg"
            val dataByteArray = dataStr.toByteArray()
            val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(DataCenter.instance.serverIp), DataCenter.instance.serverUdpPort)

            //3，通过socket服务，将已有的数据包发送出去。通过send方法。
            log("发送一条数据:$dataStr")
            val startTime = System.currentTimeMillis()
            ds.send(dp)
            sendStatus = SEND_STATUS_TYPE_SENDED

            val recvBuf = ByteArray(512)
            val recvDp = DatagramPacket(recvBuf, recvBuf.size)

            ds.receive(recvDp)//上面while(true)，此处receive()为阻塞式方法，无接收等待
            val endTime = System.currentTimeMillis()
            val ip = recvDp.address.hostAddress
            val port = recvDp.port
            val data = String(recvDp.data, 0, recvDp.length)
            log("接收到服务器响应:$data")
            if ( data.toUpperCase().equals(dataStr.md516().toUpperCase())) {
                log("数据完整性校验通过 已确定服务器接收到本次发送的消息")
            }

            log("$ip:$port:$data")
            log("延迟:${endTime - startTime}ms")

            //解析出PC的通信终端信息
            val dataSp = data.split("|")
            if ( dataSp.size > 2 ) {
                DataCenter.instance.p2pDevice.apply {
                    this.ip = dataSp[1]
                    this.port = dataSp[2].toInt()
                }
            }

            //4，关闭资源。
//            ds.close()
            sendStatus = SEND_STATUS_TYPE_SUCCESS
        }catch (e:Exception){
            loge(e.message)
        }
        return sendStatus
    }




    val pendingSendDataQueue = ConcurrentLinkedQueue<Message>()
    val sendedDataMap = ConcurrentHashMap<String, String>()

    var running = false

    fun start(){

        running = true

        startUdpServer()

        startSendHeartBeat()

    }

    private fun startUdpServer() {

        thread {
            udpSendThread()
        }

        thread {
            udpReadThread()
        }
    }

    private fun udpReadThread() {
        try {
            while ( running ) {
                val recv = receiveData()

                try {
                    handleRecv(recv)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
        println("udp read thread exit.................")
    }

    fun handleRecv(recv: Array<String>) {
        val ip = recv[0]
        val port = recv[1]
        val data = recv[2]

        val userId = data.substring(0, 16)
        val type = data.substring(25, 31)
        val dataMsg = data.substring(34)

        when (type) {
            CMD_TYPE_HEARTBEAT_BACK -> {
                val dataSp = dataMsg.split("|")
                val checkStr = dataSp[0]
                val sendedMsg = sendedDataMap[checkStr.toUpperCase()]
                if (sendedMsg != null) {
                    log("Android 收到一条心跳响应包$ip:$port: $sendedMsg")
                    sendedDataMap.remove(checkStr.toUpperCase())
                } else {
                    log("Android 收到一条未知心跳响应包$ip:$port: $data")
                }

                //解析出PC的通信终端信息
                if (dataSp.size > 2) {
                    DataCenter.instance.p2pDevice.apply {
                        this.ip = dataSp[1]
                        this.port = dataSp[2].toInt()
                    }
                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2 P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
                }

            }
            CMD_TYPE_REGISTER -> {
                //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
                val registerData = data.substring(34)
                log("Android 收到一条P2P注册包$ip:$port:$registerData")
                val registerDataSp = registerData.split("|")
                val registerIp = registerDataSp[0]
                val registerPort = registerDataSp[1].toInt()

                sendUdpDataToMachine(Message(CMD_TYPE_REGISTER, "register"), Machine(registerIp, registerPort, "", System.currentTimeMillis()))
            }
            else -> {
                log("Android 收到一条未知数据$ip:$port: $data")

            }
        }
    }


    private fun udpSendThread() {
        try {
            while ( running ) {
                if ( pendingSendDataQueue.isEmpty() ) {
                    Thread.sleep(10)
                    continue
                }
                val msg = pendingSendDataQueue.peek()
                val sendStr = sendUdpData(msg)
                if ( sendStr.length > 1 ) {
                    sendedDataMap[sendStr.md516().toUpperCase()] = sendStr
                    pendingSendDataQueue.remove(msg)
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
        println("udp send thread exit.................")
    }

    fun stop(){
        heartBeatTimer.cancel()
    }

    private fun startSendHeartBeat() {

        heartBeatTimer.schedule(object : TimerTask(){
            override fun run() {
                sendHeartBeat()
            }
        }, 0, DataCenter.instance.heartBeatPeriodic)
    }

    private fun sendHeartBeat(){
        log("Android 发送一条心跳数据:${pendingSendDataQueue.size}")
        sendData("heart beat", CMD_TYPE_HEARTBEAT)
    }

    private fun sendData( msg: String, type:String ) {
        pendingSendDataQueue.add(Message(type, msg))
    }

    private fun sendUdpData( msg: Message ): String {
        var str = ""
        try {
            if ( ds.isClosed ) {
                loge("ds 被关闭 重新创建")
                ds = DatagramSocket(DataCenter.instance.localUdpPort)
            }
            //2，确定数据，并封装成数据包。DatagramPacket(byte[] buf, int length, InetAddress address, int port)
            //数据封包
            //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
            val msgLen = msg.data.length
            var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
//            val dataStr = "${DataCenter.instance.userId}|${System.currentTimeMillis().toString().substring(4)}|$dataLen|$msg"
            val dataStr = "${DataCenter.instance.userId}${System.currentTimeMillis().toString().substring(4)}${msg.type}$dataLen$msg"
            val dataByteArray = dataStr.toByteArray()
            val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(DataCenter.instance.serverIp), DataCenter.instance.serverUdpPort)

            //3，通过socket服务，将已有的数据包发送出去。通过send方法。
            log("Android 发送一条数据:$dataStr")
            val startTime = System.currentTimeMillis()
            ds.send(dp)
            str = dataStr
        }catch (e:Exception){
            loge(e.message)
        }
        return str
    }

    private fun receiveData():Array<String> {

        val recvBuf = ByteArray(512)
        val recvDp = DatagramPacket(recvBuf, recvBuf.size)

        ds.receive(recvDp)//上面while(true)，此处receive()为阻塞式方法，无接收等待
        val ip = recvDp.address.hostAddress
        val port = recvDp.port
        val data = String(recvDp.data, 0, recvDp.length)

        return arrayOf(ip, port.toString(), data )
    }


    fun sendUdpDataToMachine( ds: DatagramSocket, msg: Message, machine: Machine){

        //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
        val msgLen = msg.data.length
        var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
//            val dataStr = "${DataCenter.instance.userId}|${System.currentTimeMillis().toString().substring(4)}|$dataLen|$msg"
        val dataStr = "${DataCenter.instance.userId}${System.currentTimeMillis().toString().substring(4)}${msg.type}$dataLen$msg"
        val dataByteArray = dataStr.toByteArray()
        val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(machine.ip), machine.port)

        //3，通过socket服务，将已有的数据包发送出去。通过send方法。
        log("Android 发送一条UDP数据 -> ${machine.ip}:${machine.port}:$dataStr")
        val startTime = System.currentTimeMillis()
        ds.send(dp)

    }

    fun sendUdpDataToMachine( msg: Message, machine: Machine){
        val tmpDs = DatagramSocket()

        val msgLen = msg.data.length
        var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
//            val dataStr = "${DataCenter.instance.userId}|${System.currentTimeMillis().toString().substring(4)}|$dataLen|$msg"
        val dataStr = "${DataCenter.instance.userId}${System.currentTimeMillis().toString().substring(4)}${msg.type}$dataLen$msg"
        val dataByteArray = dataStr.toByteArray()
        val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(machine.ip), machine.port)

        //3，通过socket服务，将已有的数据包发送出去。通过send方法。
        log("Android 发送一条UDP数据 -> ${machine.ip}:${machine.port}:$dataStr")
        val startTime = System.currentTimeMillis()
        tmpDs.send(dp)

        tmpDs.close()
    }

//    companion object {
//        val instance = UdpSender()
//    }

}