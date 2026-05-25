package edu.cit.paradela.mobile

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LiveSessionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_live_session)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup UI
        val btnLeave = findViewById<Button>(R.id.btnLeaveSession)
        val btnFlip = findViewById<Button>(R.id.btnSessionFlip)
        val tvOpponent = findViewById<TextView>(R.id.tvSessionOpponent)
        val webView = findViewById<WebView>(R.id.liveChessWebView)

        // Mock Data based on who logged in
        val role = intent.getStringExtra("ROLE") ?: "STUDENT"
        if (role == "COACH") {
            tvOpponent.text = "Coaching: Hikaru Nakamura"
        } else {
            tvOpponent.text = "Coach: Magnus Carlsen"
        }

        // Setup the WebView (God Mode)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            blockNetworkImage = false
            blockNetworkLoads = false
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        // ADD THESE TWO LINES TO STABILIZE THE VIEW ON PHYSICAL PHONES
        webView.webViewClient = android.webkit.WebViewClient()
        webView.webChromeClient = android.webkit.WebChromeClient()

        // Load the local HTML file
        webView.loadUrl("file:///android_asset/chessboard.html")

        // Handle Interactions
        btnFlip.setOnClickListener {
            // Send JS command to flip the board
            webView.evaluateJavascript("flipBoard();", null)
        }

        btnLeave.setOnClickListener {
            finish() // Closes the session and goes back to Dashboard
        }
    }
}