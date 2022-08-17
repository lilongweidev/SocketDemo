package com.llw.socket.ui

import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.llw.socket.Message
import com.llw.socket.MsgAdapter
import com.llw.socket.R
import com.llw.socket.client.ClientCallback
import com.llw.socket.client.SocketClient
import com.llw.socket.databinding.ActivityMainBinding
import com.llw.socket.server.ServerCallback
import com.llw.socket.server.SocketServer

class MainActivity : AppCompatActivity(), ServerCallback, ClientCallback {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    private val buffer = StringBuffer()

    //当前是否为服务端
    private var isServer = true

    //Socket服务是否打开
    private var openSocket = false

    //Socket服务是否连接
    private var connectSocket = false

    //消息列表
    private val messages = ArrayList<Message>()
    //消息适配器
    private lateinit var msgAdapter: MsgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.tvIpAddress.text = "Ip地址：${getIp()}"
        //服务端和客户端切换
        binding.rg.setOnCheckedChangeListener { _, checkedId ->
            isServer = when (checkedId) {
                R.id.rb_server -> true
                R.id.rb_client -> false
                else -> true
            }
            binding.layServer.visibility = if (isServer) View.VISIBLE else View.GONE
            binding.layClient.visibility = if (isServer) View.GONE else View.VISIBLE
            binding.etMsg.hint = if (isServer) "发送给客户端" else "发送给服务端"
        }
        //开启服务/关闭服务 服务端处理
        binding.btnStartService.setOnClickListener {
            openSocket = if (openSocket) {
                SocketServer.stopServer();false
            } else SocketServer.startServer(this)
            //显示日志
            showMsg(if (openSocket) "开启服务" else "关闭服务")
            //改变按钮文字
            binding.btnStartService.text = if (openSocket) "关闭服务" else "开启服务"
        }
        //连接服务/断开连接 客户端处理
        binding.btnConnectService.setOnClickListener {
            val ip = binding.etIpAddress.text.toString()
            if (ip.isEmpty()) {
                showMsg("请输入Ip地址");return@setOnClickListener
            }
            connectSocket = if (connectSocket) {
                SocketClient.closeConnect();false
            } else {
                SocketClient.connectServer(ip, this);true
            }
            showMsg(if (connectSocket) "连接服务" else "关闭连接")
            binding.btnConnectService.text = if (connectSocket) "关闭连接" else "连接服务"
        }
        //发送消息 给 服务端/客户端
        binding.btnSendMsg.setOnClickListener {
            val msg = binding.etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (openSocket) openSocket  else if (connectSocket) connectSocket  else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            if (isServer) SocketServer.sendToClient(msg) else SocketClient.sendToServer(msg)
            binding.etMsg.setText("")
            updateList(if (isServer) 1 else 2, msg)
        }
        //初始化列表
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = msgAdapter
        }
    }

    private fun getIp() =
        intToIp((applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress)

    private fun intToIp(ip: Int) =
        "${(ip and 0xFF)}.${(ip shr 8 and 0xFF)}.${(ip shr 16 and 0xFF)}.${(ip shr 24 and 0xFF)}"

    /**
     * 接收到客户端发的消息
     */
    override fun receiveClientMsg(success: Boolean, msg: String) = updateList(2, msg)

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

    private fun showMsg(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}