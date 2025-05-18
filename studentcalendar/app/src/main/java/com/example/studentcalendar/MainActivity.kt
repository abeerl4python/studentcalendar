package com.example.studentcalendar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Calendar UI Components
    private lateinit var currentMonthText: TextView
    private lateinit var prevMonthText: TextView
    private lateinit var nextMonthText: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var dateContainer: LinearLayout

    // Calendar Logic Components
    private lateinit var calendar: Calendar
    private lateinit var dateFormatter: SimpleDateFormat
    private lateinit var monthFormatter: SimpleDateFormat
    private var selectedDatePosition = -1

    // User Preferences
    private lateinit var userPreferences: UserPreferences

    // Sample events data
    private val events = mapOf(
        "2023-04-05" to listOf(
            Event("Science", "Room 5 • Otto", "9:00 AM - 10:00 AM", "#B29CD3"),
            Event("Maths", "Floor 3 • Alex", "11:00 AM - 12:50 PM", "#FF97C1")
        ),
        "2023-04-07" to listOf(
            Event("History", "Room 12 • Sarah", "2:00 PM - 3:30 PM", "#8AC9B5")
        ),
        "2023-04-08" to listOf(
            Event("Literature", "Library • James", "10:30 AM - 12:00 PM", "#FFB347")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize UserPreferences and check authentication
        userPreferences = UserPreferences(this)
        checkAuthentication()
    }

    private fun checkAuthentication() {
        lifecycleScope.launch {
            userPreferences.isLoggedInFlow.collect { isLoggedIn ->
                if (isLoggedIn) {
                    initializeUI()
                } else {
                    redirectToLogin()
                }
            }
        }
    }

    private fun initializeUI() {
        setContentView(R.layout.activity_main)

        // Initialize calendar components
        initializeCalendar()
        setupBottomNavigation()
        setupFloatingActionButton()
        setupTimerButtons()

    }

    private fun initializeCalendar() {
        calendar = Calendar.getInstance()
        dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        currentMonthText = findViewById(R.id.currentMonth)
        prevMonthText = findViewById(R.id.prevMonthText)
        nextMonthText = findViewById(R.id.nextMonthText)
        prevMonthButton = findViewById(R.id.prevMonth)
        nextMonthButton = findViewById(R.id.nextMonth)
        dateContainer = findViewById(R.id.dateContainer)

        prevMonthButton.setOnClickListener { changeMonth(-1) }
        nextMonthButton.setOnClickListener { changeMonth(1) }

        updateCalendar()
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_tasks -> {
                    startActivity(Intent(this, TaskActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    showProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFloatingActionButton() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java).apply {
                putExtra("NEW_TASK", true)
            }
            startActivity(intent)
        }
    }

    private fun setupTimerButtons() {
        findViewById<Button>(R.id.pomodoro25).setOnClickListener { startTimer(25) }
        findViewById<Button>(R.id.pomodoro50).setOnClickListener { startTimer(50) }
        findViewById<Button>(R.id.customTimer).setOnClickListener { showCustomTimerDialog() }
    }



    private fun updateCalendar() {
        currentMonthText.text = monthFormatter.format(calendar.time)

        val prevCalendar = calendar.clone() as Calendar
        prevCalendar.add(Calendar.MONTH, -1)
        prevMonthText.text = SimpleDateFormat("MMM", Locale.getDefault()).format(prevCalendar.time)

        val nextCalendar = calendar.clone() as Calendar
        nextCalendar.add(Calendar.MONTH, 1)
        nextMonthText.text = SimpleDateFormat("MMM", Locale.getDefault()).format(nextCalendar.time)

        val today = Calendar.getInstance()
        val isCurrentMonth = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)

        dateContainer.removeAllViews()

        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        val displayStart = firstDayOfMonth.clone() as Calendar
        displayStart.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 1)

        for (i in 0 until 35) {
            val dayCalendar = displayStart.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_MONTH, i)

            val dateKey = dateFormatter.format(dayCalendar.time)
            val hasEvents = events.containsKey(dateKey)
            val isToday = isCurrentMonth && dayCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            val dateCard = LayoutInflater.from(this).inflate(R.layout.item_date_card, dateContainer, false) as MaterialCardView
            val dayText = dateCard.findViewById<TextView>(R.id.dayText)
            val dayNameText = dateCard.findViewById<TextView>(R.id.dayNameText)
            val eventIndicator = dateCard.findViewById<View>(R.id.eventIndicator)

            dayText.text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString()
            dayNameText.text = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCalendar.time).take(3)

            when {
                isToday -> {
                    dateCard.setCardBackgroundColor(Color.parseColor("#573376"))
                    dayText.setTextColor(Color.WHITE)
                    dayNameText.setTextColor(Color.WHITE)
                    if (selectedDatePosition == -1) {
                        selectedDatePosition = i
                        updateEventsDisplay(dayCalendar)
                    }
                }
                i == selectedDatePosition -> {
                    dateCard.setCardBackgroundColor(Color.parseColor("#B3A1DE"))
                    dayText.setTextColor(Color.WHITE)
                    dayNameText.setTextColor(Color.WHITE)
                }
                else -> {
                    dateCard.setCardBackgroundColor(Color.parseColor("#F8F8F8"))
                    dayText.setTextColor(if (hasEvents) Color.parseColor("#573376") else Color.parseColor("#717171"))
                    dayNameText.setTextColor(if (hasEvents) Color.parseColor("#573376") else Color.parseColor("#80000000"))
                }
            }

            eventIndicator.visibility = if (hasEvents) View.VISIBLE else View.GONE

            dateCard.setOnClickListener {
                selectedDatePosition = i
                updateCalendar()
                updateEventsDisplay(dayCalendar)
            }

            dateContainer.addView(dateCard)
        }
    }

    private fun changeMonth(monthsToAdd: Int) {
        calendar.add(Calendar.MONTH, monthsToAdd)
        selectedDatePosition = -1
        updateCalendar()
    }

    private fun updateEventsDisplay(dateCalendar: Calendar) {
        val dateKey = dateFormatter.format(dateCalendar.time)
        val dateEvents = events[dateKey] ?: emptyList()

        findViewById<ConstraintLayout>(R.id.eventsContainer).isVisible = dateEvents.isNotEmpty()
        findViewById<TextView>(R.id.noEventsText).isVisible = dateEvents.isEmpty()

        val eventsLayout = findViewById<LinearLayout>(R.id.eventsList)
        eventsLayout.removeAllViews()

        dateEvents.forEach { event ->
            val eventView = LayoutInflater.from(this).inflate(R.layout.item_event, eventsLayout, false).apply {
                findViewById<TextView>(R.id.eventTitle).text = event.title
                findViewById<TextView>(R.id.eventLocation).text = event.location
                findViewById<TextView>(R.id.eventTime).text = event.time
                findViewById<MaterialCardView>(R.id.eventCard).setCardBackgroundColor(Color.parseColor(event.color))
                setOnClickListener { openTaskDetails(event.title, event.location, event.time) }
            }
            eventsLayout.addView(eventView)
        }

        val progress = when {
            dateEvents.isEmpty() -> 0
            else -> (dateEvents.count { it.title.contains("Completed") } * 100 / dateEvents.size)
        }
        findViewById<ProgressBar>(R.id.circularProgressBar).progress = progress
        findViewById<TextView>(R.id.progressText).text = "$progress%"

        val todayFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        findViewById<TextView>(R.id.todaySummaryLabel).text =
            if (isToday(dateCalendar)) "Today's Summary"
            else "${todayFormatter.format(dateCalendar.time)} Summary"
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    private fun openTaskDetails(title: String, location: String, time: String) {
        startActivity(Intent(this, TaskActivity::class.java).apply {
            putExtra("TASK_TITLE", title)
            putExtra("TASK_LOCATION", location)
            putExtra("TASK_TIME", time)
        })
    }

    private fun startTimer(minutes: Int) {
        Toast.makeText(this, "Timer started for $minutes minutes", Toast.LENGTH_SHORT).show()
    }

    private fun showCustomTimerDialog() {
        Toast.makeText(this, "Custom timer selected", Toast.LENGTH_SHORT).show()
    }

    private fun showProfile() {
        lifecycleScope.launch {
            userPreferences.userEmailFlow.collect { email ->
                startActivity(Intent(this@MainActivity, settings::class.java).apply {
                    putExtra("USER_EMAIL", email)
                })
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun logout() {
        lifecycleScope.launch {
            userPreferences.clearLoginData()
            redirectToLogin()
        }
    }

    data class Event(
        val title: String,
        val location: String,
        val time: String,
        val color: String
    )
}