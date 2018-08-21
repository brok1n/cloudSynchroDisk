package com.brok1n.kotlin.cloudsynchrodiskserver

import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_DATA
import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_HEARTBEAT
import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_HEARTBEAT_BACK
import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_REGISTER
import sun.plugin2.message.Message
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import kotlin.concurrent.thread


class UdpServer {

    var running = false

    val messageHandler = MessageHandler.instance

    val ds = DatagramSocket(DataCenter.instance.server_udp_port)//接收端监听指定端口
    val buf = ByteArray(2048)
    val dp = DatagramPacket(buf, buf.size)

    fun start(){
        running = true
        thread {
            run()
        }

        DataCenter.instance.printMachineStatus()
    }

    fun stop(){
        running = false
        //1，创建udp服务。通过DatagramSocket对象。
        val ds = DatagramSocket()

        //2，确定数据，并封装成数据包。DatagramPacket(byte[] buf, int length, InetAddress address, int port)

        val buf = "udp server stop".toByteArray()
        val dp = DatagramPacket(buf, buf.size, InetAddress.getByName("127.0.0.1"), DataCenter.instance.server_udp_port)

        //3，通过socket服务，将已有的数据包发送出去。通过send方法。
        ds.send(dp)
        //4，关闭资源。
        ds.close()

    }

    private fun run(){
        log("udp server running ${System.currentTimeMillis()}")
        try {
            while ( running ) {
                Arrays.fill(buf, 0.toByte())
                ds.receive(dp)//上面while(true)，此处receive()为阻塞式方法，无接收等待
                messageHandler.hanleMessage(ClientMessage(dp.address.hostAddress,dp.port,String(dp.data, 0, dp.length), System.currentTimeMillis()))
            }
        }catch (e:Exception){
            log(e.message)
        }
        ds.close()
        log("udp server stoped ${System.currentTimeMillis()}")
    }

    fun sendUdpData( msg:String, type:String, ip:String, port:Int ):Boolean {
        try {
            val msgLen = msg.length
            var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
            //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
            val dataStr = "${DataCenter.instance.serverUserId}${System.currentTimeMillis().toString().substring(4)}$type$dataLen$msg"
            val dataByteArray = dataStr.toByteArray()
            val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(ip), port)
            ds.send(dp)
            return true
        }catch (e:Exception){
            e.printStackTrace()
        }
        return false
    }


    companion object {
        val instance = UdpServer()
    }

}