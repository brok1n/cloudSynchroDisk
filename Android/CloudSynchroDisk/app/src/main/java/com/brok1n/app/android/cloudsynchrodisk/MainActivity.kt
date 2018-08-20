package com.brok1n.app.android.cloudsynchrodisk

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.brok1n.app.android.cloudsynchrodisk.net.NetManager
import com.brok1n.app.android.cloudsynchrodisk.data.DataCenter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        start_btn.setOnClickListener {

            val nickName = nick_name_edt.text.toString()

            DataCenter.instance.setUser(nickName)

            NetManager.instance.start()

        }

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
