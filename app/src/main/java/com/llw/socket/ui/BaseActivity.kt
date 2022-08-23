package com.llw.socket.ui

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.llw.socket.SocketApp
import com.llw.socket.adapter.EmojiAdapter
import com.llw.socket.databinding.DialogEmojiBinding

open class BaseActivity: AppCompatActivity() {

    protected fun getIp() =
        intToIp((applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress)

    private fun intToIp(ip: Int) =
        "${(ip and 0xFF)}.${(ip shr 8 and 0xFF)}.${(ip shr 16 and 0xFF)}.${(ip shr 24 and 0xFF)}"

    protected fun showMsg(msg: CharSequence?) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()



    protected open fun jumpActivity(clazz: Class<*>?) = startActivity(Intent(this, clazz))

    protected fun showEmojiDialog(context: Context, callback: EmojiCallback) {
        val emojiBinding = DialogEmojiBinding.inflate(LayoutInflater.from(context), null, false)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(emojiBinding.root)
        emojiBinding.rvEmoji.apply {
            layoutManager = GridLayoutManager(context, 6)
            adapter = EmojiAdapter(SocketApp.instance().emojiList).apply {
                setOnItemClickListener(object : EmojiAdapter.OnClickListener {
                    override fun onItemClick(position: Int) {
                        val charSequence = SocketApp.instance().emojiList[position]
                        callback.checkedEmoji(charSequence)
                        dialog.dismiss()
                    }
                })
            }
        }
        dialog.show()
    }
}