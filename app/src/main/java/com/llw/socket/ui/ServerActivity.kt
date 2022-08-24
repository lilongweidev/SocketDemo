package com.llw.socket.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.llw.socket.adapter.MsgAdapter
import com.llw.socket.bean.Message
import com.llw.socket.databinding.ActivityServerBinding
import com.llw.socket.server.ServerCallback
import com.llw.socket.server.SocketServer


/**
 * 服务端页面
 */
class ServerActivity : BaseActivity(), ServerCallback, EmojiCallback {

    private val TAG = ServerActivity::class.java.simpleName
    private lateinit var binding: ActivityServerBinding

    //Socket服务是否打开
    private var openSocket = false

    //消息列表
    private val messages = ArrayList<Message>()

    //消息适配器
    private lateinit var msgAdapter: MsgAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.toolbar.apply {
            subtitle = "IP：${getIp()}"
            setNavigationOnClickListener { onBackPressed() }
        }

        //显示emoji
        binding.ivEmoji.setOnClickListener {
            //显示底部弹窗
            showEmojiDialog(this,this)
        }

        //开启服务/关闭服务 服务端处理
        binding.tvStartService.setOnClickListener {
            openSocket = if (openSocket) {
                SocketServer.stopServer();false
            } else SocketServer.startServer(this)
            //显示日志
            showMsg(if (openSocket) "开启服务" else "关闭服务")
            //改变按钮文字
            binding.tvStartService.text = if (openSocket) "关闭服务" else "开启服务"
        }

        //发送消息给客户端
        binding.btnSendMsg.setOnClickListener {
            val msg = binding.etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (openSocket) openSocket else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            SocketServer.sendToClient(msg)
            binding.etMsg.setText("")
            updateList(1, msg)
        }
        //初始化列表
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@ServerActivity)
            adapter = msgAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 接收到客户端发的消息
     */
    override fun receiveClientMsg(success: Boolean, msg: String) = updateList(2, msg)

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

    override fun checkedEmoji(charSequence: CharSequence) {
        binding.etMsg.apply {
            setText(text.toString() + charSequence)
            setSelection(text.toString().length)//光标置于最后
        }
    }
}