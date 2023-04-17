package com.messageni

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage

class SMSBroadcastReceiver : BroadcastReceiver() {
  var onSmsReceivedListener: ((List<SmsMessageItem>) -> Unit)? = null

  override fun onReceive(context: Context, intent: Intent) {
    if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
      val pdus = intent.getSerializableExtra("pdus") as Array<Any>?
      pdus?.let {
        val messages = mutableListOf<SmsMessageItem>()
        for (pdu in pdus) {
          val message = SmsMessage.createFromPdu(pdu as ByteArray)
          val messageBody = message.messageBody
          val phoneNumber = message.originatingAddress ?: ""
          val date = message.timestampMillis

          val isSpam = Utils.isSpamNumber(phoneNumber, context.getSharedPreferences("spam_numbers", MODE_PRIVATE)) || Utils.isMessageValid(
            messageBody
          )
          messages.add(SmsMessageItem(messageBody, phoneNumber, date, isSpam))
        }
        onSmsReceivedListener?.invoke(messages)
      }
    }
  }
}

