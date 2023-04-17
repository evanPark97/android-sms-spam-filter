package com.messageni

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageViewModel : ViewModel() {
  val messages = MutableLiveData<List<SmsMessageItem>>()
  val isSpamTab = MutableLiveData<Boolean>()
  val currentTab = MutableLiveData<Int>()

  init {
    currentTab.observeForever { position ->
      isSpamTab.value = position == 1
    }
  }

  fun updateMessages(messages: List<SmsMessageItem>) {
    this.messages.postValue(messages)
  }

  fun getMessagesForCurrentTab(): List<SmsMessageItem> {
    val isSpam = isSpamTab.value ?: false
    return messages.value?.filter { it.isSpam == isSpam } ?: emptyList()
  }
}
