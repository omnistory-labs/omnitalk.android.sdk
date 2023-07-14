package com.example.dempapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
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
import org.webrtc.SurfaceViewRenderer
import java.lang.Exception

class VideoCallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_call)

        val backBtn = findViewById<Button>(R.id.backBtn)
        val localInputLayout = findViewById<TextInputLayout>(R.id.localInputLayout)
        val localInput = findViewById<TextInputEditText>(R.id.localInput)
        val remoteInput = findViewById<TextInputEditText>(R.id.remoteInput)
        val localBtn = findViewById<Button>(R.id.localBtn)
        val remoteBtn = findViewById<Button>(R.id.remoteBtn)
        val acceptBtn = findViewById<Button>(R.id.acceptBtn)
        val localView = findViewById<SurfaceViewRenderer>(R.id.localVideo)
        val remoteView = findViewById<SurfaceViewRenderer>(R.id.remoteVideo)

        val deviceControlLayout = findViewById<ConstraintLayout>(R.id.deviceControlLayout)
        val videoMuteBtn = findViewById<Button>(R.id.vMuteBtn)
        val videoUnmuteBtn = findViewById<Button>(R.id.vUnmuteBtn)
        val audioMuteBtn = findViewById<Button>(R.id.aMuteBtn)
        val audioUnmuteBtn = findViewById<Button>(R.id.aUnmuteBtn)

        val frontCameraBtn = findViewById<Button>(R.id.frontCamBtn)
        val backCameraBtn = findViewById<Button>(R.id.backCamBtn)
        val earAudioBtn = findViewById<Button>(R.id.earAudioBtn)
        val speakerAudioBtn = findViewById<Button>(R.id.speakerAudioBtn)

        val sdk = Omnitalk.getInstance()
        var mySession: String? = null

        sdk.setOnEventListener(object : OmniEventListener {
            override fun onEvent(eventName: OmniEvent, message: Any) {
                Log.d("videocall", "onEvent Message!!!")
                if (eventName == OmniEvent.RINGING_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        acceptBtn.visibility = View.VISIBLE
                    }
                } else if (eventName == OmniEvent.CONNECTED_EVENT) {

                } else if (eventName == OmniEvent.LEAVE_EVENT) {
                    sdk.leave(mySession)
                    finish()
                }
            }
            override fun onClose() {
                Log.d("videocall", "onClose Event!!!")
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
                    Log.e("videocall", "failed to create session, $err")
                }
            }
        }

        remoteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    withContext(Dispatchers.Main) {
                        localView.visibility = View.VISIBLE
                        remoteView.visibility = View.VISIBLE
                        deviceControlLayout.visibility = View.VISIBLE
                        sdk.offerCall(PublicTypes.CALL_TYPE.videocall, remoteInput.text.toString(), true, localView, remoteView)
                        localInputLayout.visibility = View.GONE
                        localBtn.visibility = View.GONE
                        remoteInput.visibility = View.GONE
                        remoteBtn.visibility = View.GONE
                    }
                } catch (err: Exception) {
                    Log.e("videocall", "failed to offer call, $err")
                }
            }
        }

        acceptBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    withContext(Dispatchers.Main) {
                        localView.visibility = View.VISIBLE
                        remoteView.visibility = View.VISIBLE
                        deviceControlLayout.visibility = View.VISIBLE
                        sdk.answerCall(localView, remoteView)
                        localInputLayout.visibility = View.GONE
                        localBtn.visibility = View.GONE
                        remoteInput.visibility = View.GONE
                        remoteBtn.visibility = View.GONE
                    }
                } catch (err: Exception) {
                    Log.e("videocall", "failed to offer call, $err")
                }
            }
        }

        videoMuteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.setMute(PublicTypes.TRACK_TYPE.video)
            }
        }
        videoUnmuteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.setUnmute(PublicTypes.TRACK_TYPE.video)
            }
        }
        audioMuteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.setMute(PublicTypes.TRACK_TYPE.audio)
            }
        }
        audioUnmuteBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.setUnmute(PublicTypes.TRACK_TYPE.audio)
            }
        }

        frontCameraBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.switchCameraDevice()
            }
        }
        backCameraBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.switchCameraDevice()
            }
        }
        earAudioBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.setAudioDevice(PublicTypes.MIC_TYPE.defaultInEar)
            }
        }
        speakerAudioBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                sdk.setAudioDevice(PublicTypes.MIC_TYPE.speaker)
            }
        }

    }
}

