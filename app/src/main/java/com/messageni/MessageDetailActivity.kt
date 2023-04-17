package com.messageni

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MessageDetailActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_message_detail)

    val messageDetailTextView = findViewById<TextView>(R.id.message_detail)
    val dateTextView = findViewById<TextView>(R.id.date_text)
    val phoneNumberTextView = findViewById<TextView>(R.id.phone_number)

    val messageBody = intent.getStringExtra("message_body")
    val phoneNumber = intent.getStringExtra("phone_number")
    val date = intent.getLongExtra("date", 0L)

    // 전달받은 메시지 내용, 날짜, 전화번호 또는 이름을 표시합니다.
    messageDetailTextView.text = messageBody
    dateTextView.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(date))
    phoneNumberTextView.text = phoneNumber?.let { Utils.getContactNameFromPhoneNumber(this, it) } ?: phoneNumber
  }
}
