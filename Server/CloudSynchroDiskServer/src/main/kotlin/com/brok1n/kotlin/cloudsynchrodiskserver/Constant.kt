package com.brok1n.kotlin.cloudsynchrodiskserver

import sun.misc.BASE64Encoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

object Constant {

    val PROJECT_NAME = "CloudSynchroDisk Server"

    //最大心跳包间隔时间
    val MAX_HEART_BEAT_PERIODIC = 40 * 1000

    val SERVER_UDP_PORT = 22888

    //PC用户名
    val PC_USER_ID = "pc_CloudSynchroDisk_pc"
    //Server用户名
    val SERVER_USER_ID = "server_CloudSynchroDisk_server"

}

object CMD_TYPE {
    val CMD_TYPE_HEARTBEAT = "001010"
    val CMD_TYPE_HEARTBEAT_BACK = "001020"
    val CMD_TYPE_REGISTER = "002020"
    val CMD_TYPE_DATA = "003030"
}

object SendStatus {
    val SEND_STATUS_TYPE_SUCCESS = 0x001010
    val SEND_STATUS_TYPE_SENDED = 0x002020
    val SEND_STATUS_TYPE_FAILED = 0x003030
}


fun log( msg: String? ){
    msg?.let {
        println("${Constant.PROJECT_NAME}:${Date().dateTimeString()}:info:$it")
    }
}

fun loge( msg: String? ){
    msg?.let {
        println("${Constant.PROJECT_NAME}:${Date().dateTimeString()}:error:$it")
    }
}

/**
 * 日期转化为字符串
 */
fun Date.stringFormat(formatType:String):String{
    return SimpleDateFormat(formatType).format(this)
}

/**
 * 日期转化为字符串
 */
fun Date.dateString():String{
    return SimpleDateFormat("yyyy-MM-dd").format(this)
}

/**
 * 日期转化为字符串
 */
fun Date.dateTimeString():String{
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this)
}

fun String.md532(): String {
    try {
        //获取md5加密对象
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        //对字符串加密，返回字节数组
        val digest:ByteArray = instance.digest(this.toByteArray())
        var sb : StringBuffer = StringBuffer()
        for (b in digest) {
            //获取低八位有效值
            var i :Int = b.toInt() and 0xff
            //将整数转化为16进制
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                hexString = "0" + hexString
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

fun String.md516(): String {
    try {
        //获取md5加密对象
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        //对字符串加密，返回字节数组
        val digest:ByteArray = instance.digest(this.toByteArray())
        var sb : StringBuffer = StringBuffer()
        for (b in digest) {
            //获取低八位有效值
            var i :Int = b.toInt() and 0xff
            //将整数转化为16进制
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                hexString = "0" + hexString
            }
            sb.append(hexString)
        }
        return sb.toString().substring(8, 24)

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

/**
 * Convert byte[] to hex string
 *
 * @param src byte[] data
 * @return hex string
 */
fun ByteArray.bytesToHexString(): String {
    val stringBuilder = StringBuilder("")
    if (this.size <= 0) {
        return ""
    }
    for (i in this.indices) {
        val v = this[i].toInt() and 0xFF
        val hv = Integer.toHexString(v)
        if (hv.length < 2) {
            stringBuilder.append(0)
        }
        stringBuilder.append(hv)
    }
    return stringBuilder.toString()
}

fun String.base64():String {
    try {
        return BASE64Encoder().encode(this.toByteArray())
    }catch (e:Exception){
        e.printStackTrace()
    }

    return ""
}