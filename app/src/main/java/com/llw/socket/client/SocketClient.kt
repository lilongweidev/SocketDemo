package com.llw.socket.client

import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Socket客户端
 */
object SocketClient {

    private val TAG = SocketClient::class.java.simpleName

    private var socket: Socket? = null

    private var outputStream: OutputStream? = null

    private var inputStreamReader: InputStreamReader? = null

    private lateinit var mCallback: ClientCallback

    private const val SOCKET_PORT = 9527

    // 客户端线程池
    private var clientThreadPool: ExecutorService? = null

    //心跳发送间隔
    private const val HEART_SPACETIME = 3 * 1000

    private val mHandler: Handler = Handler()

    /**
     * 连接服务
     */
    fun connectServer(ipAddress: String, callback: ClientCallback) {
        mCallback = callback
        Thread {
            try {
                socket = Socket(ipAddress, SOCKET_PORT)
                //开启心跳,每隔3秒钟发送一次心跳
                mHandler.post(mHeartRunnable)
                ClientThread(socket!!, mCallback).start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        inputStreamReader?.close()
        outputStream?.close()
        socket?.close()
        //关闭线程池
        clientThreadPool?.shutdownNow()
        clientThreadPool = null
    }

    /**
     * 发送数据至服务器
     * @param msg 要发送至服务器的字符串
     */
    fun sendToServer(msg: String) {
        if (clientThreadPool == null) {
            clientThreadPool = Executors.newSingleThreadExecutor()
        }
        clientThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接")
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭")
                return@execute
            }
            outputStream = socket?.getOutputStream()
            try {
                outputStream?.write(msg.toByteArray())
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向服务端发送消息: $msg 失败")
            }
        }
    }

    private val mHeartRunnable = Runnable { sendHeartbeat() }

    /**
     * 发送心跳消息
     */
    private fun sendHeartbeat() {
        if (clientThreadPool == null) {
            clientThreadPool = Executors.newSingleThreadExecutor()
        }
        val msg = "洞幺洞幺，呼叫洞拐，听到请回答，听到请回答，Over!"
        clientThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接")
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭")
                return@execute
            }
            outputStream = socket?.getOutputStream()
            try {
                outputStream?.write(msg.toByteArray())
                outputStream?.flush()
                //发送成功以后，重新建立一个心跳消息
                mHandler.postDelayed(mHeartRunnable, HEART_SPACETIME.toLong())
                Log.i(TAG, msg)
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向服务端发送消息: $msg 失败")
            }
        }
    }

    class ClientThread(private val socket: Socket, private val callback: ClientCallback) :
        Thread() {
        override fun run() {
            val inputStream: InputStream?
            try {
                inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var len: Int
                var receiveStr = ""
                if (inputStream.available() == 0) {
                    Log.e(TAG, "inputStream.available() == 0")
                }
                while (inputStream.read(buffer).also { len = it } != -1) {
                    receiveStr += String(buffer, 0, len, Charsets.UTF_8)
                    if (len < 1024) {
                        socket.inetAddress.hostAddress?.let {
                            if (receiveStr == "洞拐收到，洞拐收到，Over!") {//收到来自服务端的心跳回复消息
                                Log.i(TAG, "洞拐收到，洞拐收到，Over!")
                                //准备回复
                            } else {
                                callback.receiveServerMsg(it, receiveStr)
                            }
                        }
                        receiveStr = ""
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                when (e) {
                    is SocketTimeoutException -> {
                        Log.e(TAG, "连接超时，正在重连")
                    }
                    is NoRouteToHostException -> {
                        Log.e(TAG, "该地址不存在，请检查")
                    }
                    is ConnectException -> {
                        Log.e(TAG, "连接异常或被拒绝，请检查")
                    }
                    is SocketException -> {
                        when (e.message) {
                            "Already connected" -> Log.e(TAG, "连接异常或被拒绝，请检查")
                            "Socket closed" -> Log.e(TAG, "连接已关闭")
                        }
                    }
                }
            }
        }
    }

}