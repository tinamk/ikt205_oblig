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
import com.example.applikasjons_avokadoene.adapters.StudentInCourseAdapter
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.models.Student
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.firestore.ktx.toObject
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

// Course detail activity
class CourseDetailActivity : AppCompatActivity() {

    private lateinit var textViewCourseTitle: TextView
    private lateinit var textViewCourseName: TextView
    private lateinit var textViewCourseCode: TextView
    private lateinit var textViewInstructor: TextView
    private lateinit var textViewStudentsEnrolled: TextView
    private lateinit var textViewAverageGrade: TextView
    private lateinit var textViewGradeDistribution: TextView
    private lateinit var recyclerViewGrades: RecyclerView
    private lateinit var recyclerViewStudents: RecyclerView
    private lateinit var textViewStudentsListTitle: TextView
    private lateinit var progressBar: ProgressBar
    
    private var courseId: String? = null
    private var courseName: String? = null
    
    private val gradesList = mutableListOf<Grade>()
    private val studentsList = mutableListOf<Student>()
    private lateinit var gradeAdapter: GradeAdapter
    private lateinit var studentAdapter: StudentInCourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)
        
        // Add back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize views
        textViewCourseTitle = findViewById(R.id.textCourseTitle)
        textViewCourseName = findViewById(R.id.textCourseName)
        textViewCourseCode = findViewById(R.id.textCourseCode)
        textViewInstructor = findViewById(R.id.textInstructor)
        textViewStudentsEnrolled = findViewById(R.id.textStudentsEnrolled)
        textViewAverageGrade = findViewById(R.id.textAverageGrade)
        textViewGradeDistribution = findViewById(R.id.textGradeDistribution)
        recyclerViewGrades = findViewById(R.id.recyclerViewGrades)
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents)
        textViewStudentsListTitle = findViewById(R.id.textViewStudentsListTitle)
        progressBar = findViewById(R.id.progressBar)
        
        // Get data from intent
        courseId = intent.getStringExtra(CourseListActivity.EXTRA_COURSE_ID)
        courseName = intent.getStringExtra(CourseListActivity.EXTRA_COURSE_NAME)
        
        // Set title
        title = courseName ?: "Course Details"
        textViewCourseTitle.text = "Details for $courseName"
        
        // Set up RecyclerViews
        recyclerViewGrades.layoutManager = LinearLayoutManager(this)
        gradeAdapter = GradeAdapter(gradesList)
        recyclerViewGrades.adapter = gradeAdapter
        
        recyclerViewStudents.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentInCourseAdapter(studentsList)
        recyclerViewStudents.adapter = studentAdapter
        
        // Load course data and grades
        if (courseId != null) {
            loadCourseData()
            loadCourseGrades()
            loadStudentsInCourse()
        } else {
            Toast.makeText(this, "Error: No course ID provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Load course data from Firestore
    private fun loadCourseData() {
        progressBar.visibility = View.VISIBLE
        
        FirebaseUtil.getCoursesCollection().document(courseId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val course = document.toObject<Course>()
                    course?.let {
                        // Update UI with course data
                        textViewCourseName.text = "Course Name: ${it.name}"
                        textViewCourseCode.text = "Course Code: ${it.code}"
                        textViewInstructor.text = "Instructor: ${it.instructor}"
                        textViewStudentsEnrolled.text = "Enrolled Students: ${it.studentsEnrolled}"
                        textViewAverageGrade.text = "Average Grade: ${it.averageGrade}"
                    }
                } else {
                    Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading course: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    // Load course grades from Firestore
    private fun loadCourseGrades() {
        progressBar.visibility = View.VISIBLE
        
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                gradesList.clear()
                
                if (documents.isEmpty) {
                    textViewGradeDistribution.text = "No grades found for this course"
                } else {
                    // Count of grades for distribution
                    val gradeCounts = HashMap<String, Int>()
                    
                    for (document in documents) {
                        val grade = document.toObject<Grade>()
                        grade.id = document.id
                        gradesList.add(grade)
                        
                        // Count for distribution
                        val letterGrade = grade.grade
                        gradeCounts[letterGrade] = (gradeCounts[letterGrade] ?: 0) + 1
                    }
                    
                    // Create distribution text
                    val distributionText = StringBuilder("Grade Distribution:\n")
                    for (grade in listOf("A", "B", "C", "D", "E", "F")) {
                        val count = gradeCounts[grade] ?: 0
                        distributionText.append("$grade: $count\n")
                    }
                    
                    textViewGradeDistribution.text = distributionText.toString()
                    
                    // Sort grades by student name
                    gradesList.sortBy { it.studentName }
                    
                    // Update the adapter
                    gradeAdapter.notifyDataSetChanged()
                }
                
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading grades: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
    
    // Load students enrolled in the course
    private fun loadStudentsInCourse() {
        progressBar.visibility = View.VISIBLE
        
        // First, get all grades for this course to find student IDs
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { gradeDocuments ->
                // Extract unique student IDs
                val studentIds = HashSet<String>()
                
                for (gradeDoc in gradeDocuments) {
                    val data = gradeDoc.data
                    val studentId = data["studentId"] as? String
                    if (!studentId.isNullOrEmpty()) {
                        studentIds.add(studentId)
                    }
                }
                
                if (studentIds.isEmpty()) {
                    textViewStudentsListTitle.text = "No students enrolled in this course"
                    progressBar.visibility = View.GONE
                    return@addOnSuccessListener
                }
                
                // Clear the existing student list
                studentsList.clear()
                
                // Fetch each student's details
                var completedCount = 0
                
                for (studentId in studentIds) {
                    FirebaseUtil.getStudentsCollection().document(studentId)
                        .get()
                        .addOnSuccessListener { studentDoc ->
                            if (studentDoc.exists()) {
                                val data = studentDoc.data
                                if (data != null) {
                                    val student = Student(
                                        id = studentDoc.id,
                                        name = data["name"] as? String ?: "Unknown",
                                        studentId = data["studentId"] as? String ?: "",
                                        email = data["email"] as? String ?: "",
                                        phone = data["phone"] as? String ?: ""
                                    )
                                    studentsList.add(student)
                                }
                            }
                            
                            // Count this student as processed
                            completedCount++
                            
                            // If all students have been processed, update the UI
                            if (completedCount >= studentIds.size) {
                                // Sort student list by name
                                studentsList.sortBy { it.name }
                                
                                // Update UI with student list
                                textViewStudentsListTitle.text = "Students in this course (${studentsList.size})"
                                studentAdapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener { e ->
                            // Count as processed even on failure
                            completedCount++
                            
                            if (completedCount >= studentIds.size) {
                                textViewStudentsListTitle.text = "Students in this course (${studentsList.size})"
                                studentAdapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading students: ${e.message}", Toast.LENGTH_SHORT).show()
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
