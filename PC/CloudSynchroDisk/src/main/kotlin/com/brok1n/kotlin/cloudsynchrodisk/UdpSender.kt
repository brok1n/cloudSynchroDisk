package com.brok1n.kotlin.cloudsynchrodisk

import com.brok1n.kotlin.cloudsynchrodisk.CMD_TYPE.CMD_TYPE_HEARTBEAT
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.thread

class UdpSender {

    var heartBeatTimer = Timer()

    //1，创建udp服务。通过DatagramSocket对象。
    lateinit var ds:DatagramSocket

    val messageHandler = MessageHandler()

    var running = false

    fun start(){

        running = true

        ds = DatagramSocket(DataCenter.instance.serverUdpLocalPort)

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

        val recvBuf = ByteArray(2048)
        val recvDp = DatagramPacket(recvBuf, recvBuf.size)
        try {
            while ( running ) {

                Arrays.fill(recvBuf, 0.toByte())
                ds.receive(recvDp)//上面while(true)，此处receive()为阻塞式方法，无接收等待
                messageHandler.hanleMessage(ServerMessage(recvDp.address.hostAddress, recvDp.port, String(recvDp.data, 0, recvDp.length), System.currentTimeMillis()))
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
        println("udp read thread exit.................")
    }


    private fun udpSendThread() {
        try {
            while ( running ) {
                if ( DataCenter.instance.pendingSendDataQueue.isEmpty() ) {
                    Thread.sleep(10)
                    continue
                }
                val msg = DataCenter.instance.pendingSendDataQueue.peek()
                val sendStr = sendUdpData(msg)
                if ( sendStr.length > 1 ) {
                    DataCenter.instance.sendedDataMap[sendStr.md516().toUpperCase()] = sendStr
                    DataCenter.instance.pendingSendDataQueue.remove(msg)
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
                log("PC 发送一条心跳数据:${DataCenter.instance.pendingSendDataQueue.size}")
                sendData(PendingSendMessage(CMD_TYPE_HEARTBEAT, "heart beat"))
            }
        }, 0, DataCenter.instance.heartBeatPeriodic)
    }

    fun sendData( msg: PendingSendMessage) {
        DataCenter.instance.pendingSendDataQueue.add(msg)
    }

    private fun sendUdpData( msg: PendingSendMessage ): String {
        var str = ""
        try {
            if ( ds.isClosed ) {
                loge("ds 被关闭 重新创建")
                ds = DatagramSocket(DataCenter.instance.serverUdpLocalPort)
            }
            //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
            val msgLen = msg.data.length
            var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
            val dataStr = "${DataCenter.instance.userId}${System.currentTimeMillis().toString().substring(4)}${msg.type}$dataLen${msg.data}"
            val dataByteArray = dataStr.toByteArray()
            val targetIp = if (msg.targetIp.length > 5) msg.targetIp else DataCenter.instance.serverIp
            val targetPort = if ( msg.targetPort > 1000 ) msg.targetPort else DataCenter.instance.serverUdpPort
            val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(targetIp), targetPort)
            log("PC 发送一条数据  -> ${targetIp}:${targetPort}:$dataStr")
            ds.send(dp)
            str = dataStr
        }catch (e:Exception){
            loge(e.message)
        }
        return str
    }

    companion object {
        val instance = UdpSender()
    }

}