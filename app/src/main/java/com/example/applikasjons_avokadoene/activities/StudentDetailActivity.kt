package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.adapters.GradeAdapter
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.models.Student
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.firestore.ktx.toObject

// Student detail activity
class StudentDetailActivity : AppCompatActivity() {

    private lateinit var textViewStudentName: TextView
    private lateinit var textViewStudentId: TextView
    private lateinit var textViewStudentEmail: TextView
    private lateinit var textViewStudentPhone: TextView
    private lateinit var textViewGradesTitle: TextView
    private lateinit var recyclerViewGrades: RecyclerView
    private lateinit var progressBar: ProgressBar
    
    private var studentId: String? = null
    private var studentName: String? = null
    
    private val gradesList = mutableListOf<Grade>()
    private lateinit var gradeAdapter: GradeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)
        
        // Add back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize views
        textViewStudentName = findViewById(R.id.textViewStudentName)
        textViewStudentId = findViewById(R.id.textViewStudentId)
        textViewStudentEmail = findViewById(R.id.textViewStudentEmail)
        textViewStudentPhone = findViewById(R.id.textViewStudentPhone)
        textViewGradesTitle = findViewById(R.id.textViewGradesTitle)
        recyclerViewGrades = findViewById(R.id.recyclerViewGrades)
        progressBar = findViewById(R.id.progressBar)
        
        // Get student ID from intent
        studentId = intent.getStringExtra("STUDENT_ID")
        studentName = intent.getStringExtra("STUDENT_NAME")
        
        // Set activity title
        title = studentName ?: "Student Details"
        
        // Setup RecyclerView
        recyclerViewGrades.layoutManager = LinearLayoutManager(this)
        gradeAdapter = GradeAdapter(gradesList)
        recyclerViewGrades.adapter = gradeAdapter
        
        // Load student data and grades if we have a student ID
        if (studentId != null) {
            loadStudentData()
            loadStudentGrades()
        } else {
            Toast.makeText(this, "Error: No student ID provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun loadStudentData() {
        progressBar.visibility = View.VISIBLE
        
        // Get student details from Firestore
        FirebaseUtil.getStudentsCollection().document(studentId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Use data map to avoid serialization issues
                    val data = document.data
                    if (data != null) {
                        // Update UI with student data
                        textViewStudentName.text = data["name"] as? String ?: "Unknown"
                        textViewStudentId.text = "Student ID: ${data["studentId"] as? String ?: "N/A"}"
                        textViewStudentEmail.text = "Email: ${data["email"] as? String ?: "N/A"}"
                        textViewStudentPhone.text = "Phone: ${data["phone"] as? String ?: "N/A"}"
                    } else {
                        Toast.makeText(this, "Error: Student data is empty", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading student: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
    
    private fun loadStudentGrades() {
        progressBar.visibility = View.VISIBLE
        
        // Get all grades for this student from Firestore
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { documents ->
                gradesList.clear()
                
                if (documents.isEmpty) {
                    textViewGradesTitle.text = "No grades found for this student"
                } else {
                    textViewGradesTitle.text = "Grades (${documents.size()})"
                    
                    for (document in documents) {
                        try {
                            // Create Grade object manually
                            val data = document.data
                            val grade = Grade(
                                id = document.id,
                                studentId = data["studentId"] as? String ?: "",
                                studentName = data["studentName"] as? String ?: "",
                                courseId = data["courseId"] as? String ?: "",
                                courseName = data["courseName"] as? String ?: "",
                                grade = data["grade"] as? String ?: ""
                            )
                            gradesList.add(grade)
                        } catch (e: Exception) {
                            // Skip this grade if there's an error
                        }
                    }
                    
                    // Sort grades by courseName
                    gradesList.sortBy { it.courseName }
                    
                    // Update adapter
                    gradeAdapter.notifyDataSetChanged()
                }
                
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading grades: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    // Handle back button press
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 