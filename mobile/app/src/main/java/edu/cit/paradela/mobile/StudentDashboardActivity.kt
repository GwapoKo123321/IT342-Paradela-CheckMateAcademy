package edu.cit.paradela.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class StudentDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.studentBottomNavigation)
        val btnLogout = findViewById<Button>(R.id.btnStudentLogout)

        if (savedInstanceState == null) {
            loadFragment(MyScheduleFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_student_schedule -> { loadFragment(MyScheduleFragment()); true }
                R.id.nav_student_book -> { loadFragment(BookLessonFragment()); true }

                // ADDED THE TRAINING BOARD ROUTE
                R.id.nav_student_training -> { loadFragment(TrainingBoardFragment()); true }

                R.id.nav_student_reviews -> { loadFragment(LessonReviewsFragment()); true }
                R.id.nav_student_profile -> { loadFragment(AccountProfileFragment()); true }
                else -> false
            }
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_fragment_container, fragment)
            .commit()
    }
}