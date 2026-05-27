package edu.cit.paradela.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LandingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.btn_login_hero)
        val btnRegister = findViewById<Button>(R.id.btn_register_hero)

        btnLogin.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
