package edu.cit.paradela.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment

class TrainingBoardFragment : Fragment() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the exact XML you just verified
        val view = inflater.inflate(R.layout.fragment_training_board, container, false)

        // Bind the WebView using the ID from your XML
        val webView = view.findViewById<WebView>(R.id.chessWebView)

        // Safety check to ensure it found the view before applying settings
        // Safety check to ensure it found the view before applying settings
        // Safety check to ensure it found the view before applying settings
        if (webView != null) {
            webView.settings.apply {
                // 1. Basic Setup
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true

                // 2. Unblock all Network Traffic & Mixed Content
                blockNetworkImage = false
                blockNetworkLoads = false
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // 3. THE FIX FOR PHYSICAL DEVICES (Bypass CORS for local files)
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
            }

            // Clean up scrollbars
            webView.isVerticalScrollBarEnabled = false
            webView.isHorizontalScrollBarEnabled = false

            // ADD THESE TWO LINES TO STABILIZE THE VIEW ON PHYSICAL PHONES
            webView.webViewClient = android.webkit.WebViewClient()
            webView.webChromeClient = android.webkit.WebChromeClient()

            // Load the local HTML file
            webView.loadUrl("file:///android_asset/chessboard.html")

            // Load the local HTML file
            webView.loadUrl("file:///android_asset/chessboard.html")
        }

        return view
    }
}