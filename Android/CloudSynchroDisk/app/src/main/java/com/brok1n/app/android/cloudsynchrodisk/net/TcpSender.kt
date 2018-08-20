package com.brok1n.app.android.cloudsynchrodisk.net

import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_DATA
import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_HEARTBEAT
import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_HEARTBEAT_BACK
import com.brok1n.app.android.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_REGISTER
import com.brok1n.app.android.cloudsynchrodisk.log
import com.brok1n.app.android.cloudsynchrodisk.loge
import com.brok1n.app.android.cloudsynchrodisk.md516
import com.brok1n.app.android.cloudsynchrodisk.data.DataCenter
import com.brok1n.app.android.cloudsynchrodisk.data.Machine
import com.brok1n.app.android.cloudsynchrodisk.data.Message
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

class TcpSender {

    //心跳定时器
    var heartBeatTimer = Timer()

    //1，创建udp服务。通过DatagramSocket对象。


    lateinit  var bw: BufferedWriter
    lateinit  var br: BufferedReader

    val pendingSendDataQueue = ConcurrentLinkedQueue<Message>()
    val sendedDataMap = ConcurrentHashMap<String, String>()

    var running = false

    fun start(){

        running = true

        startTcpServer()

        startSendHeartBeat()

    }

    private fun startTcpServer() {

        var socket = Socket()
        socket.reuseAddress = true
        socket.connect(InetSocketAddress(DataCenter.instance.serverIp, DataCenter.instance.serverTcpPort))

        DataCenter.instance.localTcpPort = socket.localPort

        bw = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
        br = BufferedReader(InputStreamReader(socket.getInputStream()))

        thread {
            tcpSendThread()
        }

        thread {
            tcpReadThread()
        }
    }

    private fun tcpReadThread() {
        try {
            while ( running ) {
                val recv = br.readLine()

                try {
                    handleRecv(recv)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
        println("tcp read thread exit.................")
    }

    fun handleRecv(recv: String) {

        log("Android 收到一条TCP数据: $recv")

//        val ip = recv[0]
//        val port = recv[1]
//        val data = recv[2]
//
//        val userId = data.substring(0, 16)
//        val type = data.substring(25, 31)
//        val dataMsg = data.substring(34)
//
//        when (type) {
//            CMD_TYPE_HEARTBEAT_BACK -> {
//                val dataSp = dataMsg.split("|")
//                val checkStr = dataSp[0]
//                val sendedMsg = sendedDataMap[checkStr.toUpperCase()]
//                if (sendedMsg != null) {
//                    log("Android 收到一条心跳响应包$ip:$port: $sendedMsg")
//                    sendedDataMap.remove(checkStr.toUpperCase())
//                } else {
//                    log("Android 收到一条未知心跳响应包$ip:$port: $data")
//                }
//
//                //解析出PC的通信终端信息
//                if (dataSp.size > 2) {
//                    DataCenter.instance.p2pDevice.apply {
//                        this.ip = dataSp[1]
//                        this.port = dataSp[2].toInt()
//                    }
//                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
//                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
//                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
//                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
//                    sendUdpDataToMachine(ds, Message(CMD_TYPE_DATA, "P2 P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PCP2P DATA TEST 1  I am an Android device I want go to PC"), DataCenter.instance.p2pDevice)
//                }
//
//            }
//            CMD_TYPE_REGISTER -> {
//                //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
//                val registerData = data.substring(34)
//                log("Android 收到一条P2P注册包$ip:$port:$registerData")
//                val registerDataSp = registerData.split("|")
//                val registerIp = registerDataSp[0]
//                val registerPort = registerDataSp[1].toInt()
//
//                sendUdpDataToMachine(Message(CMD_TYPE_REGISTER, "register"), Machine(registerIp, registerPort, "", System.currentTimeMillis()))
//            }
//            else -> {
//                log("Android 收到一条未知数据$ip:$port: $data")
//
//            }
//        }
    }


    private fun tcpSendThread() {
        try {
            while ( running ) {
                if ( pendingSendDataQueue.isEmpty() ) {
                    Thread.sleep(10)
                    continue
                }
                val msg = pendingSendDataQueue.peek()
                val sendStr = sendTcpData(msg)
                if ( sendStr.length > 1 ) {
                    sendedDataMap[sendStr.md516().toUpperCase()] = sendStr
                    pendingSendDataQueue.remove(msg)
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
        println("tcp send thread exit.................")
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
        log("Android 发送一条TCP心跳数据:${pendingSendDataQueue.size}")
        sendData("heart beat", CMD_TYPE_HEARTBEAT)
    }

    private fun sendData( msg: String, type:String ) {
        pendingSendDataQueue.add(Message(type, msg))
    }

    private fun sendTcpData(msg: Message ): String {
        var str = ""
        try {

            //2，确定数据，并封装成数据包。DatagramPacket(byte[] buf, int length, InetAddress address, int port)
            //数据封包
            //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
            val msgLen = msg.data.length
            val dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
            val dataStr = "${DataCenter.instance.userId}${System.currentTimeMillis().toString().substring(4)}${msg.type}$dataLen$msg"

            log("Android 发送一条TCP数据:$msg")

            bw.write(dataStr)
            bw.flush()

            str = dataStr
        }catch (e:Exception){
            loge(e.message)
        }
        return str
    }

//    companion object {
//        val instance = UdpSender()
//    }

}