package com.brok1n.kotlin.cloudsynchrodisk

import sun.rmi.runtime.Log


fun main(args: Array<String>) {

    log(" project start")

    /**
     *
     * 09cab1556510400874955500c32fb698
     * 09cab1556510400874955500c32fb698
     *
     * 6510400874955500
     * 6510400874955500
     * YnJvazFu
     * */

    DataCenter.instance.initData()
    log(DataCenter.instance.serverUserId)

    UdpSender.instance.start()



}

