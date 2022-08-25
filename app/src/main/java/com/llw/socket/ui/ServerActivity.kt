package com.llw.socket.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.llw.socket.R
import com.llw.socket.SocketApp
import com.llw.socket.adapter.EmojiAdapter
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

    //是否显示表情
    private var isShowEmoji = false

    //
    private var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

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
        //初始化BottomSheet
        initBottomSheet()
        //显示emoji
        binding.layBottomSheetEdit.ivEmoji.setOnClickListener {
            if (isShowEmoji) {
                isShowEmoji = false
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.layBottomSheetEdit.ivEmoji.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emoji))
            } else {
                isShowEmoji = true
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                binding.layBottomSheetEdit.ivEmoji.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emoji_checked))
            }
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
        binding.layBottomSheetEdit.btnSendMsg.setOnClickListener {
            val msg = binding.layBottomSheetEdit.etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (openSocket) openSocket else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            SocketServer.sendToClient(msg)
            binding.layBottomSheetEdit.etMsg.setText("")
            updateList(1, msg)
        }
        //初始化列表
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@ServerActivity)
            adapter = msgAdapter
        }
    }

    private fun initBottomSheet() {
        //Emoji布局
        bottomSheetBehavior =
            BottomSheetBehavior.from(binding.layBottomSheetEdit.bottomSheet).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
                isHideable = false
                isDraggable = false
            }
        binding.layBottomSheetEdit.rvEmoji.apply {
            layoutManager = GridLayoutManager(context, 6)
            adapter = EmojiAdapter(SocketApp.instance().emojiList).apply {
                setOnItemClickListener(object : EmojiAdapter.OnClickListener {
                    override fun onItemClick(position: Int) {
                        val charSequence = SocketApp.instance().emojiList[position]
                        checkedEmoji(charSequence)
                    }
                })
            }
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

    override fun otherMsg(msg: String) = showMsg(msg)

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
        binding.layBottomSheetEdit.etMsg.apply {
            setText(text.toString() + charSequence)
            setSelection(text.toString().length)//光标置于最后
        }
    }
}