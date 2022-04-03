package com.example.task2client

import android.app.Service
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.ServiceConnection
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.TextView
import com.example.aidlserver.IOrientationService
import com.example.aidlserver.OrientationService

class MainActivity : AppCompatActivity() {
    private var orientationService: IOrientationService? = null
    val uiUpdateDelayMS = 1500L
    private lateinit var txtPitch: TextView
    private lateinit var txtRoll: TextView
    val handler = Handler()

    private val runnable: Runnable = object : Runnable {

        override fun run() {
            val orientation = orientationService?.orientation
            txtPitch.text = orientation?.get(0)?.toString()
            txtRoll.text = orientation?.get(1)?.toString()
            handler.postDelayed(this, uiUpdateDelayMS)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            orientationService = IOrientationService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            orientationService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtPitch = findViewById<TextView>(R.id.txtPitch)
        txtRoll = findViewById<TextView>(R.id.txtRoll)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, OrientationService::class.java)
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
        startUiUpdation()
    }

    override fun onPause() {
        super.onPause()
        unbindService(serviceConnection)
        stopUiUpdation()
    }

    fun startUiUpdation() {
        handler.postDelayed(runnable, uiUpdateDelayMS)
    }

    fun stopUiUpdation() {
        handler.removeCallbacks(runnable)
    }
}