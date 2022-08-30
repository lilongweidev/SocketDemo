package com.llw.socket.server

/**
 * 服务端回调
 */
interface ServerCallback {
    //接收客户端的消息
    fun receiveClientMsg(ipAddress: String, msg: String)
    //其他消息
    fun otherMsg(msg: String)
}