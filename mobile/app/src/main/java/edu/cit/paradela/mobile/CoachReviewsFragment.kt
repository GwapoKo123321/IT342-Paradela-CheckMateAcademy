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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CoachReviewsFragment : Fragment() {

    private lateinit var llContainer: LinearLayout
    private var coachId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_coach_reviews, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llContainer = view.findViewById(R.id.llCoachReviewsContainer)
        coachId     = requireActivity().intent.getStringExtra("USER_ID") ?: ""
        fetchCompletedLessons()
    }

    private fun fetchCompletedLessons() {
        if (coachId.isEmpty()) { showEmpty("Not logged in. Please restart the app."); return }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.coachService.getCoachLessons(coachId)
                withContext(Dispatchers.Main) {
                    llContainer.removeAllViews()
                    if (response.isSuccessful && response.body() != null) {
                        val completed = response.body()!!.filter { it.status == "COMPLETED" }
                            .sortedByDescending { it.startTime }
                        if (completed.isEmpty()) showEmpty("No completed lessons yet.")
                        else completed.forEach { createReviewCard(it) }
                    } else {
                        showEmpty("Failed to load reviews (${response.code()}).")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    llContainer.removeAllViews()
                    showEmpty("Server offline. Try again later.")
                }
            }
        }
    }

    private fun createReviewCard(lesson: LessonModel) {
        val card = MaterialCardView(requireContext()).apply {
            setCardBackgroundColor(Color.WHITE)
            radius = 24f
            cardElevation = 8f
            setContentPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
        }

        val content = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }

        // Student name
        content.addView(TextView(requireContext()).apply {
            text = "Student: ${lesson.studentName ?: "Unknown Student"}"
            textSize = 18f
            setTextColor(Color.parseColor("#333333"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        // Date
        content.addView(TextView(requireContext()).apply {
            text = formatLessonTime(lesson.startTime)
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, 8, 0, 24)
        })

        // Bottom row: COMPLETED badge + View/Edit Notes button
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        row.addView(TextView(requireContext()).apply {
            text = "COMPLETED"
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 8, 24, 8)
            setTextColor(Color.parseColor("#15803D"))
            setBackgroundColor(Color.parseColor("#DCFCE7"))
        })

        row.addView(Space(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        row.addView(Button(requireContext()).apply {
            text = "View / Edit Notes"
            setBackgroundColor(Color.parseColor("#6B4F3A"))
            setTextColor(Color.WHITE)
            textSize = 12f
            setOnClickListener {
                val intent = Intent(requireActivity(), LessonReviewActivity::class.java)
                intent.putExtra("LESSON_ID", lesson.id)
                intent.putExtra("STUDENT_NAME", lesson.studentName ?: "Student")
                intent.putExtra("LESSON_DATE", formatLessonTime(lesson.startTime))
                startActivity(intent)
            }
        })

        content.addView(row)
        card.addView(content)
        llContainer.addView(card)
    }

    private fun formatLessonTime(raw: String?): String {
        if (raw == null) return "Unknown Date"
        return try {
            val inFmt   = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateFmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inFmt.parse(raw) ?: return raw
            "${dateFmt.format(date)} • ${timeFmt.format(date)}"
        } catch (e: Exception) { raw }
    }

    private fun showEmpty(msg: String) {
        llContainer.addView(TextView(requireContext()).apply {
            text = msg
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 16)
        })
    }
}