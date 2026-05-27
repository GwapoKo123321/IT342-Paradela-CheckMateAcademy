package edu.cit.paradela.mobile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.cit.paradela.mobile.models.RegisterRequest
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private var selectedRole = "STUDENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnToggleStudent = findViewById<Button>(R.id.btnToggleStudent)
        val btnToggleCoach = findViewById<Button>(R.id.btnToggleCoach)
        val etRegEmail = findViewById<EditText>(R.id.etRegEmail)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val etRegFullName = findViewById<EditText>(R.id.etRegFullName)
        val etRegElo = findViewById<EditText>(R.id.etRegElo)
        val etRegHandle = findViewById<EditText>(R.id.etRegHandle)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val btnSubmitRegister = findViewById<Button>(R.id.btnSubmitRegister)

        val colorGold = ContextCompat.getColor(this, R.color.accent_gold)
        val colorWhite = Color.WHITE

        btnToggleStudent.setOnClickListener {
            selectedRole = "STUDENT"

            btnToggleStudent.backgroundTintList = ColorStateList.valueOf(colorGold)
            btnToggleCoach.backgroundTintList = ColorStateList.valueOf(colorWhite)

            etRegElo.visibility = View.GONE
            etRegHandle.visibility = View.GONE
        }

        btnToggleCoach.setOnClickListener {
            selectedRole = "Coach"

            btnToggleCoach.backgroundTintList = ColorStateList.valueOf(colorGold)
            btnToggleStudent.backgroundTintList = ColorStateList.valueOf(colorWhite)

            etRegElo.visibility = View.VISIBLE
            etRegHandle.visibility = View.VISIBLE
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }

        btnSubmitRegister.setOnClickListener {
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString().trim()
            val fullName = etRegFullName.text.toString().trim()

            var eloRating: Int? = null
            var chessHandle: String? = null

            if (selectedRole == "Coach") {
                val eloString = etRegElo.text.toString().trim()
                if (eloString.isNotEmpty()) {
                    eloRating = eloString.toIntOrNull()
                }
                chessHandle = etRegHandle.text.toString().trim()
            }

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmitRegister.isEnabled = false
            btnSubmitRegister.text = "Creating Account..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val request = RegisterRequest(
                        email = email,
                        password = password,
                        fullName = fullName,
                        role = selectedRole,
                        currentElo = eloRating,
                        chessUsername = chessHandle
                    )

                    val response = RetrofitClient.authService.registerUser(request)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val authResponse = response.body()!!

                            if (authResponse.success) {
                                Toast.makeText(this@RegisterActivity, "Account Created!", Toast.LENGTH_SHORT).show()

                                val intent = if (authResponse.role == "Coach") {
                                    Intent(this@RegisterActivity, CoachDashboardActivity::class.java)
                                } else {
                                    Intent(this@RegisterActivity, StudentDashboardActivity::class.java)
                                }

                                intent.putExtra("USER_ID", authResponse.userId)
                                intent.putExtra("TOKEN", authResponse.token)
                                intent.putExtra("NAME", fullName)
                                intent.putExtra("CHESS_USERNAME", chessHandle)
                                intent.putExtra("CURRENT_ELO", eloRating ?: 0)

                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@RegisterActivity, authResponse.message, Toast.LENGTH_LONG).show()
                                resetBtn(btnSubmitRegister)
                            }
                        } else {
                            Toast.makeText(this@RegisterActivity, "Registration Failed. Email may be taken.", Toast.LENGTH_LONG).show()
                            resetBtn(btnSubmitRegister)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Server offline. Try again.", Toast.LENGTH_LONG).show()
                        resetBtn(btnSubmitRegister)
                    }
                }
            }
        }
    }

    private fun resetBtn(btn: Button) {
        btn.isEnabled = true
        btn.text = "Register"
    }
}
