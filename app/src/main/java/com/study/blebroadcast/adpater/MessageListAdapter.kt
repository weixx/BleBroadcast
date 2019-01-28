package com.study.blebroadcast.adpater

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.study.blebroadcast.R
import com.study.blebroadcast.bean.MessageEntity
import java.text.SimpleDateFormat
import java.util.*

class MessageListAdapter (dataList: ArrayList<MessageEntity>, var mContext: Context) : BaseRecyclerViewAdapter<MessageEntity>(dataList) {
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun createHolder(holder: ViewHolder, d: MessageEntity) {
        val textView = holder.get<TextView>(R.id.tvMessage)
        d.time

        textView.text = "${SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(d.time)}  ${if(d.isSend) "广播发送：内容为\n" else "收到响应：内容为\n"} ${ d.content}"
    }
    override fun getLayoutId(): Int {
        return R.layout.item_message_list
    }


}