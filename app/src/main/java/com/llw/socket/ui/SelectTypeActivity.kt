package com.llw.socket.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.llw.socket.R

class SelectTypeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_type)

        findViewById<Button>(R.id.btn_server).setOnClickListener {
            jumpActivity(ServerActivity::class.java)
        }

        findViewById<Button>(R.id.btn_client).setOnClickListener {
            jumpActivity(ClientActivity::class.java)
        }
    }
}