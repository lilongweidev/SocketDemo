package com.llw.socket.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.emoji2.text.EmojiCompat
import androidx.recyclerview.widget.RecyclerView
import com.llw.socket.databinding.ItemEmojiBinding

/**
 * Emoji表情适配器
 */
class EmojiAdapter(private val emojis: ArrayList<CharSequence>) :
    RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemEmojiBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emoji = emojis[position]
        holder.mView.tvEmoji.apply {
            text = EmojiCompat.get().process(emoji)
            setOnClickListener { clickListener?.onItemClick(position) }
        }
    }

    override fun getItemCount() = emojis.size

    class ViewHolder(itemView: ItemEmojiBinding) : RecyclerView.ViewHolder(itemView.root) {
        var mView: ItemEmojiBinding

        init {
            mView = itemView
        }
    }


    interface OnClickListener {
        fun onItemClick(position: Int)
    }

    private var clickListener: OnClickListener? = null

    fun setOnItemClickListener(listener: OnClickListener) {
        clickListener = listener
    }

}