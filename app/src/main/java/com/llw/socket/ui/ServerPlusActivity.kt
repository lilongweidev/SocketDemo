package com.llw.socket.ui

import android.os.Bundle

/**
 * 服务端Plus页面
 */
class ServerPlusActivity : BaseSocketActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //开启服务/停止服务
        setServerTitle { if (openSocket) stopServer() else startServer() }

        //发送消息给服务端
        btnSendMsg.setOnClickListener {
            val msg = etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (openSocket) openSocket else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            sendToClient(msg)
        }
    }
}