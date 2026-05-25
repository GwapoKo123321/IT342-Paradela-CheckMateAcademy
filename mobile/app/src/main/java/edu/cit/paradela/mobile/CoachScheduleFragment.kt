package edu.cit.paradela.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class CoachScheduleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_coach_schedule, container, false)

        // Find the Join button by the ID we just added in the XML
        val btnJoin = view.findViewById<Button>(R.id.btnJoinLesson)

        // Set the click listener to open the Live Session Room
        btnJoin?.setOnClickListener {
            val intent = Intent(requireActivity(), LiveSessionActivity::class.java)

            // Pass the role so the LiveSessionActivity knows a Coach is entering
            intent.putExtra("ROLE", "COACH")

            startActivity(intent)
        }

        return view
    }
}