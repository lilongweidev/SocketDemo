package com.llw.socket.client

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.Executors

class SocketClientService : Service() {


    override fun onBind(intent: Intent?) =
        ClientBinder(intent?.getStringExtra("ipAddress").toString())

    class ClientBinder(val ipAddress: String) : Binder() {

        private val TAG = ClientBinder::class.java.simpleName

        private val SOCKET_PORT = 9527

        private var socket: Socket? = null

        private var outputStream: OutputStream? = null

        private var inputStreamReader: InputStreamReader? = null

        private lateinit var mCallback: ClientCallback

        // 服务端线程池
        val clientThreadPool = Executors.newCachedThreadPool()

        /**
         * 连接服务
         */
        fun connectServer(callback: ClientCallback) {
            mCallback = callback
            clientThreadPool.execute {
                Runnable {
                    socket = Socket(ipAddress, SOCKET_PORT)
                    val inputStream: InputStream?
                    try {
                        inputStream = socket?.getInputStream()
                        val buffer = ByteArray(1024)
                        var len: Int
                        var receiveStr = ""
                        if (inputStream?.available() == 0) {
                            Log.e(TAG, "inputStream.available() == 0")
                        }
                        while (inputStream!!.read(buffer).also { len = it } != -1) {
                            receiveStr += String(buffer, 0, len, Charsets.UTF_8)
                            if (len < 1024) {
                                callback.receiveServerMsg(receiveStr)
                                receiveStr = ""
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        e.message?.let { Log.e("socket error", it) }
                        callback.receiveServerMsg( "")
                    }
                }
            }
        }

        /**
         * 关闭连接
         */
        fun closeConnect() {
            inputStreamReader?.close()
            outputStream?.close()
            socket?.close()
            Log.d(TAG, "关闭连接")
        }

        /**
         * 发送数据至服务器
         * @param msg 要发送至服务器的字符串
         */
        fun sendToServer(msg: String) {
            clientThreadPool.execute {
                Runnable {
                    if (socket!!.isClosed) {
                        Log.e(TAG, "sendToServer: Socket is closed")
                        return@Runnable
                    }
                    outputStream = socket?.getOutputStream()
                    try {
                        outputStream?.write(msg.toByteArray())
                        outputStream?.flush()
                        mCallback.otherMsg("toServer: $msg")
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e(TAG, "向服务端发送消息失败")
                    }
                }
            }
        }
    }
}