package com.brok1n.kotlin.cloudsynchrodiskserver

import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_DATA
import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_HEARTBEAT
import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_HEARTBEAT_BACK
import com.brok1n.kotlin.cloudsynchrodiskserver.CMD_TYPE.CMD_TYPE_REGISTER
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread


class UdpServer {

    var port = 22888

    var running = false

    fun start(){
        running = true
        thread {
            run()
        }
    }

    fun stop(){
        running = false
        //1，创建udp服务。通过DatagramSocket对象。
        val ds = DatagramSocket()

        //2，确定数据，并封装成数据包。DatagramPacket(byte[] buf, int length, InetAddress address, int port)

        val buf = "udp server stop".toByteArray()
        val dp = DatagramPacket(buf, buf.size, InetAddress.getByName("127.0.0.1"), port)

        //3，通过socket服务，将已有的数据包发送出去。通过send方法。
        ds.send(dp)
        //4，关闭资源。
        ds.close()

    }

    private fun run(){
        log("udp server running ${System.currentTimeMillis()}")
        val ds = DatagramSocket(port)//接收端监听指定端口
        try {
            while ( running ) {

                val buf = ByteArray(1024)
                val dp = DatagramPacket(buf, buf.size)

                ds.receive(dp)//上面while(true)，此处receive()为阻塞式方法，无接收等待
                val recvTime = System.currentTimeMillis()

                val data = String(dp.data, 0, dp.length)
                val address = dp.address
                val ip = address.hostAddress
                val port = dp.port

                val userId = data.substring(0, 16)
                val type = data.substring(25, 31)
                val dataMsg = data.substring(34)

                var machine = DataCenter.instance.machineMap[userId]
                if (machine == null) {
                    machine = Machine()
                    machine.ip = ip
                    machine.port = port
                    machine.lastHeartBeatTime = recvTime
                    DataCenter.instance.machineMap[userId] = machine
                } else {
                    machine.lastHeartBeatTime = recvTime
                }

                when( type ){
                    CMD_TYPE_HEARTBEAT -> {
                        log("receive a heart beat data $ip:$port: $data")
                        var pcData = ""
                        val pcMachine = DataCenter.instance.machineMap[DataCenter.instance.pcUserId]
                        if ( pcMachine != null )
                            pcData = "${pcMachine.ip}|${pcMachine.port}"
                        val dd = "${data.md516()}|$pcData"
                        sendUdpData(ds, dd, CMD_TYPE_HEARTBEAT_BACK, ip, port)

                        if (userId != DataCenter.instance.pcUserId) {
                            var machine = DataCenter.instance.machineMap[DataCenter.instance.pcUserId]
                            machine?.let {
                                val register = "$ip|$port"
                                sendUdpData(ds, register, CMD_TYPE_REGISTER, machine.ip, machine.port)
                            }
                        }
                    }
                    CMD_TYPE_DATA -> {
                        log("receive a data $ip:$port: $data")
                    }
                    else -> {
                        log("receive a Unknown data $ip:$port: $data")
                    }
                }

            }
        }catch (e:Exception){
            log(e.message)
        }
        ds.close()
        log("udp server stoped ${System.currentTimeMillis()}")
    }

    fun sendUdpData( ds: DatagramSocket, msg:String, type:String, ip:String, port:Int ) {
        //数据封包
        //用户ID(16字节)   数据包ID(unix时间戳9字节) CMD_TYPE(6字节)  数据长度(3字节)  数据
        val msgLen = msg.length
        var dataLen = if ( msgLen < 10 ) "00$msgLen" else if ( msgLen < 100 ) "0$msgLen" else "$msgLen"
//            val dataStr = "${DataCenter.instance.userId}|${System.currentTimeMillis().toString().substring(4)}|$dataLen|$msg"
        val dataStr = "${DataCenter.instance.serverUserId}${System.currentTimeMillis().toString().substring(4)}$type$dataLen$msg"
        val dataByteArray = dataStr.toByteArray()
        val dp = DatagramPacket(dataByteArray, dataByteArray.size, InetAddress.getByName(ip), port)
        ds.send(dp)
    }


    companion object {
        val instance = UdpServer()
    }

}