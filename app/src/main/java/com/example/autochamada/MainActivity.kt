package com.example.autochamada

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.text.InputType

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("config", MODE_PRIVATE) }
    private val blue = Color.rgb(22,119,242)
    private val dark = Color.rgb(7,20,38)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
        requestNeededPermissions()
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= 33) needed.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = needed.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 10)
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 24, 28, 24)
            setBackgroundColor(dark)
        }
    }

    private fun button(label: String, color: Int, action: () -> Unit): Button =
        Button(this).apply {
            text = label; setTextColor(Color.WHITE); setBackgroundTintList(android.content.res.ColorStateList.valueOf(color))
            textSize = 18f
            setOnClickListener { action() }
        }

    private fun showHome() {
        val root = baseLayout()
        val settings = TextView(this).apply {
            text = "⚙"; textSize = 30f; setTextColor(Color.WHITE); gravity = Gravity.END
            setOnClickListener { adminLogin() }
        }
        root.addView(settings, LinearLayout.LayoutParams(-1, 60))
        root.addView(TextView(this).apply {
            text = "AUTOCHAMADA"; textSize = 23f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, 70))
        root.addView(button("☎  CHAMAR", Color.rgb(13,166,91)) {
            val number = prefs.getString("number", "")?.trim().orEmpty()
            if (number.isBlank()) {
                Toast.makeText(this, "O administrador ainda não configurou o número.", Toast.LENGTH_LONG).show()
            } else if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestNeededPermissions()
                Toast.makeText(this, "Conceda as permissões solicitadas e tente novamente.", Toast.LENGTH_LONG).show()
            } else {
                startService(Intent(this, CallService::class.java).setAction(CallService.ACTION_START).putExtra("number", number))
                Toast.makeText(this, "Ciclo de chamadas iniciado.", Toast.LENGTH_SHORT).show()
            }
        }, LinearLayout.LayoutParams(-1, 72).apply { bottomMargin = 22 })
        root.addView(button("☎  DESLIGAR / PARAR", Color.rgb(220,45,65)) {
            startService(Intent(this, CallService::class.java).setAction(CallService.ACTION_STOP))
            Toast.makeText(this, "Novas tentativas canceladas.", Toast.LENGTH_SHORT).show()
        }, LinearLayout.LayoutParams(-1, 72))
        setContentView(root)
    }

    private fun adminLogin() {
        val input = EditText(this).apply { hint = "Senha do administrador"; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD }
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 8)
            addView(input)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Acesso administrativo")
            .setMessage("Digite a senha de administrador.")
            .setView(input)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Entrar") { _, _ ->
                val stored = prefs.getString("admin_password", "1234")
                if (input.text.toString() == stored) showAdmin()
                else Toast.makeText(this, "Senha incorreta.", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun showAdmin() {
        val number = EditText(this).apply {
            hint = "Número com DDD (ex.: 11987654321)"
            inputType = InputType.TYPE_CLASS_PHONE
            setText(prefs.getString("number", ""))
        }
        val password = EditText(this).apply {
            hint = "Nova senha (deixe vazio para manter)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 8)
            addView(TextView(this@MainActivity).apply { text = "Número de destino"; textSize = 16f })
            addView(number)
            addView(TextView(this@MainActivity).apply { text = "Alterar senha administrativa (opcional)"; textSize = 16f })
            addView(password)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Configurações do administrador")
            .setView(box)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar") { _, _ ->
                val n = number.text.toString().trim()
                if (n.isBlank()) Toast.makeText(this, "Informe um número válido.", Toast.LENGTH_LONG).show()
                else {
                    prefs.edit().putString("number", n).apply()
                    if (password.text.toString().isNotBlank()) prefs.edit().putString("admin_password", password.text.toString()).apply()
                    Toast.makeText(this, "Configurações salvas.", Toast.LENGTH_SHORT).show()
                }
            }.show()
    }
}
