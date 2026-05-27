package edu.cit.paradela.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment

class TrainingBoardFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var tvTurnIndicator: TextView
    private lateinit var tvMoveHistory: TextView
    private lateinit var btnFastRewind: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var btnFastForward: Button
    private lateinit var btnFlip: ImageButton
    private lateinit var btnClear: Button

    private val moveHistory = mutableListOf<String>()
    private var viewIndex = -1

    companion object {
        private val MOVENUM_RE   = Regex("^\\d+\\.+$")
        private val RESULT_TOK   = Regex("^(1-0|0-1|1/2-1/2|\\*)$")
        private val WHITESPACE_RE = Regex("\\s+")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_training_board, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView         = view.findViewById(R.id.chessWebView)
        tvTurnIndicator = view.findViewById(R.id.tvTurnIndicator)
        tvMoveHistory   = view.findViewById(R.id.tvMoveHistoryPlaceholder)
        btnFastRewind   = view.findViewById(R.id.btnFastRewind)
        btnPrev         = view.findViewById(R.id.btnPrevMove)
        btnNext         = view.findViewById(R.id.btnNextMove)
        btnFastForward  = view.findViewById(R.id.btnFastForward)
        btnFlip         = view.findViewById(R.id.btnFlipBoard)
        btnClear        = view.findViewById(R.id.btnClearLine)

        setupWebView()
        wireButtons()
        updateUi()
    }

    @Suppress("DEPRECATION")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled  = true
            domStorageEnabled  = true
            loadWithOverviewMode = true
            useWideViewPort    = true
            blockNetworkImage  = false
            blockNetworkLoads  = false
            mixedContentMode   = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess    = true
            allowContentAccess = true
            allowFileAccessFromFileURLs      = true
            allowUniversalAccessFromFileURLs = true
        }
        webView.isVerticalScrollBarEnabled   = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient   = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(TrainingBridge(this), "AndroidBridge")

        webView.loadUrl("file:///android_asset/chessboard.html")
    }

    private fun wireButtons() {
        btnFastRewind .setOnClickListener { navigateTo(-1) }
        btnPrev       .setOnClickListener { navigateTo(viewIndex - 1) }
        btnNext       .setOnClickListener { navigateTo(viewIndex + 1) }
        btnFastForward.setOnClickListener { navigateTo(moveHistory.size - 1) }
        btnFlip       .setOnClickListener { js("flipBoard();") }
        btnClear      .setOnClickListener { clearBoard() }
    }

    fun onLocalMove(newFen: String, newPgn: String) {

        rebuildHistory(newPgn)
        viewIndex = moveHistory.size - 1
        updateUi()
    }

    private fun navigateTo(index: Int) {
        val max = (moveHistory.size - 1).coerceAtLeast(-1)
        viewIndex = index.coerceIn(-1, max)
        js("goToMove($viewIndex);")
        updateUi()
    }

    private fun clearBoard() {
        moveHistory.clear()
        viewIndex = -1
        js("resetBoard();")
        updateUi()
    }

    private fun rebuildHistory(pgn: String) {
        moveHistory.clear()
        if (pgn.isBlank()) return

        val movesSection = pgn.lines()
            .filter { !it.trimStart().startsWith("[") }
            .joinToString(" ")
            .replace(Regex("[{][^}]*[}]"), " ")
            .replace(Regex("[(][^)]*[)]"), " ")
            .replace(Regex("\\s*(1-0|0-1|1/2-1/2|\\*)\\s*$"), "")
            .trim()

        for (token in movesSection.split(WHITESPACE_RE)) {
            val t = token.trim()
            if (t.isEmpty() || t.matches(MOVENUM_RE) || t.matches(RESULT_TOK)) continue
            moveHistory.add(t)
        }
    }

    private fun updateUi() {
        updateMoveHistoryText()
        updateNavButtons()
        updateTurnLabel()
    }

    private fun updateMoveHistoryText() {
        if (moveHistory.isEmpty()) {
            tvMoveHistory.text = "Make a move to start recording..."
            return
        }
        val sb = StringBuilder()
        var moveNum = 1
        for (i in moveHistory.indices) {
            if (i % 2 == 0) sb.append("$moveNum. ")
            sb.append(if (i == viewIndex) "▶${moveHistory[i]}" else moveHistory[i])
            if (i % 2 == 1) { sb.append("\n"); moveNum++ } else sb.append("  ")
        }
        tvMoveHistory.text = sb.toString().trimEnd()
    }

    private fun updateNavButtons() {
        val atStart = viewIndex <= -1
        val atEnd   = viewIndex >= moveHistory.size - 1
        btnFastRewind .isEnabled = !atStart; btnFastRewind .alpha = if (atStart) 0.35f else 1f
        btnPrev       .isEnabled = !atStart; btnPrev       .alpha = if (atStart) 0.35f else 1f
        btnNext       .isEnabled = !atEnd;   btnNext       .alpha = if (atEnd)   0.35f else 1f
        btnFastForward.isEnabled = !atEnd;   btnFastForward.alpha = if (atEnd)   0.35f else 1f
    }

    private fun updateTurnLabel() {
        tvTurnIndicator.text = when {
            viewIndex < 0      -> "⚪ White to move"
            viewIndex % 2 == 0 -> "⚫ Black to move"
            else               -> "⚪ White to move"
        }
    }

    private fun js(script: String) = webView.evaluateJavascript(script, null)

    class TrainingBridge(fragment: TrainingBoardFragment) {
        private val ref = java.lang.ref.WeakReference(fragment)

        @JavascriptInterface
        fun onPlayerMove(newFen: String, newPgn: String, san: String, turn: String) {
            val frag = ref.get() ?: return
            frag.requireActivity().runOnUiThread {
                frag.onLocalMove(newFen, newPgn)
            }
        }
    }
}
