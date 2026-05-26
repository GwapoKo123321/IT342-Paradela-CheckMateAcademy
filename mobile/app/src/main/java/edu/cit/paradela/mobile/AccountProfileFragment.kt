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
import edu.cit.paradela.mobile.models.UserProfile
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountProfileFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var etChessUsername: EditText
    private lateinit var etElo: EditText
    private lateinit var btnSave: Button

    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_account_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etFullName      = view.findViewById(R.id.etProfileFullName)
        etChessUsername = view.findViewById(R.id.etProfileChessUsername)
        etElo           = view.findViewById(R.id.etProfileElo)
        btnSave         = view.findViewById(R.id.btnSaveProfile)

        // Get the user ID passed in from LoginActivity
        userId = requireActivity().intent.getStringExtra("USER_ID") ?: ""

        // Pre-fill profile fields from the login/register intent when available.
        val savedName = requireActivity().intent.getStringExtra("NAME")
        if (!savedName.isNullOrBlank()) etFullName.setText(savedName)
        requireActivity().intent.getStringExtra("CHESS_USERNAME")
            ?.takeIf { it.isNotBlank() }
            ?.let { etChessUsername.setText(it) }
        val savedElo = requireActivity().intent.getIntExtra("CURRENT_ELO", -1)
        if (savedElo >= 0) etElo.setText(savedElo.toString())

        btnSave.setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "Not logged in. Please restart the app.", Toast.LENGTH_SHORT).show()
            return
        }

        val name     = etFullName.text.toString().trim()
        val username = etChessUsername.text.toString().trim()
        val eloStr   = etElo.text.toString().trim()
        val elo      = eloStr.toIntOrNull() ?: 0

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Full name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val update = UserProfile(
                    fullName      = name,
                    chessUsername = username.ifEmpty { null },
                    currentElo    = elo
                )
                val resp = RetrofitClient.studentService.updateProfile(userId, update)

                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful && resp.body() != null) {
                        requireActivity().intent.putExtra("NAME", name)
                        requireActivity().intent.putExtra("CHESS_USERNAME", username)
                        requireActivity().intent.putExtra("CURRENT_ELO", elo)
                        Toast.makeText(requireContext(), "✓ Profile saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Save failed (${resp.code()}). Try again.", Toast.LENGTH_SHORT).show()
                    }
                    btnSave.isEnabled = true
                    btnSave.text = "Save Updates"
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Network error. Check your connection.", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    btnSave.text = "Save Updates"
                }
            }
        }
    }
}
