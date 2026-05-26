package edu.cit.paradela.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import edu.cit.paradela.mobile.models.LessonModel
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyScheduleFragment : Fragment() {

    private lateinit var llContainer: LinearLayout
    private var studentId: String = ""
    private var studentName: String = ""
    private var token: String = ""

    // ── Join window helpers ─────────────────────────────────────────────────

    /**
     * Returns true if the current time is within 10 minutes of the lesson
     * start time (or after it). This is the window in which "Join Lesson" is active.
     */
    private fun canJoinLesson(startTime: String?): Boolean {
        if (startTime == null) return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val lessonDate = sdf.parse(startTime) ?: return true
            val windowOpen = Date(lessonDate.time - 10 * 60 * 1000L)
            Date().after(windowOpen) || Date() == windowOpen
        } catch (e: Exception) { true }
    }

    /** Text to show on the disabled button while the window has not opened yet. */
    private fun joinCountdownLabel(startTime: String?): String {
        if (startTime == null) return "Join Lesson"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val lessonDate = sdf.parse(startTime) ?: return "Join Lesson"
            val minsUntil = ((lessonDate.time - Date().time) / 60_000L).toInt().coerceAtLeast(0)
            if (minsUntil <= 10) return "Join Lesson"
            val hrs = minsUntil / 60; val mins = minsUntil % 60
            if (hrs > 0) "Opens in ${hrs}h ${mins}m" else "Opens in ${minsUntil} min"
        } catch (e: Exception) { "Join Lesson" }
    }

    /** Polling coroutine — runs every 10 s while the fragment is visible. */
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llContainer = view.findViewById(R.id.llStudentScheduleContainer)

        studentId   = requireActivity().intent.getStringExtra("USER_ID") ?: ""
        studentName = requireActivity().intent.getStringExtra("NAME")    ?: "Student"
        token       = requireActivity().intent.getStringExtra("TOKEN")   ?: ""
        // Initial fetch is triggered by onResume() below
    }

    // ── Polling lifecycle ────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Fetch the schedule immediately, then repeat every 10 seconds while
     * the fragment is in the foreground. This makes status changes made by
     * the coach (accept / reject) appear without the student navigating away.
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                fetchStudentSchedule()
                delay(10_000L)
            }
        }
    }

    private fun fetchStudentSchedule() {
        if (studentId.isEmpty()) {
            showEmptyMessage("Not logged in. Please restart the app.")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Correct endpoint: GET /api/lessons/student/{studentId}
                val response = RetrofitClient.studentService.getStudentLessons(studentId)

                withContext(Dispatchers.Main) {
                    llContainer.removeAllViews()

                    if (response.isSuccessful && response.body() != null) {
                        val lessons = response.body()!!
                        if (lessons.isEmpty()) {
                            showEmptyMessage("No lessons yet. Book one from the Book tab!")
                        } else {
                            // Show most recent first
                            lessons.sortedByDescending { it.startTime }.forEach { lesson ->
                                createLessonCard(lesson)
                            }
                        }
                    } else {
                        showEmptyMessage("Failed to load schedule (${response.code()}).")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    llContainer.removeAllViews()
                    showEmptyMessage("Server offline. Pull to retry.")
                }
            }
        }
    }

    private fun createLessonCard(lesson: LessonModel) {
        val card = MaterialCardView(requireContext()).apply {
            setCardBackgroundColor(Color.WHITE)
            radius = 24f
            setContentPadding(40, 40, 40, 40)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
            layoutParams = params
        }

        val contentLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }

        // Coach Name
        val nameText = TextView(requireContext()).apply {
            text = "Coach: ${lesson.coachName ?: "Unknown Coach"}"
            textSize = 20f
            setTextColor(Color.parseColor("#333333"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Time
        val timeText = TextView(requireContext()).apply {
            text = formatLessonTime(lesson.startTime)
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 8, 0, 24)
        }

        val bottomRow = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }

        // Status Badge
        val statusBadge = TextView(requireContext()).apply {
            text = lesson.status ?: "PENDING"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 8, 24, 8)

            when (lesson.status) {
                "COMPLETED" -> {
                    setTextColor(Color.parseColor("#15803D"))
                    setBackgroundColor(Color.parseColor("#DCFCE7"))
                }
                "REJECTED" -> {
                    setTextColor(Color.parseColor("#991B1B"))
                    setBackgroundColor(Color.parseColor("#FEE2E2"))
                }
                "ACCEPTED" -> {
                    setTextColor(Color.parseColor("#1E40AF"))
                    setBackgroundColor(Color.parseColor("#DBEAFE"))
                }
                else -> { // PENDING
                    setTextColor(Color.parseColor("#92400E"))
                    setBackgroundColor(Color.parseColor("#FEF3C7"))
                }
            }
        }

        val space = Space(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Only show a join/review action once the coach has accepted.
        // PENDING = waiting for coach; REJECTED = declined; neither gets a button.
        if (lesson.status == "ACCEPTED" || lesson.status == "COMPLETED") {
            val btnJoin = Button(requireContext()).apply {
                when {
                    lesson.status == "COMPLETED" -> {
                        text = "Review Lesson"
                        setBackgroundColor(Color.parseColor("#BDBDBD"))
                        isEnabled = true
                    }
                    canJoinLesson(lesson.startTime) -> {
                        text = "Join Lesson"
                        setBackgroundColor(Color.parseColor("#D4AF37"))
                        isEnabled = true
                    }
                    else -> {
                        text = joinCountdownLabel(lesson.startTime)
                        setBackgroundColor(Color.parseColor("#9E9E9E"))
                        isEnabled = false
                        alpha = 0.6f
                    }
                }
                setTextColor(Color.WHITE)

                setOnClickListener {
                    if (lesson.status == "COMPLETED") {
                        // Go to read-only review screen
                        val intent = Intent(requireActivity(), LessonReviewActivity::class.java)
                        intent.putExtra("LESSON_ID", lesson.id)
                        intent.putExtra("COACH_NAME", lesson.coachName ?: "Coach")
                        intent.putExtra("LESSON_DATE", formatLessonTime(lesson.startTime))
                        startActivity(intent)
                    } else {
                        // Go to live session
                        val intent = Intent(requireActivity(), LiveSessionActivity::class.java)
                        intent.putExtra("ROLE", "STUDENT")
                        intent.putExtra("NAME", studentName)
                        intent.putExtra("LESSON_ID", lesson.id)
                        startActivity(intent)
                    }
                }
            }
            bottomRow.addView(space)
            bottomRow.addView(btnJoin)
        }

        bottomRow.addView(statusBadge)

        contentLayout.addView(nameText)
        contentLayout.addView(timeText)
        contentLayout.addView(bottomRow)
        card.addView(contentLayout)
        llContainer.addView(card)
    }

    private fun showEmptyMessage(msg: String) {
        val tv = TextView(requireContext()).apply {
            text = msg
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 16)
        }
        llContainer.addView(tv)
    }

    /**
     * Converts "2026-05-26T10:00:00" → "May 26, 2026 • 10:00 AM"
     */
    private fun formatLessonTime(raw: String?): String {
        if (raw == null) return "Unknown Time"
        return try {
            val inFmt  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFmt = SimpleDateFormat("h:mm a",       Locale.getDefault())
            val date = inFmt.parse(raw) ?: return raw
            "${dateFmt.format(date)} • ${timeFmt.format(date)}"
        } catch (e: Exception) { raw }
    }
}