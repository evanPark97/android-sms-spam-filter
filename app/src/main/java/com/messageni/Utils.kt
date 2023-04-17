package com.messageni

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import java.util.*

object Utils {
  private val filterKeywords = listOf("(광고)", "대출")

  fun isMessageValid(messageBody: String): Boolean {
    return filterKeywords.any { keyword ->
      messageBody.contains(
        keyword,
        ignoreCase = true
      )
    }
  }

  fun getContactNameFromPhoneNumber(context: Context, phoneNumber: String): String? {
    val uri =
      Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    val contactNameColumnIndex =
      cursor?.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME) ?: -1

    return cursor?.use {
      if (it.moveToFirst()) {
        it.getString(contactNameColumnIndex)
      } else {
        null
      }
    }
  }

  fun addSpamNumber(phoneNumber: String, sharedPreferences: SharedPreferences) {
    val editor = sharedPreferences.edit()
    val spamNumbers =
      sharedPreferences.getStringSet("spam_numbers", mutableSetOf<String>())
        ?.toMutableSet() ?: mutableSetOf<String>()
    spamNumbers.add(phoneNumber)
    editor.putStringSet("spam_numbers", spamNumbers)
    editor.apply()
  }

  fun readReceivedMessages(
    contentResolver: ContentResolver,
    sharedPreferences: SharedPreferences,
    onMessagesLoaded: (List<SmsMessageItem>) -> Unit
  ) {
    val smsUri = Telephony.Sms.CONTENT_URI

    val cursor = contentResolver.query(smsUri, null, null, null, null)

    if (cursor != null) {
      val bodyColumnIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
      val addressColumnIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
      val dateColumnIndex = cursor.getColumnIndex(Telephony.Sms.DATE)

      val messages = mutableListOf<SmsMessageItem>()

      while (cursor.moveToNext()) {
        if (bodyColumnIndex >= 0 && addressColumnIndex >= 0 && dateColumnIndex >= 0) {
          val messageBody = cursor.getString(bodyColumnIndex)
          val phoneNumber = cursor.getString(addressColumnIndex)
          val date = cursor.getLong(dateColumnIndex)

          val isSpam =
            isSpamNumber(phoneNumber, sharedPreferences) || isMessageValid(
              messageBody
            )
          messages.add(SmsMessageItem(messageBody, phoneNumber, date, isSpam))
        }
      }
      cursor.close()
      onMessagesLoaded(messages)
    }
  }

  fun isSpamNumber(phoneNumber: String, sharedPreferences: SharedPreferences): Boolean {
    val spamNumbers =
      sharedPreferences.getStringSet("spam_numbers", setOf<String>()) ?: setOf<String>()
    return spamNumbers.contains(phoneNumber)
  }
}
