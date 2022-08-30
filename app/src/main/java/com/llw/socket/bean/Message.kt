package com.llw.socket.bean

/**
 * 消息
 * @param isMyself 是否自身
 * @param msg 消息内容
 */
data class Message(val isMyself: Boolean, val msg: String)
