package edu.cit.paradela.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.cit.paradela.mobile.models.LoginRequest
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSubmitLogin = findViewById<Button>(R.id.btnSubmitLogin)

        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnSubmitLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmitLogin.isEnabled = false
            btnSubmitLogin.text = "Waking Server... Please wait"

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val request = LoginRequest(email, password)
                    val response = RetrofitClient.authService.loginUser(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val authResponse = response.body()!!

                            if (authResponse.success) {
                                Toast.makeText(this@LoginActivity, "Welcome back!", Toast.LENGTH_SHORT).show()

                                val intent = if (authResponse.role == "Coach") {
                                    Intent(this@LoginActivity, CoachDashboardActivity::class.java)
                                } else {
                                    Intent(this@LoginActivity, StudentDashboardActivity::class.java)
                                }

                                intent.putExtra("USER_ID", authResponse.userId)
                                intent.putExtra("TOKEN", authResponse.token)
                                authResponse.data?.user?.let { user ->
                                    intent.putExtra("NAME", user.fullName)
                                    intent.putExtra("CHESS_USERNAME", user.chessUsername)
                                    intent.putExtra("CURRENT_ELO", user.currentElo ?: 0)
                                }

                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, authResponse.message, Toast.LENGTH_LONG).show()
                                resetLoginButton(btnSubmitLogin)
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Invalid Credentials", Toast.LENGTH_LONG).show()
                            resetLoginButton(btnSubmitLogin)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Server offline. Try again.", Toast.LENGTH_LONG).show()
                        resetLoginButton(btnSubmitLogin)
                    }
                }
            }
        }
    }

    private fun resetLoginButton(button: Button) {
        button.isEnabled = true
        button.text = "Login"
    }
}
