package com.llw.socket.server

import android.util.Log
import com.llw.socket.client.SocketClient
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SocketServer {

    private val TAG = SocketServer::class.java.simpleName

    private const val SOCKET_PORT = 9527

    private lateinit var socket: Socket

    var result = true

    private lateinit var mCallback: ServerCallback

    fun startServer(callback: ServerCallback): Boolean {
        mCallback = callback
        Thread {
            try {
                //创建一个ServerSocket，用于监听客户端Socket的连接请求
                val serverSocket = ServerSocket(SOCKET_PORT)
                while (result) {
                    //每当接收到客户端的Socket请求，服务器端也相应的创建一个Socket
                    socket = serverSocket.accept()
                    mCallback.otherMsg("${socket.inetAddress} to connected")
                    ServerThread(socket, mCallback).start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                result = false
            }
        }.start()
        return result
    }

    fun stopServer() {
        socket.shutdownInput()
        socket.shutdownOutput()
        socket.close()
    }

    private lateinit var outputStream: OutputStream

    /**
     * 发送到客户端
     */
    fun sendToClient(msg: String) {
        Thread {
            var message = msg
            if (socket.isClosed) {
                Log.e(TAG, "sendToClient: Socket is closed")
                return@Thread
            }
            outputStream = socket.getOutputStream()
            try {
                val me = message.toByteArray()  //基本输出流只能输出字符数组，如果要直接输出字符串要使用OutputStreamWriter
                outputStream.write(me)
                outputStream.flush()    //输出完记得刷新一下
                mCallback.otherMsg("toClient: $msg")
                Log.d(TAG, "发送到客户端成功")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "向客户端发送消息失败")
            }
        }.start()
    }

    class ServerThread(private val socket: Socket, private val callback: ServerCallback) :
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
                        callback.receiveClientMsg(true, receiveStr)
                        receiveStr = ""
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                e.message?.let { Log.e("socket error", it) }
                callback.receiveClientMsg(false, "")
            }
        }
    }
}