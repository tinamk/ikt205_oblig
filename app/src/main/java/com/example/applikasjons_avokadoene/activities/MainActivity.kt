package com.example.applikasjons_avokadoene.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.fragments.CourseFragment

/**
 * Main screen with navigation buttons to Students and Courses sections
 */
class MainActivity : AppCompatActivity() {

    private lateinit var btnManageStudents: Button
    private lateinit var btnManageCourses: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set title
        title = "Teacher Tracker"

        // Initialize UI elements
        btnManageStudents = findViewById(R.id.btn_manage_students)
        btnManageCourses = findViewById(R.id.btn_manage_courses)

        // Set click listeners
        setupClickListeners()

        // Add CourseFragment to the FragmentContainerView
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, CourseFragment())
                .commit()
        }
    }

    /**
     * Set up button click listeners
     */
    private fun setupClickListeners() {
        // Navigate to Students screen
        btnManageStudents.setOnClickListener {
            val intent = Intent(this, StudentListActivity::class.java)
            startActivity(intent)
        }

        // Navigate to Courses screen
        btnManageCourses.setOnClickListener {
            val intent = Intent(this, CourseListActivity::class.java)
            startActivity(intent)
        }
    }
}