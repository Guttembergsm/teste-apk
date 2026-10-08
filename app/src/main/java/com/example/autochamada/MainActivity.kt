package com.example.autochamada

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var phoneInput: EditText
    private lateinit var intervalInput: EditText
    private lateinit var retriesInput: EditText
    private lateinit var speakerSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(CallService.EXTRA_STATUS) ?: return
            val attempt = intent.getIntExtra(CallService.EXTRA_ATTEMPT, 0)
            val max = intent.getIntExtra(CallService.EXTRA_MAX_ATTEMPTS, 0)
            statusText.text = if (max > 0) "Status: $status (Tentativa $attempt/$max)" else "Status: $status"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            gravity = Gravity.TOP
        }

        val titleView = TextView(this).apply {
            text = "AutoChamada"
            textSize = 24f
            setPadding(0, 0, 0, 24)
        }

        phoneInput = EditText(this).apply {
            hint = "Número de telefone (ex: 11999999999)"
            inputType = InputType.TYPE_CLASS_PHONE
            setText("")
        }

        intervalInput = EditText(this).apply {
            hint = "Intervalo entre chamadas (segundos)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("15")
        }

        retriesInput = EditText(this).apply {
            hint = "Máximo de tentativas"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("10")
        }

        speakerSwitch = Switch(this).apply {
            text = "Ativar viva-voz automaticamente"
            isChecked = true
            setPadding(0, 16, 0, 24)
        }

        statusText = TextView(this).apply {
            text = "Status: Aguardando início"
            textSize = 16f
            setPadding(0, 16, 0, 32)
        }

        startButton = Button(this).apply {
            text = "Iniciar AutoChamada"
            setOnClickListener { checkPermissionsAndStart() }
        }

        stopButton = Button(this).apply {
            text = "Parar Serviço"
            setOnClickListener { stopCallService() }
        }

        rootLayout.addView(titleView)
        rootLayout.addView(phoneInput)
        rootLayout.addView(intervalInput)
        rootLayout.addView(retriesInput)
        rootLayout.addView(speakerSwitch)
        rootLayout.addView(statusText)
        rootLayout.addView(startButton)
        rootLayout.addView(stopButton)

        setContentView(rootLayout)
    }

    private fun checkPermissionsAndStart() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
            return
        }

        startCallService()
    }

    private fun startCallService() {
        val phone = phoneInput.text.toString().trim()
        if (phone.isEmpty()) {
            Toast.makeText(this, "Informe um número válido", Toast.LENGTH_SHORT).show()
            return
        }

        val intervalSec = intervalInput.text.toString().toIntOrNull() ?: 15
        val maxRetries = retriesInput.text.toString().toIntOrNull() ?: 10

        val serviceIntent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTION_START
            putExtra(CallService.EXTRA_PHONE, phone)
            putExtra(CallService.EXTRA_INTERVAL_SEC, intervalSec)
            putExtra(CallService.EXTRA_MAX_ATTEMPTS, maxRetries)
            putExtra(CallService.EXTRA_SPEAKER, speakerSwitch.isChecked)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        statusText.text = "Status: Serviço iniciado para $phone"
    }

    private fun stopCallService() {
        val stopIntent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTION_STOP
        }
        startService(stopIntent)
        statusText.text = "Status: Ciclo interrompido pelo usuário"
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(CallService.ACTION_STATUS_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(statusReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(statusReceiver)
    }
}
