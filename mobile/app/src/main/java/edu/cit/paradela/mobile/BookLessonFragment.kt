package edu.cit.paradela.mobile

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import edu.cit.paradela.mobile.models.BookingRequest
import edu.cit.paradela.mobile.models.CoachAvailableSlot
import edu.cit.paradela.mobile.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BookLessonFragment : Fragment() {

    private lateinit var llAvailableTimesContainer: LinearLayout
    private lateinit var etBookingDate: EditText
    private lateinit var btnConfirmBooking: Button

    private var selectedDateStr: String? = null
    private var selectedSlot: CoachAvailableSlot? = null

    private val slotCards = mutableListOf<MaterialCardView>()

    private var studentId: String = ""
    private var studentName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentId = requireActivity().intent.getStringExtra("USER_ID") ?: ""
        studentName = requireActivity().intent.getStringExtra("NAME") ?: "Student"

        llAvailableTimesContainer = view.findViewById(R.id.llAvailableTimesContainer)
        etBookingDate = view.findViewById(R.id.etBookingDate)
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking)
        btnConfirmBooking.isEnabled = false

        etBookingDate.setOnClickListener {
            showDatePicker()
        }

        btnConfirmBooking.setOnClickListener {
            val slot = selectedSlot
            if (slot != null) {
                submitBookingRequest(slot)
            } else {
                Toast.makeText(requireContext(), "Please select a time slot.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                val displayFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                etBookingDate.setText(displayFormat.format(calendar.time))

                val queryFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDateStr = queryFormat.format(calendar.time)

                selectedSlot = null
                btnConfirmBooking.isEnabled = false
                fetchAvailableSlots(selectedDateStr!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        picker.datePicker.minDate = Calendar.getInstance().timeInMillis
        picker.show()
    }

    private fun isSlotFuture(slot: CoachAvailableSlot, dateStr: String): Boolean {
        return try {
            val rawTime = slot.startTime ?: return true

            val timePart = if (rawTime.contains('T')) rawTime.substringAfter('T').substring(0, 5)
                           else rawTime.substring(0, 5)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val slotDate: Date = sdf.parse("${dateStr}T${timePart}") ?: return true
            slotDate.after(Date())
        } catch (e: Exception) {
            true
        }
    }

    private fun fetchAvailableSlots(date: String) {
        llAvailableTimesContainer.removeAllViews()
        slotCards.clear()

        val loadingTv = TextView(requireContext()).apply {
            text = "Loading available slots..."
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 16)
        }
        llAvailableTimesContainer.addView(loadingTv)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {

                val response = RetrofitClient.studentService.getAvailableSlots(
                    date = date,
                    studentId = studentId
                )

                withContext(Dispatchers.Main) {
                    llAvailableTimesContainer.removeAllViews()
                    slotCards.clear()

                    if (response.isSuccessful && response.body() != null) {
                        val allSlots = response.body()!!

                        val slots = allSlots.filter { isSlotFuture(it, date) }
                        if (slots.isEmpty()) {
                            showEmptyMessage("No available coaches on this date. Try another day.")
                        } else {
                            slots.forEach { slot -> addSlotCard(slot) }
                        }
                    } else {
                        val errorText = response.errorBody()?.string() ?: "Unknown Error"
                        android.util.Log.e("SLOT_ERROR", "Code: ${response.code()}, Msg: $errorText")
                        showEmptyMessage("Failed to load slots (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    llAvailableTimesContainer.removeAllViews()
                    android.util.Log.e("SLOT_ERROR", "Exception: ${e.message}")
                    showEmptyMessage("Server offline or connection error.")
                }
            }
        }
    }

    private fun addSlotCard(slot: CoachAvailableSlot) {
        val cardView = layoutInflater.inflate(
            R.layout.item_coach_slot, llAvailableTimesContainer, false
        ) as MaterialCardView

        val tvName = cardView.findViewById<TextView>(R.id.tvItemCoachName)
        val tvElo = cardView.findViewById<TextView>(R.id.tvItemCoachElo)

        val startFormatted = formatSlotTime(slot.startTime)
        val endFormatted   = formatSlotTime(slot.endTime)
        tvName.text = "${slot.coachName ?: "Unknown Coach"}  •  $startFormatted – $endFormatted"

        val eloText = if (slot.currentElo != null && slot.currentElo > 0)
            "ELO: ${slot.currentElo}${if (slot.eloVerified == true) " ✓" else ""}"
        else "ELO: Unrated"
        tvElo.text = eloText

        if (slot.studentConflict == true) {
            cardView.alpha = 0.5f
            tvElo.text = "$eloText  |  ${slot.conflictLabel ?: "You have a conflict"}"
        }

        cardView.setOnClickListener {
            if (slot.studentConflict == true) {
                Toast.makeText(requireContext(), "You already have a lesson at this time.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            slotCards.forEach {
                it.strokeColor = Color.parseColor("#E5E7EB")
                it.strokeWidth = 2
                it.setCardBackgroundColor(Color.WHITE)
            }

            cardView.strokeColor = Color.parseColor("#D4AF37")
            cardView.strokeWidth = 4
            cardView.setCardBackgroundColor(Color.parseColor("#FFFDF5"))

            selectedSlot = slot
            btnConfirmBooking.isEnabled = true
            btnConfirmBooking.setBackgroundColor(Color.parseColor("#6B4F3A"))
        }

        slotCards.add(cardView)
        llAvailableTimesContainer.addView(cardView)
    }

    private fun submitBookingRequest(slot: CoachAvailableSlot) {
        btnConfirmBooking.isEnabled = false
        btnConfirmBooking.text = "Booking..."

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = BookingRequest(
                    coachId = slot.coachId ?: "",
                    studentId = studentId,
                    coachName = slot.coachName ?: "Coach",
                    studentName = studentName,
                    startTime = slot.startTime ?: "",
                    endTime = slot.endTime ?: ""
                )

                val response = RetrofitClient.studentService.bookLesson(request)

                withContext(Dispatchers.Main) {
                    btnConfirmBooking.text = "Confirm Booking"
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Lesson booked! Waiting for coach approval.",
                            Toast.LENGTH_LONG
                        ).show()

                        selectedSlot = null
                        selectedDateStr = null
                        etBookingDate.setText("")
                        llAvailableTimesContainer.removeAllViews()
                        slotCards.clear()
                        btnConfirmBooking.isEnabled = false
                    } else {
                        val error = response.errorBody()?.string() ?: "Booking failed"
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        btnConfirmBooking.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnConfirmBooking.isEnabled = true
                    btnConfirmBooking.text = "Confirm Booking"
                    Toast.makeText(requireContext(), "Network error. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEmptyMessage(message: String) {
        val tv = TextView(requireContext()).apply {
            text = message
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 16)
        }
        llAvailableTimesContainer.addView(tv)
    }

    private fun formatSlotTime(raw: String?): String {
        if (raw == null) return ""
        return try {
            val timePart = if (raw.contains('T')) raw.substringAfter('T') else raw
            val inFmt  = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outFmt = SimpleDateFormat("h:mm a",  Locale.getDefault())
            outFmt.format(inFmt.parse(timePart) ?: return raw)
        } catch (e: Exception) { raw }
    }
}
