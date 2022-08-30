package com.llw.socket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.llw.socket.bean.Message
import com.llw.socket.databinding.ItemRvMsgBinding

/**
 * 消息适配器
 */
class MsgAdapter(private val messages: ArrayList<Message>) : RecyclerView.Adapter<MsgAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRvMsgBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        if (message.isMyself) {
            holder.mView.tvMyselfMsg.text = message.msg
        } else {
            holder.mView.tvOtherMsg.text = message.msg
        }

        holder.mView.layOther.visibility = if (message.isMyself) View.GONE else View.VISIBLE
        holder.mView.layMyself.visibility = if (message.isMyself) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = messages.size

    class ViewHolder(itemView: ItemRvMsgBinding) : RecyclerView.ViewHolder(itemView.root) {
        var mView: ItemRvMsgBinding

        init {
            mView = itemView
        }
    }
}