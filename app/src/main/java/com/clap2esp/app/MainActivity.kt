package com.clap2esp.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService

class MainActivity : AppCompatActivity() {

    private val microphonePermissionCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
       
        Logger.log("MainActivity created")

        checkMicrophonePermission()

        val startButton = findViewById<Button>(
            R.id.startButton
        )

        val stopButton = findViewById<Button>(
            R.id.stopButton
        )

        startButton.setOnClickListener {

            val serviceIntent = Intent(
                this,
                AudioService::class.java
            )

            startForegroundService(
                this,
                serviceIntent
            )
            
            Logger.log("Start button pressed")
            
        }

        stopButton.setOnClickListener {

            val serviceIntent = Intent(
                this,
                AudioService::class.java
            )

            stopService(serviceIntent)

            Logger.log("Stop button pressed")
        }
        
    }

    private fun checkMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        )
        
        Logger.log("Microphone permission granted")

        Logger.log("Requesting microphone permission")
        
        {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                microphonePermissionCode
            )
        }
    }
}
