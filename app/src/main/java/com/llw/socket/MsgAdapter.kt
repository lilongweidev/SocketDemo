package com.llw.socket

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.llw.socket.databinding.ItemRvMsgBinding

class MsgAdapter(private val messages: ArrayList<Message>) :
    RecyclerView.Adapter<MsgAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRvMsgBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        if (message.type == 1) {
            holder.mView.tvServerMsg.text = message.msg
        } else {
            holder.mView.tvClientMsg.text = message.msg
        }

        holder.mView.ivServer.visibility = if (message.type == 1) View.VISIBLE else View.INVISIBLE
        holder.mView.ivClient.visibility = if (message.type == 1) View.INVISIBLE else View.VISIBLE
        holder.mView.tvServerMsg.visibility = if (message.type == 1) View.VISIBLE else View.GONE
        holder.mView.tvClientMsg.visibility = if (message.type == 1) View.GONE else View.VISIBLE
    }

    override fun getItemCount() = messages.size

    class ViewHolder(itemView: ItemRvMsgBinding) : RecyclerView.ViewHolder(itemView.root) {
        var mView: ItemRvMsgBinding

        init {
            mView = itemView
        }
    }
}