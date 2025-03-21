package com.example.applikasjons_avokadoene.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.fragments.CourseFragment
import com.example.applikasjons_avokadoene.utils.FirebaseUtil

// MainActivity
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
        
        // Test Firebase connection
        testFirebaseConnection()
    }

    // Set up click listeners for buttons
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
    
   // Test Firebase connection
    private fun testFirebaseConnection() {
        // Check if we can get a collection reference
        val coursesCollection = FirebaseUtil.getCoursesCollection()
        coursesCollection.limit(1).get()
            .addOnSuccessListener {
                // Connection successful
                android.util.Log.d("MainActivity", "Firebase connection test: SUCCESS")
            }
            .addOnFailureListener { e ->
                // Connection failed
                android.util.Log.e("MainActivity", "Firebase connection test: FAILED - ${e.message}")
                Toast.makeText(this, "Firebase tilkobling feilet: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}