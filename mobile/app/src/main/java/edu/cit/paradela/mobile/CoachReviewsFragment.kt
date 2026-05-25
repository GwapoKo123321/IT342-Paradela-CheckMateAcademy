package edu.cit.paradela.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class CoachReviewsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_coach_reviews, container, false)

        val btnViewNotes = view.findViewById<Button>(R.id.btnViewNotes)

        btnViewNotes?.setOnClickListener {
            startActivity(Intent(requireActivity(), LessonReviewActivity::class.java))
        }

        return view
    }
}