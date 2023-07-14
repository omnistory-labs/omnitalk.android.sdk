package com.example.dempapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import io.omnitalk.sdk.Omnitalk


class MainActivity : AppCompatActivity() {

    companion object {
        val serviceId =
        val serviceKey =
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Omnitalk.initSdk(serviceId, serviceKey, applicationContext)

        val videoRoomBtn = findViewById<Button>(R.id.videoRoomBtn)
        val videoCallBtn = findViewById<Button>(R.id.videoCallBtn)
        val audioRoomBtn = findViewById<Button>(R.id.audioRoomBtn)
        val audioCallBtn = findViewById<Button>(R.id.audioCallBtn)

        videoRoomBtn.setOnClickListener {
            val intent = Intent(this, VideoRoomActivity::class.java)
            startActivity(intent)
        }
        videoCallBtn.setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            startActivity(intent)
        }
        audioRoomBtn.setOnClickListener {
            val intent = Intent(this, AudioRoomActivity::class.java)
            startActivity(intent)
        }
        audioCallBtn.setOnClickListener {
            val intent = Intent(this, AudioCallActivity::class.java)
            startActivity(intent)
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}

