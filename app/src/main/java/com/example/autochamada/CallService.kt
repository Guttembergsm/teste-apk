package com.example.autochamada

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class CallService : Service() {

    companion object {
        const val CHANNEL_ID = "autochamada_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_START = "com.example.autochamada.ACTION_START"
        const val ACTION_STOP = "com.example.autochamada.ACTION_STOP"
        const val ACTION_STATUS_UPDATE = "com.example.autochamada.ACTION_STATUS_UPDATE"

        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_INTERVAL_SEC = "extra_interval_sec"
        const val EXTRA_MAX_ATTEMPTS = "extra_max_attempts"
        const val EXTRA_SPEAKER = "extra_speaker"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_ATTEMPT = "extra_attempt"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var targetPhone: String = ""
    private var intervalSec: Int = 15
    private var maxAttempts: Int = 10
    private var useSpeaker: Boolean = true
    private var currentAttempt: Int = 0

    private val callRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            if (currentAttempt >= maxAttempts) {
                broadcastStatus("Concluído (limite atingido)", currentAttempt, maxAttempts)
                stopSelf()
                return
            }

            currentAttempt++
            placePhoneCall(targetPhone)
            broadcastStatus("Discando para $targetPhone", currentAttempt, maxAttempts)

            handler.postDelayed(this, (intervalSec + 45) * 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                targetPhone = intent.getStringExtra(EXTRA_PHONE) ?: ""
                intervalSec = intent.getIntExtra(EXTRA_INTERVAL_SEC, 15)
                maxAttempts = intent.getIntExtra(EXTRA_MAX_ATTEMPTS, 10)
                useSpeaker = intent.getBooleanExtra(EXTRA_SPEAKER, true)
                currentAttempt = 0
                isRunning = true

                startForeground(NOTIFICATION_ID, buildNotification("Discagem ativa para $targetPhone"))
                handler.removeCallbacks(callRunnable)
                handler.post(callRunnable)
            }
            ACTION_STOP -> {
                isRunning = false
                handler.removeCallbacks(callRunnable)
                broadcastStatus("Serviço parado", currentAttempt, maxAttempts)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun placePhoneCall(phone: String) {
        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(phone)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(callIntent)

            if (useSpeaker) {
                handler.postDelayed({
                    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.mode = AudioManager.MODE_IN_CALL
                    @Suppress("DEPRECATION")
                    audioManager.isSpeakerphoneOn = true
                }, 1500L)
            }
        } catch (e: SecurityException) {
            broadcastStatus("Erro de permissão CALL_PHONE", currentAttempt, maxAttempts)
        }
    }

    private fun broadcastStatus(status: String, attempt: Int, max: Int) {
        val update = Intent(ACTION_STATUS_UPDATE).apply {
            setPackage(packageName)
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_ATTEMPT, attempt)
            putExtra(EXTRA_MAX_ATTEMPTS, max)
        }
        sendBroadcast(update)
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoChamada")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Serviço AutoChamada",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacks(callRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
