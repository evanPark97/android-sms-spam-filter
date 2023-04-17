package com.messageni

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageListAdapter(
    private var messages: List<SmsMessageItem>,
    private val onItemClickListener: ((SmsMessageItem) -> Unit)? = null // 변경된 부분
) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {

    // ViewHolder 클래스 정의
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageBody: TextView = itemView.findViewById(R.id.message_body)
        val phoneNumber: TextView = itemView.findViewById(R.id.phone_number)
        val dateText: TextView = itemView.findViewById(R.id.date_text)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val messageItem = messages[position]
                    val context = itemView.context
                    val intent = Intent(context, MessageDetailActivity::class.java).apply {
                        putExtra("phone_number", messageItem.phoneNumber)
                        putExtra("date", messageItem.date)
                        putExtra("message_body", messageItem.messageBody)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageItem = messages[position]

        // 메시지의 최대 길이를 10자로 설정합니다.
        val maxLength = 10

        // 메시지가 50자보다 길면 끝에 "..."을 추가합니다.
        val messagePreview = if (messageItem.messageBody.length > maxLength) {
            messageItem.messageBody.substring(0, maxLength) + "..."
        } else {
            messageItem.messageBody
        }

        holder.messageBody.text = messagePreview

        // 저장된 전화번호의 이름을 가져옵니다.
        val contactName = Utils.getContactNameFromPhoneNumber(holder.itemView.context, messageItem.phoneNumber)
        holder.phoneNumber.text = contactName ?: messageItem.phoneNumber

        holder.dateText.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(messageItem.date))
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateMessages(newMessages: List<SmsMessageItem>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
