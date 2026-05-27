package edu.cit.paradela.mobile
import edu.cit.paradela.mobile.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class CoachDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_coach_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.coachBottomNavigation)
        val btnLogout = findViewById<Button>(R.id.btnCoachLogout)

        if (savedInstanceState == null) {
            loadFragment(CoachScheduleFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_coach_lessons -> {
                    loadFragment(CoachScheduleFragment())
                    true
                }
                R.id.nav_coach_reviews -> {
                    loadFragment(CoachReviewsFragment())
                    true
                }
                R.id.nav_coach_profile -> {
                    loadFragment(CoachProfileFragment())
                    true
                }
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
            .replace(R.id.coach_fragment_container, fragment)
            .commit()
    }
}
