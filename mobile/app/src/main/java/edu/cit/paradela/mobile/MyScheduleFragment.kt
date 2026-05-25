package edu.cit.paradela.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MyScheduleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_schedule, container, false)

        // Find the Join button using the ID we added
        val btnJoin = view.findViewById<Button>(R.id.btnStudentJoinLesson)

        // Set the click listener to open the Live Session Room
        btnJoin?.setOnClickListener {
            val intent = Intent(requireActivity(), LiveSessionActivity::class.java)

            // Pass the role so the LiveSessionActivity knows a Student is entering
            intent.putExtra("ROLE", "STUDENT")

            startActivity(intent)
        }

        return view
    }
}