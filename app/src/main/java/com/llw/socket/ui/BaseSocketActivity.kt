package com.llw.socket.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.llw.socket.client.ClientCallback
import com.llw.socket.client.SocketClient
import com.llw.socket.databinding.ActivityBaseSocketBinding
import com.llw.socket.server.ServerCallback
import com.llw.socket.server.SocketServer

open class BaseSocketActivity : BaseActivity(), ServerCallback, ClientCallback, EmojiCallback {

    lateinit var binding: ActivityBaseSocketBinding

    private val TAG = BaseSocketActivity::class.java.simpleName

    lateinit var etMsg: EditText
    lateinit var btnSendMsg: Button
    lateinit var ivMore: ImageView

    //Socket服务是否打开
    var openSocket = false

    //Socket服务是否连接
    var connectSocket = false

    //消息列表
    private val messages = ArrayList<Message>()

    //消息适配器
    private lateinit var msgAdapter: MsgAdapter

    //是否显示表情
    private var isShowEmoji = false

    private var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseSocketBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        etMsg = binding.layBottomSheetEdit.etMsg
        btnSendMsg = binding.layBottomSheetEdit.btnSendMsg
        ivMore = binding.layBottomSheetEdit.ivMore
        //初始化BottomSheet
        initBottomSheet()

        //输入监听
/*        etMsg.addTextChangedListener {
            if (it?.toString()?.isNotEmpty() == true) {
                btnSendMsg.visibility = View.VISIBLE
                ivMore.visibility = View.GONE
            } else {
                btnSendMsg.visibility = View.GONE
                ivMore.visibility = View.VISIBLE
            }
        }*/
        //初始化列表
        msgAdapter = MsgAdapter(messages)
        binding.rvMsg.apply {
            layoutManager = LinearLayoutManager(this@BaseSocketActivity)
            adapter = msgAdapter
        }
    }

    /**
     * 设置服务端页面标题
     */
    fun setServerTitle(startService: View.OnClickListener) =
        setTitle("服务端", "IP：${getIp()}", "开启服务", startService)

    /**
     * 设置客户端页面标题
     */
    fun setClientTitle(connectService: View.OnClickListener) =
        setTitle(mTitle = "客户端", funcTitle = "连接服务", click = connectService)

    /**
     * 设置标题
     */
    private fun setTitle(
        mTitle: String, mSubtitle: String = "", funcTitle: String,
        click: View.OnClickListener
    ) {
        binding.toolbar.apply {
            title = mTitle
            subtitle = mSubtitle
            setNavigationOnClickListener { onBackPressed() }
        }
        binding.tvFunc.text = funcTitle
        binding.tvFunc.setOnClickListener(click)
    }

    /**
     * 开启服务
     */
    fun startServer() {
        openSocket = true
        SocketServer.startServer(this)
        showMsg("开启服务")
        binding.tvFunc.text = "关闭服务"
    }

    /**
     * 停止服务
     */
    fun stopServer() {
        openSocket = false
        SocketServer.stopServer()
        showMsg("关闭服务")
        binding.tvFunc.text = "开启服务"
    }

    /**
     * 连接服务
     */
    fun connectServer(ipAddress: String) {
        connectSocket = true
        SocketClient.connectServer(ipAddress, this)
        showMsg("连接服务")
        binding.tvFunc.text = "关闭连接"
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        connectSocket = false
        SocketClient.closeConnect()
        showMsg("关闭连接")
        binding.tvFunc.text = "连接服务"
    }

    /**
     * 发送到客户端
     */
    fun sendToClient(msg: String) {
        SocketServer.sendToClient(msg)
        etMsg.setText("")
        updateList(true, msg)
    }

    /**
     * 发送到服务端
     */
    fun sendToServer(msg: String) {
        SocketClient.sendToServer(msg)
        etMsg.setText("")
        updateList(true, msg)
    }

    /**
     * 初始化BottomSheet
     */
    private fun initBottomSheet() {
        //Emoji布局
        bottomSheetBehavior =
            BottomSheetBehavior.from(binding.layBottomSheetEdit.bottomSheet).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
                isHideable = false
                isDraggable = false
            }
        //表情列表适配器
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
            bottomSheetBehavior!!.state =
                if (isShowEmoji) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
        }
        //BottomSheet显示隐藏的相关处理
        bottomSheetBehavior!!.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                isShowEmoji = when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {//显示
                        binding.layBottomSheetEdit.ivEmoji.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@BaseSocketActivity,
                                R.drawable.ic_emoji_checked
                            )
                        )
                        true
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {//隐藏
                        binding.layBottomSheetEdit.ivEmoji.setImageDrawable(
                            ContextCompat.getDrawable(this@BaseSocketActivity, R.drawable.ic_emoji)
                        )
                        false
                    }
                    else -> false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    /**
     * 更新列表
     */
    private fun updateList(isMyself: Boolean, msg: String) {
        messages.add(Message(isMyself, msg))
        runOnUiThread {
            (if (messages.size == 0) 0 else messages.size - 1).apply {
                msgAdapter.notifyItemChanged(this)
                binding.rvMsg.smoothScrollToPosition(this)
            }
        }
    }

    /**
     * 接收客户端消息
     */
    override fun receiveClientMsg(ipAddress: String, msg: String) = updateList(false, msg)

    /**
     * 接收服务端消息
     */
    override fun receiveServerMsg(ipAddress: String, msg: String) = updateList(false, msg)

    /**
     * 其他消息
     */
    override fun otherMsg(msg: String) {
        Log.d(TAG, "otherMsg: $msg")
    }

    /**
     * 选择表情
     */
    override fun checkedEmoji(charSequence: CharSequence) {
        etMsg.apply {
            setText(text.toString() + charSequence)
            setSelection(text.toString().length)//光标置于最后
        }
    }

}

