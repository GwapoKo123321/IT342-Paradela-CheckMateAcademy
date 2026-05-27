package edu.cit.paradela.mobile

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.cit.paradela.mobile.network.BoardUpdateRequest
import edu.cit.paradela.mobile.network.NotesUpdateRequest
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LiveSessionActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView
    private lateinit var etChatInput: EditText
    private lateinit var tvSessionTurn: TextView
    private lateinit var tvMoveHistory: TextView
    private lateinit var btnSendChat: Button
    private lateinit var btnNavStart: Button
    private lateinit var btnNavPrev: Button
    private lateinit var btnNavNext: Button
    private lateinit var btnNavEnd: Button

    private var lessonId = ""
    private var myRole   = "STUDENT"
    private var myName   = "User"

    private val moveHistory = mutableListOf<String>()

    private var viewIndex = -1

    @Volatile private var canonicalPgn = ""

    @Volatile private var serverFen = ""

    @Volatile private var currentNotes = ""

    @Volatile private var lastLocalMoveMs = 0L

    private var isCompleted = false

    companion object {
        private val HEADER_RE    = Regex("\\[[^\\]]*\\]\\s*")
        private val COMMENT_RE   = Regex("[{][^}]*[}]")
        private val PAREN_RE     = Regex("[(][^)]*[)]")
        private val RESULT_RE    = Regex("\\s*(1-0|0-1|1/2-1/2|\\*)\\s*$")
        private val MOVENUM_RE   = Regex("^\\d+\\.+$")
        private val RESULT_TOK   = Regex("^(1-0|0-1|1/2-1/2|\\*)$")
        private val WHITESPACE_RE = Regex("\\s+")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            runOnUiThread {
                try {
                    AlertDialog.Builder(this)
                        .setTitle("Crash: ${throwable.javaClass.simpleName}")
                        .setMessage(throwable.stackTraceToString())
                        .setPositiveButton("Close") { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                } catch (_: Exception) {
                    Toast.makeText(this, "Crash: ${throwable.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_live_session)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(b.left, b.top, b.right, b.bottom)
                insets
            }

            lessonId = intent.getStringExtra("LESSON_ID") ?: ""

            val rawRole = intent.getStringExtra("ROLE") ?: "Student"
            myRole = if (rawRole.equals("Coach", ignoreCase = true)) "Coach" else "Student"
            myName = intent.getStringExtra("NAME") ?: myRole

            bindViews()
            setupWebView()
            wireButtons()
            loadInitialState()

        } catch (e: Throwable) {
            AlertDialog.Builder(this)
                .setTitle("Initialization Error")
                .setMessage(e.stackTraceToString())
                .setPositiveButton("Close") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun bindViews() {
        webView        = findViewById(R.id.liveChessWebView)
        tvSessionTurn  = findViewById(R.id.tvSessionTurn)
        tvMoveHistory  = findViewById(R.id.tvMoveHistoryList)
        etChatInput    = findViewById(R.id.etChatInput)
        btnSendChat    = findViewById(R.id.btnSendChat)
        btnNavStart    = findViewById(R.id.btnLiveStart)
        btnNavPrev     = findViewById(R.id.btnLivePrev)
        btnNavNext     = findViewById(R.id.btnLiveNext)
        btnNavEnd      = findViewById(R.id.btnLiveEnd)
        chatScrollView = findViewById(R.id.chatScrollView)
        chatContainer  = findViewById(R.id.chatMessagesContainer)
        tvMoveHistory.text = "No moves yet."
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
        webView.webViewClient   = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(ChessBridge(this), "AndroidBridge")
        webView.loadUrl("file:///android_asset/chessboard.html")
    }

    private fun wireButtons() {
        findViewById<Button>(R.id.btnLeaveSession).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSessionFlip).setOnClickListener { js("flipBoard();") }

        btnSendChat.setOnClickListener {
            val text = etChatInput.text.toString().trim()
            if (text.isNotEmpty() && !isCompleted) {
                sendChatMessage(text)
                etChatInput.text.clear()
            }
        }

        btnNavStart.setOnClickListener { navigateTo(-1) }
        btnNavPrev .setOnClickListener { navigateTo(viewIndex - 1) }
        btnNavNext .setOnClickListener { navigateTo(viewIndex + 1) }
        btnNavEnd  .setOnClickListener { navigateTo(moveHistory.size - 1) }

        findViewById<Button>(R.id.btnSaveBoardState).setOnClickListener {
            if (!isCompleted) saveCurrentBoardState()
        }

        updateNavButtons()
    }

    private fun loadInitialState() {
        if (lessonId.isEmpty()) { startPollingLoop(); return }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.liveSessionService.getLessonById(lessonId)
                if (resp.isSuccessful && resp.body() != null) {
                    val lesson = resp.body()!!

                    val pgn   = lesson.pgnHistory ?: ""
                    val fen   = lesson.boardState ?: ""
                    val notes = lesson.notes      ?: ""

                    withContext(Dispatchers.Main) {
                        if (lesson.status == "COMPLETED") markCompleted()
                        serverFen = fen

                        webView.postDelayed({
                            applyBoardState(pgn, fen, jumpToLatest = true)
                        }, 800)

                        if (notes.isNotBlank()) {
                            currentNotes = notes
                            renderChatHistory(notes)
                        }
                    }
                }
            } catch (_: Exception) {  }
            startPollingLoop()
        }
    }

    private fun startPollingLoop() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (isActive && !isCompleted) {
                delay(2000)
                if (lessonId.isEmpty()) continue
                try {
                    val resp = RetrofitClient.liveSessionService.getLessonById(lessonId)
                    if (!resp.isSuccessful || resp.body() == null) continue
                    val lesson = resp.body()!!

                    val newStatus = lesson.status        ?: ""
                    val newPgn    = lesson.pgnHistory    ?: ""
                    val newFen    = lesson.boardState    ?: ""
                    val newNotes  = lesson.notes         ?: ""

                    val suppressEcho = System.currentTimeMillis() < lastLocalMoveMs + 3000

                    val strippedNew  = if (suppressEcho) canonicalPgn else stripPgn(newPgn)
                    val pgnChanged   = !suppressEcho && strippedNew != canonicalPgn
                    val fenChanged   = !suppressEcho && !pgnChanged && newPgn.isBlank() && newFen != serverFen
                    val notesChanged = newNotes.isNotBlank() && newNotes != currentNotes
                    val justCompleted = newStatus == "COMPLETED" && !isCompleted

                    if (!justCompleted && !pgnChanged && !fenChanged && !notesChanged) continue

                    withContext(Dispatchers.Main) {
                        if (justCompleted) markCompleted()

                        if (pgnChanged || fenChanged) {
                            serverFen = newFen
                            val wasAtEnd = viewIndex >= moveHistory.size - 1
                            applyBoardState(newPgn, newFen, jumpToLatest = wasAtEnd)
                        }

                        if (notesChanged) {
                            currentNotes = newNotes
                            renderChatHistory(newNotes)
                        }
                    }

                } catch (_: Exception) {  }
            }
        }
    }

    private fun applyBoardState(pgn: String, fen: String, jumpToLatest: Boolean) {
        if (pgn.isNotBlank()) {
            val stripped = stripPgn(pgn)
            canonicalPgn = stripped

            js("loadFromPgn(${pgn.jsStr()});")

            rebuildHistoryFromPgn(stripped)

            if (jumpToLatest) {
                viewIndex = (moveHistory.size - 1).coerceAtLeast(-1)
            } else {
                viewIndex = viewIndex.coerceIn(-1, (moveHistory.size - 1).coerceAtLeast(-1))
                js("goToMove($viewIndex);")
            }

        } else if (fen.isNotBlank()) {
            canonicalPgn = ""
            js("updateBoardFromFen(${fen.jsStr()});")
            moveHistory.clear()
            viewIndex = -1
            updateTurnFromFen(fen)
        } else {
            return
        }

        updateMoveHistoryText()
        updateNavButtons()
        updateTurnLabel()
    }

    private fun stripPgn(pgn: String): String {
        if (pgn.isBlank()) return ""
        return pgn
            .replace(HEADER_RE,  "")
            .replace(COMMENT_RE, " ")
            .replace(PAREN_RE,   " ")
            .replace(RESULT_RE,  "")
            .trim()
    }

    private fun rebuildHistoryFromPgn(stripped: String) {
        moveHistory.clear()
        if (stripped.isBlank()) { updateMoveHistoryText(); return }
        for (token in stripped.split(WHITESPACE_RE)) {
            val t = token.trim()
            if (t.isEmpty() || t.matches(MOVENUM_RE) || t.matches(RESULT_TOK)) continue
            moveHistory.add(t)
        }
    }

    private fun updateMoveHistoryText() {
        if (moveHistory.isEmpty()) { tvMoveHistory.text = "No moves yet."; return }
        val sb = StringBuilder()
        var moveNum = 1
        for (i in moveHistory.indices) {
            if (i % 2 == 0) sb.append("$moveNum. ")
            sb.append(if (i == viewIndex) "▶${moveHistory[i]}" else moveHistory[i])
            if (i % 2 == 1) { sb.append("\n"); moveNum++ } else sb.append("  ")
        }
        tvMoveHistory.text = sb.toString().trimEnd()
    }

    private fun navigateTo(index: Int) {
        val max = (moveHistory.size - 1).coerceAtLeast(-1)
        viewIndex = index.coerceIn(-1, max)
        js("goToMove($viewIndex);")
        updateMoveHistoryText()
        updateNavButtons()
        updateTurnLabel()
    }

    private fun updateNavButtons() {
        val atStart = viewIndex <= -1
        val atEnd   = viewIndex >= moveHistory.size - 1
        btnNavStart.isEnabled = !atStart; btnNavStart.alpha = if (atStart) 0.35f else 1f
        btnNavPrev .isEnabled = !atStart; btnNavPrev .alpha = if (atStart) 0.35f else 1f
        btnNavNext .isEnabled = !atEnd;   btnNavNext .alpha = if (atEnd)   0.35f else 1f
        btnNavEnd  .isEnabled = !atEnd;   btnNavEnd  .alpha = if (atEnd)   0.35f else 1f
    }

    private fun updateTurnLabel() {
        if (isCompleted) return
        tvSessionTurn.text = when {
            viewIndex < 0      -> "⚪ White to Move"
            viewIndex % 2 == 0 -> "⚫ Black to Move"
            else               -> "⚪ White to Move"
        }
    }

    private fun updateTurnFromFen(fen: String) {
        if (isCompleted) return
        val side = fen.split(" ").getOrElse(1) { "w" }
        tvSessionTurn.text = if (side == "w") "⚪ White to Move" else "⚫ Black to Move"
    }

    private fun onLocalMove(newFen: String, newPgn: String) {
        lastLocalMoveMs = System.currentTimeMillis()
        serverFen = newFen

        val stripped = stripPgn(newPgn)
        canonicalPgn = stripped
        rebuildHistoryFromPgn(stripped)

        viewIndex = moveHistory.size - 1
        updateMoveHistoryText()
        updateNavButtons()
        updateTurnLabel()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.liveSessionService.updateBoardState(
                    lessonId, BoardUpdateRequest(newFen, newPgn)
                )
            } catch (_: Exception) {  }
        }
    }

    private fun saveCurrentBoardState() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.liveSessionService.updateBoardState(
                    lessonId, BoardUpdateRequest(serverFen, canonicalPgn)
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LiveSessionActivity, "Board state saved!", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LiveSessionActivity, "Save failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendChatMessage(text: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val msg = JSONObject().apply {
                    put("sender", myRole)
                    put("name",   myName)
                    put("text",   text)
                    put("time",   SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
                }
                val arr     = if (currentNotes.isNotBlank()) JSONArray(currentNotes) else JSONArray()
                arr.put(msg)
                val updated = arr.toString()
                currentNotes = updated

                withContext(Dispatchers.Main) { renderChatHistory(updated) }

                RetrofitClient.liveSessionService.saveLessonNotes(
                    lessonId, NotesUpdateRequest(updated)
                )
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LiveSessionActivity, "Failed to send.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderChatHistory(jsonNotes: String) {
        chatContainer.removeAllViews()
        try {
            val arr = JSONArray(jsonNotes)
            for (i in 0 until arr.length()) {
                val msg = arr.getJSONObject(i)

                val isMe = msg.optString("sender", "").equals(myRole, ignoreCase = true)
                addChatBubble(
                    senderName = msg.optString("name",   "Unknown"),
                    message    = msg.optString("text",   ""),
                    time       = msg.optString("time",   ""),
                    isMe       = isMe
                )
            }
        } catch (_: Exception) {
            addChatBubble("System", jsonNotes, "", isSystem = true)
        }
        chatScrollView.post { chatScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun addChatBubble(
        senderName: String,
        message: String,
        time: String,
        isMe: Boolean = false,
        isSystem: Boolean = false
    ) {
        val header = TextView(this).apply {
            text = if (isSystem) "System" else "$senderName • $time"
            textSize = 10f
            setTextColor(Color.parseColor("#8F8174"))
            gravity = if (isMe) Gravity.END else Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val bubble = TextView(this).apply {
            text = message
            textSize = 14f
            setPadding(28, 18, 28, 18)
            maxWidth = (resources.displayMetrics.widthPixels * 0.68f).toInt()
            setTextColor(if (isMe) Color.WHITE else Color.parseColor("#2B2B2B"))
            setBackgroundResource(
                when {
                    isSystem -> R.drawable.bg_chat_bubble_system
                    isMe     -> R.drawable.bg_chat_bubble_outgoing
                    else     -> R.drawable.bg_chat_bubble_incoming
                }
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity      = if (isSystem) Gravity.CENTER_HORIZONTAL else if (isMe) Gravity.END else Gravity.START
                topMargin    = 4
                bottomMargin = 16
            }
        }
        chatContainer.addView(header)
        chatContainer.addView(bubble)
    }

    private fun markCompleted() {
        isCompleted = true
        etChatInput.isEnabled = false
        etChatInput.hint = "Session completed"
        tvSessionTurn.text = "Session Completed"
        webView.post { js("setReadOnly();") }
    }

    private fun js(script: String) = webView.evaluateJavascript(script, null)

    private fun String.jsStr(): String {
        val escaped = replace("\\", "\\\\")
            .replace("'",  "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
        return "'$escaped'"
    }

    class ChessBridge(activity: LiveSessionActivity) {
        private val actRef = java.lang.ref.WeakReference(activity)

        @JavascriptInterface
        fun onPlayerMove(newFen: String, newPgn: String, @Suppress("UNUSED_PARAMETER") san: String, @Suppress("UNUSED_PARAMETER") turn: String) {
            val act = actRef.get() ?: return
            act.runOnUiThread { act.onLocalMove(newFen, newPgn) }
        }
    }
}
