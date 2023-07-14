package com.example.dempapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.omnitalk.sdk.OmniEventListener
import io.omnitalk.sdk.Omnitalk
import io.omnitalk.sdk.types.OmniEvent
import io.omnitalk.sdk.types.PublicTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class AudioCallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_call)

        val backBtn = findViewById<Button>(R.id.backBtn)
        val localInputLayout = findViewById<TextInputLayout>(R.id.localInputLayout)
        val localInput = findViewById<TextInputEditText>(R.id.localInput)
        var remoteInputLayout = findViewById<TextInputLayout>(R.id.remoteInputLayout)
        val remoteInput = findViewById<TextInputEditText>(R.id.remoteInput)
        val localBtn = findViewById<Button>(R.id.localBtn)
        val remoteBtn = findViewById<Button>(R.id.remoteBtn)
        val acceptBtn = findViewById<Button>(R.id.acceptBtn)
        val callingText = findViewById<TextView>(R.id.callingView)

        val sdk = Omnitalk.getInstance()
        var mySession: String? = null

        // sdk 이벤트 리스너 등록
        sdk.setOnEventListener(object : OmniEventListener {
            override fun onEvent(eventName: OmniEvent, message: Any) {
                if (eventName == OmniEvent.RINGING_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        acceptBtn.visibility = View.VISIBLE
                    }
                } else if (eventName == OmniEvent.CONNECTED_EVENT) {
                    callingText.text = "통화중..."
                } else if (eventName == OmniEvent.LEAVE_EVENT) {
                    sdk.leave(mySession)
                    finish()
                }
            }
            override fun onClose() {
                Log.d("audiocall", "onClose Event!!!")
            }
        })

        backBtn.setOnClickListener {
            sdk.leave(mySession)
            finish()
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                sdk.leave(mySession)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        localBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    withContext(Dispatchers.Main) {
                        val sessionResult = sdk.createSession(localInput.text.toString())
                        mySession = sessionResult.session
                        localInputLayout.isEnabled = false
                        localBtn.isEnabled = false
                        remoteInput.isEnabled = true
                        remoteBtn.isEnabled = true
                    }
                } catch (err: Exception) {
                    Log.e("audiocall", "failed to create session, $err")
                }
            }
        }

        remoteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    withContext(Dispatchers.Main) {
                        sdk.offerCall(PublicTypes.CALL_TYPE.audiocall, remoteInput.text.toString(), true, null, null)
                        localInputLayout.visibility = View.GONE
                        remoteInputLayout.visibility = View.GONE
                        localBtn.visibility = View.GONE
                        remoteInput.visibility = View.GONE
                        remoteBtn.visibility = View.GONE
                        callingText.visibility = View.VISIBLE
                    }
                } catch (err: Exception) {
                    Log.e("audiocall", "failed to offer call, $err")
                }
            }
        }

        acceptBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    withContext(Dispatchers.Main) {
                        sdk.answerCall(null, null)
                        localInputLayout.visibility = View.GONE
                        remoteInputLayout.visibility = View.GONE
                        localBtn.visibility = View.GONE
                        remoteInput.visibility = View.GONE
                        remoteBtn.visibility = View.GONE
                        acceptBtn.visibility = View.GONE
                        callingText.visibility = View.VISIBLE
                    }
                } catch (err: Exception) {
                    Log.e("audiocall", "failed to offer call, $err")
                }
            }
        }
    }
}

