package com.brok1n.kotlin.cloudsynchrodisk


data class PendingSendMessage(var type: String = "", var data:  String = "", var targetIp: String = "", var targetPort: Int = 0)