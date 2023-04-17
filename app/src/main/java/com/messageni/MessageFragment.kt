package com.messageni

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessageFragment : Fragment() {
  private val messageViewModel: MessageViewModel by activityViewModels()
  private lateinit var messagesAdapter: MessageListAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_message, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(context)

    messagesAdapter = MessageListAdapter(emptyList())
    recyclerView.adapter = messagesAdapter

    messageViewModel.currentTab.value = arguments?.getInt("tab_index") ?: 0

    messageViewModel.messages.observe(viewLifecycleOwner, Observer { _ ->
      messagesAdapter.updateMessages(messageViewModel.getMessagesForCurrentTab())
    })

    messageViewModel.isSpamTab.observe(viewLifecycleOwner, Observer { _ ->
      messagesAdapter.updateMessages(messageViewModel.getMessagesForCurrentTab())
    })
  }

  companion object {
    fun newInstance(tabIndex: Int): MessageFragment {
      val fragment = MessageFragment()
      val args = Bundle()
      args.putInt("tab_index", tabIndex)
      fragment.arguments = args
      return fragment
    }
  }
}
