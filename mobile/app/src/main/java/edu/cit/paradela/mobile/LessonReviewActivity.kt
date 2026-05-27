package edu.cit.paradela.mobile

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

class LessonReviewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvMoveHistory: TextView
    private lateinit var tvOpponent: TextView
    private lateinit var llChatMessages: LinearLayout
    private lateinit var tvNoChat: TextView

    private var totalMoves = 0
    private var currentMoveIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lesson_review)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val lessonId    = intent.getStringExtra("LESSON_ID") ?: ""
        val coachName   = intent.getStringExtra("COACH_NAME")
        val studentName = intent.getStringExtra("STUDENT_NAME")
        val lessonDate  = intent.getStringExtra("LESSON_DATE") ?: ""

        tvOpponent    = findViewById(R.id.tvReviewOpponent)
        tvMoveHistory = findViewById(R.id.tvReviewHistoryList)
        llChatMessages = findViewById(R.id.llChatMessages)
        tvNoChat      = findViewById(R.id.tvNoChat)
        webView       = findViewById(R.id.reviewChessWebView)

        tvOpponent.text = when {
            coachName   != null -> "with Coach $coachName • $lessonDate"
            studentName != null -> "with $studentName • $lessonDate"
            else                -> lessonDate
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = true
            allowContentAccess = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.isVerticalScrollBarEnabled   = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webChromeClient = android.webkit.WebChromeClient()

        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {

                webView.evaluateJavascript("setReadOnly();", null)
                if (lessonId.isNotEmpty()) fetchAndPopulate(lessonId)
            }
        }
        webView.loadUrl("file:///android_asset/chessboard.html")

        findViewById<Button>(R.id.btnBackToDashboard).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnReviewFlip).setOnClickListener {
            webView.evaluateJavascript("flipBoard();", null)
        }

        setupNavButtons()
    }

    private fun fetchAndPopulate(lessonId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.liveSessionService.getLessonById(lessonId)
                withContext(Dispatchers.Main) {
                    if (!response.isSuccessful || response.body() == null) {
                        tvMoveHistory.text = "Could not load lesson data (${response.code()})."
                        return@withContext
                    }
                    val lesson = response.body()!!

                    val pgn = lesson.pgnHistory?.trim() ?: ""
                    val fen = lesson.boardState?.trim() ?: ""

                    if (pgn.isNotEmpty()) {
                        val escaped = pgn.replace("\\", "\\\\").replace("'", "\\'")
                        webView.evaluateJavascript("loadFromPgn('$escaped');", null)

                        totalMoves = countHalfMoves(pgn)
                        currentMoveIndex = totalMoves - 1
                    } else if (fen.isNotEmpty()) {
                        val escaped = fen.replace("'", "\\'")
                        webView.evaluateJavascript("updateBoardFromFen('$escaped');", null)
                        totalMoves = 0
                    }

                    buildMoveHistoryText(pgn)

                    renderChat(lesson.notes ?: "[]")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvMoveHistory.text = "Could not load lesson data."
                }
            }
        }
    }

    private fun countHalfMoves(pgn: String): Int {
        if (pgn.isBlank()) return 0
        return pgn
            .replace(Regex("\\[[^\\]]*\\]"), "")
            .replace(Regex("\\{[^}]*\\}"), "")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && !it.matches(Regex("\\d+\\.+")) &&
                      it != "*" && it != "1-0" && it != "0-1" && it != "1/2-1/2" }
            .size
    }

    private fun buildMoveHistoryText(pgn: String) {
        if (pgn.isBlank()) {
            tvMoveHistory.text = "No moves recorded."
            return
        }

        val moves = pgn
            .replace(Regex("\\[[^\\]]*\\]"), "")
            .replace(Regex("\\{[^}]*\\}"), "")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && !it.matches(Regex("\\d+\\.+")) &&
                      it != "*" && it != "1-0" && it != "0-1" && it != "1/2-1/2" }

        if (moves.isEmpty()) { tvMoveHistory.text = "No moves recorded."; return }

        val sb = StringBuilder()
        moves.chunked(2).forEachIndexed { i, pair ->
            sb.append("${i + 1}. ${pair[0]}")
            if (pair.size > 1) sb.append("  ${pair[1]}")
            sb.append("\n")
        }
        tvMoveHistory.text = sb.toString().trim()
    }

    private fun setupNavButtons() {
        try {

            findViewById<Button>(R.id.btnNavFirst).setOnClickListener {
                currentMoveIndex = -1
                webView.evaluateJavascript("goToMove(-1);", null)
                highlightMove(-1)
            }

            findViewById<Button>(R.id.btnNavPrev).setOnClickListener {
                if (currentMoveIndex > -1) {
                    currentMoveIndex--
                    webView.evaluateJavascript("goToMove($currentMoveIndex);", null)
                    highlightMove(currentMoveIndex)
                }
            }

            findViewById<Button>(R.id.btnNavNext).setOnClickListener {
                if (currentMoveIndex < totalMoves - 1) {
                    currentMoveIndex++
                    webView.evaluateJavascript("goToMove($currentMoveIndex);", null)
                    highlightMove(currentMoveIndex)
                }
            }

            findViewById<Button>(R.id.btnNavLast).setOnClickListener {
                currentMoveIndex = totalMoves - 1
                webView.evaluateJavascript("goToMove($currentMoveIndex);", null)
                highlightMove(currentMoveIndex)
            }
        } catch (e: Exception) {  }
    }

    private fun highlightMove(halfMoveIndex: Int) {
        val raw = tvMoveHistory.text.toString()
        if (raw == "No moves recorded." || raw.isBlank()) return

        val lines = raw.split("\n").filter { it.isNotBlank() }
        val sb = StringBuilder()
        lines.forEachIndexed { lineIdx, line ->

            val whitePly = lineIdx * 2
            val blackPly = lineIdx * 2 + 1
            val parts = line.split(Regex("\\s{2,}"), limit = 2)
            if (parts.size == 2) {
                val wPart = if (halfMoveIndex == whitePly) "[${parts[0]}]" else parts[0]
                val bPart = if (halfMoveIndex == blackPly) "[${parts[1]}]" else parts[1]
                sb.appendLine("$wPart  $bPart")
            } else {
                val wPart = if (halfMoveIndex == whitePly) "[${line}]" else line
                sb.appendLine(wPart)
            }
        }
        tvMoveHistory.text = sb.toString().trim()
    }

    private fun renderChat(notesJson: String) {
        val messages = try {
            val arr = JSONArray(notesJson)
            (0 until arr.length()).mapNotNull { i ->
                val obj     = arr.getJSONObject(i)
                val name    = obj.optString("name",   "").trim()
                val role    = obj.optString("sender", "").trim()
                val message = obj.optString("text",   "").trim()
                val time    = obj.optString("time",   "").trim()

                if (message.isNotEmpty()) Triple("$name|$role", message, time) else null
            }
        } catch (e: Exception) {
            emptyList()
        }

        if (messages.isEmpty()) {
            tvNoChat.visibility = android.view.View.VISIBLE
            return
        }

        tvNoChat.visibility = android.view.View.GONE
        messages.forEach { (triple, message, time) ->

            val parts  = triple.split("|")
            val name   = parts.getOrElse(0) { triple }
            val role   = parts.getOrElse(1) { "" }
            addChatBubble(name, role, message, time)
        }
    }

    private fun addChatBubble(name: String, role: String, message: String, time: String) {

        val isStudent = role.equals("STUDENT", ignoreCase = true)
        val gravity   = if (isStudent) Gravity.END else Gravity.START

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 20) }
        }

        container.addView(TextView(this).apply {
            text = if (time.isNotBlank()) "$name • $time" else name
            textSize = 10f
            setTextColor(Color.parseColor("#8F8174"))
            this.gravity = gravity
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 4 }
        })

        container.addView(TextView(this).apply {
            text = message
            textSize = 14f
            setPadding(28, 18, 28, 18)
            maxWidth = (resources.displayMetrics.widthPixels * 0.68f).toInt()
            setTextColor(if (isStudent) Color.WHITE else Color.parseColor("#2B2B2B"))
            setBackgroundResource(
                if (isStudent) R.drawable.bg_chat_bubble_outgoing else R.drawable.bg_chat_bubble_incoming
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { this.gravity = gravity }
        })

        llChatMessages.addView(container)
    }
}
