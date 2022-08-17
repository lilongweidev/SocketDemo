package com.llw.socket.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.llw.socket.Message
import com.llw.socket.MsgAdapter
import com.llw.socket.client.ClientCallback
import com.llw.socket.client.SocketClient
import com.llw.socket.databinding.ActivityClientBinding
import com.llw.socket.databinding.DialogEditIpBinding


/**
 * 客户端页面
 */
class ClientActivity : BaseActivity(), ClientCallback {

    private val TAG = BaseActivity::class.java.simpleName
    private lateinit var binding: ActivityClientBinding

    //Socket服务是否连接
    private var connectSocket = false

    //消息列表
    private val messages = ArrayList<Message>()
    //消息适配器
    private lateinit var msgAdapter: MsgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        //连接服务/断开连接 客户端处理
        binding.tvConnectService.setOnClickListener {
            if (connectSocket) {
                SocketClient.closeConnect()
                connectSocket = false
                showMsg("关闭连接")
            } else {
                showEditDialog()
            }
            binding.tvConnectService.text = if (connectSocket) "关闭连接" else "连接服务"
        }
        //发送消息给服务端
        binding.btnSendMsg.setOnClickListener {
            val msg = binding.etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (connectSocket) connectSocket  else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            SocketClient.sendToServer(msg)
            binding.etMsg.setText("")
            updateList(2, msg)
        }
        //初始化列表
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@ClientActivity)
            adapter = msgAdapter
        }
    }

    private fun showEditDialog() {
        val dialogBinding = DialogEditIpBinding.inflate(LayoutInflater.from(this@ClientActivity),null,false)
        val dialog = AlertDialog.Builder(this@ClientActivity).create()
        dialog.show()
        dialog.window!!.setContentView(dialogBinding.root)
        dialogBinding.tvCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.tvSure.setOnClickListener {
            val ip = dialogBinding.etIpAddress.text.toString()
            if (ip.isEmpty()) {
                showMsg("请输入Ip地址");return@setOnClickListener
            }
            connectSocket = true
            SocketClient.connectServer(ip, this@ClientActivity)
            showMsg("连接服务")
            binding.tvConnectService.text = "关闭连接"
            dialog.dismiss()
        }
    }

    /**
     * 接收到服务端发的消息
     */
    override fun receiveServerMsg(msg: String) = updateList(1, msg)

    override fun otherMsg(msg: String) {
        Log.d(TAG, msg)
    }

    /**
     * 更新列表
     */
    private fun updateList(type: Int, msg: String) {
        messages.add(Message(type, msg))
        runOnUiThread {
            (if (messages.size == 0) 0 else messages.size - 1).apply {
                msgAdapter.notifyItemChanged(this)
                binding.rvMsg.smoothScrollToPosition(this)
            }
        }
    }
}