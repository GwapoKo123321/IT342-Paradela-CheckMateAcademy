package edu.cit.paradela.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.cit.paradela.mobile.models.CoachProfileRequest
import edu.cit.paradela.mobile.models.UserProfile
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoachProfileFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var etChessUsername: EditText
    private lateinit var etElo: EditText
    private lateinit var etSpecialties: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: Button

    private var coachId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_coach_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etFullName      = view.findViewById(R.id.etCoachFullName)
        etChessUsername = view.findViewById(R.id.etCoachChessUsername)
        etElo           = view.findViewById(R.id.etCoachElo)
        etSpecialties   = view.findViewById(R.id.etCoachSpecialties)
        etBio           = view.findViewById(R.id.etCoachBio)
        btnSave         = view.findViewById(R.id.btnSaveCoachProfile)

        coachId = requireActivity().intent.getStringExtra("USER_ID") ?: ""

        // Pre-fill name from login intent
        val savedName = requireActivity().intent.getStringExtra("NAME")
        if (!savedName.isNullOrBlank()) etFullName.setText(savedName)

        // Load current coach profile from server to pre-fill all fields
        if (coachId.isNotEmpty()) loadCoachProfile()

        btnSave.setOnClickListener { saveCoachProfile() }
    }

    // ── Load ───────────────────────────────────────────────────────────────────
    private fun loadCoachProfile() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.coachService.getCoachProfile(coachId)
                if (resp.isSuccessful && resp.body() != null) {
                    val profile = resp.body()!!
                    withContext(Dispatchers.Main) {
                        if (!profile.fullName.isNullOrBlank())       etFullName.setText(profile.fullName)
                        if (!profile.chessUsername.isNullOrBlank())  etChessUsername.setText(profile.chessUsername)
                        if (profile.currentElo != null)              etElo.setText(profile.currentElo.toString())
                        if (!profile.specialties.isNullOrBlank())    etSpecialties.setText(profile.specialties)
                        if (!profile.bio.isNullOrBlank())            etBio.setText(profile.bio)
                    }
                }
            } catch (_: Exception) {
                // Silently fail — user can still fill fields manually
            }
        }
    }

    // ── Save ───────────────────────────────────────────────────────────────────
    private fun saveCoachProfile() {
        if (coachId.isEmpty()) {
            Toast.makeText(requireContext(), "Not logged in. Please restart the app.", Toast.LENGTH_SHORT).show()
            return
        }

        val name        = etFullName.text.toString().trim()
        val username    = etChessUsername.text.toString().trim()
        val eloStr      = etElo.text.toString().trim()
        val elo         = eloStr.toIntOrNull() ?: 0
        val specialties = etSpecialties.text.toString().trim()
        val bio         = etBio.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Full name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            var success = false
            var errorCode = 0
            try {
                // 1. Update name, chess username, elo via the general user profile endpoint
                val userUpdate = UserProfile(
                    fullName      = name,
                    chessUsername = username.ifEmpty { null },
                    currentElo    = elo
                )
                val userResp = RetrofitClient.coachService.updateUserProfile(coachId, userUpdate)
                if (!userResp.isSuccessful) { errorCode = userResp.code() }

                // 2. Update coach-specific fields (specialties, bio) via the coach profile endpoint
                val coachUpdate = CoachProfileRequest(
                    specialties  = specialties,
                    bio          = bio,
                    availability = emptyList()  // availability managed separately
                )
                val coachResp = RetrofitClient.coachService.updateCoachProfile(coachId, coachUpdate)

                success = userResp.isSuccessful && coachResp.isSuccessful
                if (!coachResp.isSuccessful) errorCode = coachResp.code()

            } catch (_: Exception) {
                success = false
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(requireContext(), "✓ Coach profile saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Save failed ($errorCode). Try again.", Toast.LENGTH_SHORT).show()
                }
                btnSave.isEnabled = true
                btnSave.text = "Save Profile"
            }
        }
    }
}