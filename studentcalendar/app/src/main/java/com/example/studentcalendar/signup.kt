package com.example.studentcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        userPreferences = UserPreferences(this)

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                lifecycleScope.launch {
                    userPreferences.saveCredentials(email, password)
                    showSuccessAndNavigate()
                }
            }
        }

        findViewById<TextView>(R.id.tvLoginLink).setOnClickListener {
            navigateToLogin()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            findViewById<EditText>(R.id.etEmail).error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            findViewById<EditText>(R.id.etEmail).error = "Please enter a valid email"
            return false
        }
        if (password.isEmpty()) {
            findViewById<EditText>(R.id.etPassword).error = "Password is required"
            return false
        }
        if (password.length < 6) {
            findViewById<EditText>(R.id.etPassword).error = "Password must be at least 6 characters"
            return false
        }
        if (confirmPassword.isEmpty()) {
            findViewById<EditText>(R.id.etConfirmPassword).error = "Please confirm your password"
            return false
        }
        if (password != confirmPassword) {
            findViewById<EditText>(R.id.etConfirmPassword).error = "Passwords don't match"
            return false
        }
        return true
    }

    private fun showSuccessAndNavigate() {
        Toast.makeText(this, "Registration successful! Please login", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}