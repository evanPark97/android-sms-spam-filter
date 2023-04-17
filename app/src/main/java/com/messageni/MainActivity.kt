package com.messageni

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.messageni.Utils

// MainActivity 클래스는 앱의 메인 화면을 구성하고, 필요한 데이터 및 기능을 처리합니다.
class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1
    private lateinit var sharedPreferences: SharedPreferences
    private val smsBroadcastReceiver = SMSBroadcastReceiver()
    private val messageViewModel: MessageViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    // 액티비티가 생성될 때 호출되는 함수입니다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ViewPager2 및 TabLayout을 초기화합니다.
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        // ViewPager2에 어댑터를 설정합니다.
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return MessageFragment.newInstance(position)
            }
        }

        // TabLayout과 ViewPager2를 연결합니다.
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) {
                getString(R.string.tab_regular_messages)
            } else {
                getString(R.string.tab_spam_messages)
            }
        }.attach()

        // 페이지 변경 리스너를 등록합니다.
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                messageViewModel.currentTab.value = position
            }
        })

        // SMS 수신 리스너를 설정합니다.
        smsBroadcastReceiver.onSmsReceivedListener = { newMessages ->
            Log.i("newMessages", newMessages.toString())
            runOnUiThread {
                readAndDisplayMessages(newMessages)
            }
        }

        // 공유 환경설정 인스턴스를 가져옵니다.
        sharedPreferences = getSharedPreferences("spam_numbers", MODE_PRIVATE)

        // READ_SMS 및 READ_CONTACTS 권한을 요청하는 코드
        requestPermissionsIfNeeded()

        // 수신된 메시지를 읽고 화면에 표시합니다.
        readAndDisplayMessages()
    }

    private fun requestPermissionsIfNeeded() {
        val requiredPermissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_SMS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_CONTACTS)
        }

        if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    // 새로운 메시지가 수신되었을 때 메시지를 읽고 화면에 표시합니다.
    private fun readAndDisplayMessages(newMessages: List<SmsMessageItem> = emptyList()) {
        Utils.readReceivedMessages(contentResolver, sharedPreferences) { messages ->
            val allMessages = messages + newMessages
            messageViewModel.updateMessages(allMessages)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    readAndDisplayMessages()
                }
            }
        }
    }
}

