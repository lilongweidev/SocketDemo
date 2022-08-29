package com.llw.socket.ui

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.llw.socket.R
import com.llw.socket.SocketApp
import com.llw.socket.adapter.EmojiAdapter
import com.llw.socket.adapter.MsgAdapter
import com.llw.socket.bean.Message
import com.llw.socket.client.ClientCallback
import com.llw.socket.client.SocketClient
import com.llw.socket.databinding.ActivityClientBinding
import com.llw.socket.databinding.DialogEditIpBinding


/**
 * 客户端页面
 */
class ClientActivity : BaseActivity(), ClientCallback, EmojiCallback {

    private val TAG = BaseActivity::class.java.simpleName
    private lateinit var binding: ActivityClientBinding

    //Socket服务是否连接
    private var connectSocket = false

    //消息列表
    private val messages = ArrayList<Message>()

    //消息适配器
    private lateinit var msgAdapter: MsgAdapter

    //是否显示表情
    private var isShowEmoji = false

    private var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        //初始化BottomSheet
        initBottomSheet()

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
        binding.layBottomSheetEdit.btnSendMsg.setOnClickListener {
            val msg = binding.layBottomSheetEdit.etMsg.text.toString().trim()
            if (msg.isEmpty()) {
                showMsg("请输入要发送的信息");return@setOnClickListener
            }
            //检查是否能发送消息
            val isSend = if (connectSocket) connectSocket else false
            if (!isSend) {
                showMsg("当前未开启服务或连接服务");return@setOnClickListener
            }
            SocketClient.sendToServer(msg)
            binding.layBottomSheetEdit.etMsg.setText("")
            updateList(2, msg)
        }
        //初始化列表
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@ClientActivity)
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
    }

    private fun showEditDialog() {
        val dialogBinding =
            DialogEditIpBinding.inflate(LayoutInflater.from(this@ClientActivity), null, false)
        AlertDialog.Builder(this@ClientActivity).apply {
            setIcon(R.drawable.ic_connect)
            setTitle("连接Ip地址")
            setView(dialogBinding.root)
            setPositiveButton("确定") { dialog, _ ->
                val ip = dialogBinding.etIpAddress.text.toString()
                if (ip.isEmpty()) {
                    showMsg("请输入Ip地址");return@setPositiveButton
                }
                connectSocket = true
                SocketClient.connectServer(ip, this@ClientActivity)
                showMsg("连接服务")
                binding.tvConnectService.text = "关闭连接"
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
        }.show()
    }

    /**
     * 接收到服务端发的消息
     */
    override fun receiveServerMsg(msg: String) = updateList(1, msg)

    override fun otherMsg(msg: String) {
        Log.d(TAG, "otherMsg: $msg")
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
        binding.layBottomSheetEdit.etMsg.apply {
            setText(text.toString() + charSequence)
            setSelection(text.toString().length)//光标置于最后
        }
    }
}