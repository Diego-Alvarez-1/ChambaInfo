package com.chambainfo.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chambainfo.app.databinding.ActivityTermsBinding
import com.chambainfo.app.ui.MainActivity

class TermsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding
    private var fromRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fromRegister = intent.getBooleanExtra("FROM_REGISTER", false)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            if (fromRegister) {
                // Si viene del registro, ir a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                finish()
            }
        }

        binding.btnAceptar.setOnClickListener {
            if (fromRegister) {
                // Aceptar términos y ir a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }
    }
}