package com.example.autochamada

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.net.Uri
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.util.Log

class CallService : Service() {
    companion object {
        const val ACTION_START = "com.example.autochamada.START"
        const val ACTION_STOP = "com.example.autochamada.STOP"
        private const val TAG = "AutoChamada"
    }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var telephony: TelephonyManager
    private var number = ""
    private var running = false
    private var callWasActive = false
    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private val retry = Runnable { if (running) makeCall() }
    private val listener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state != TelephonyManager.CALL_STATE_IDLE) {
                callWasActive = true
                handler.removeCallbacks(retry)
            } else if (callWasActive && running) {
                callWasActive = false
                handler.removeCallbacks(retry)
                handler.postDelayed(retry, 3000)
            }
            lastState = state
        }
    }

    override fun onCreate() {
        super.onCreate()
        telephony = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: SecurityException) { Log.e(TAG, "Permissão de estado de chamada ausente", e) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                number = intent.getStringExtra("number") ?: ""
                running = true
                callWasActive = false
                handler.removeCallbacks(retry)
                makeCall()
            }
            ACTION_STOP -> {
                running = false
                handler.removeCallbacks(retry)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun makeCall() {
        if (!running || number.isBlank()) return
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            running = false; stopSelf(); return
        }
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(number)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Não foi possível iniciar a chamada", e)
            handler.removeCallbacks(retry)
            handler.postDelayed(retry, 3000)
        }
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(retry)
        try { telephony.listen(listener, PhoneStateListener.LISTEN_NONE) } catch (_: Exception) {}
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
