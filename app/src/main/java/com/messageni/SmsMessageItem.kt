package com.messageni

data class SmsMessageItem(val messageBody: String, val phoneNumber: String, val date: Long, val isSpam: Boolean)
