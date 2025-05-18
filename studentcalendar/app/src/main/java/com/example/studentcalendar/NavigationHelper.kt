package com.example.studentcalendar

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHelper {
    fun setupBottomNavigation(
        context: Context,
        bottomNavigationView: BottomNavigationView,
        selectedItemId: Int
    ) {
        bottomNavigationView.selectedItemId = selectedItemId

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_calendar -> {
                    val intent = Intent(context, studytimer::class.java)
                    context.startActivity(intent)
                    true
                }
                R.id.nav_tasks -> {
                    val intent = Intent(context, TaskActivity::class.java)
                    context.startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(context, settings::class.java)
                    context.startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}