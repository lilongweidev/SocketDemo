package com.llw.socket.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.llw.socket.R
import com.llw.socket.client.SocketClient
import com.llw.socket.databinding.DialogEditIpBinding

/**
 * 客户端Plus页面
 */
class ClientPlusActivity: BaseSocketActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //连接服务/关闭服务
        setClientTitle { if (connectSocket) closeConnect() else showEditDialog() }
        //发送消息给服务端
        btnSendMsg.setOnClickListener {
            val msg = etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (connectSocket) connectSocket else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            sendToServer(msg)
        }
    }

    private fun showEditDialog() {
        val dialogBinding =
            DialogEditIpBinding.inflate(LayoutInflater.from(this@ClientPlusActivity), null, false)
        AlertDialog.Builder(this@ClientPlusActivity).apply {
            setIcon(R.drawable.ic_connect)
            setTitle("连接Ip地址")
            setView(dialogBinding.root)
            setPositiveButton("确定") { dialog, _ ->
                val ip = dialogBinding.etIpAddress.text.toString()
                if (ip.isEmpty()) {
                    showMsg("请输入Ip地址");return@setPositiveButton
                }
                connectServer(ip)
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
        }.show()
    }
}