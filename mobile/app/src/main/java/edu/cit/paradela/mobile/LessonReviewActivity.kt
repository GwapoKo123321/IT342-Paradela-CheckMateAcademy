package edu.cit.paradela.mobile

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LessonReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lesson_review)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<Button>(R.id.btnBackToDashboard)
        val btnFlip = findViewById<Button>(R.id.btnReviewFlip)
        val webView = findViewById<WebView>(R.id.reviewChessWebView)

        // Setup the WebView (God Mode for local assets)
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
            webView.evaluateJavascript("flipBoard();", null)
        }

        btnBack.setOnClickListener {
            finish() // Closes the review screen
        }
    }
}