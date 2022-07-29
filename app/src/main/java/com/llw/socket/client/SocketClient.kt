package com.llw.socket.client

import android.util.Log
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

object SocketClient {

    private val TAG = SocketClient::class.java.simpleName

    private const val SOCKET_PORT = 9527
    private lateinit var socket: Socket

    private lateinit var outputStream: OutputStream
    private lateinit var inputStreamReader: InputStreamReader

    private lateinit var mCallback: ClientCallback


    fun connectServer(ipAddress: String, callback: ClientCallback) {
        mCallback = callback
        Thread {
            try {
                //通过socket连接服务器,参数ip为服务端ip地址，port为服务端监听端口
                socket = Socket(ipAddress, SOCKET_PORT)
                //获取输入流并转换为StreamReader，约定编码格式
                inputStreamReader = InputStreamReader(socket.getInputStream(), "utf-8")
                val inMessage = CharArray(1024)
                val length = inputStreamReader.read(inMessage) //a存储返回消息的长度
                val message = String(inMessage, 0, length) //用string的构造方法来转换字符数组为字符串
                mCallback.receiveServerMsg(message)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        inputStreamReader.close()
        outputStream.close()
        socket.close()
        Log.d(TAG, "关闭连接")
    }

    /**
     * 发送数据至服务器
     * @param msg 要发送至服务器的字符串
     */
    fun sendToServer(msg: String) {
        Thread {
            var message = msg
            if (socket.isClosed) {
                Log.e(TAG, "sendToServer: Socket is closed")
                return@Thread
            }
            outputStream = socket.getOutputStream()    //获取输出流
            try {
                val me = message.toByteArray()  //基本输出流只能输出字符数组，如果要直接输出字符串要使用OutputStreamWriter
                outputStream.write(me)
                outputStream.flush()    //输出完记得刷新一下
                mCallback.otherMsg("toServer: $msg")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "向服务端发送消息失败")
            }
        }.start()
    }
}