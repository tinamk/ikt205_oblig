package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.models.Student
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject

class AddGradeActivity : AppCompatActivity() {

    private lateinit var editTextStudentId: EditText
    private lateinit var spinnerStudents: Spinner
    private lateinit var buttonSelectStudent: Button
    private lateinit var editTextCourseCode: EditText
    private lateinit var spinnerCourses: Spinner
    private lateinit var buttonSelectCourse: Button
    private lateinit var editTextGrade: EditText
    private lateinit var buttonSaveGrade: Button
    private lateinit var progressBar: ProgressBar
    
    private var studentId: String? = null
    private var studentName: String? = null
    private var courseId: String? = null
    private var courseName: String? = null
    
    private val studentList = mutableListOf<Student>()
    private val courseList = mutableListOf<Course>()
    
    companion object {
        const val RESULT_GRADE_ADDED = 100
        const val RESULT_GRADE_UPDATED = 101
        const val RESULT_NO_CHANGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grade)
        title = getString(R.string.add_grade)

        // Initialize views
        editTextStudentId = findViewById(R.id.editTextStudentId)
        spinnerStudents = findViewById(R.id.spinnerStudents)
        buttonSelectStudent = findViewById(R.id.buttonSelectStudent)
        editTextCourseCode = findViewById(R.id.editTextCourseCode)
        spinnerCourses = findViewById(R.id.spinnerCourses)
        buttonSelectCourse = findViewById(R.id.buttonSelectCourse)
        editTextGrade = findViewById(R.id.editTextGrade)
        buttonSaveGrade = findViewById(R.id.buttonSaveGrade)
        progressBar = findViewById(R.id.progressBarAddGrade)

        // Get data from intent
        studentId = intent.getStringExtra("STUDENT_ID")
        studentName = intent.getStringExtra("STUDENT_NAME")
        courseId = intent.getStringExtra("COURSE_ID")
        courseName = intent.getStringExtra("COURSE_NAME")
        
        // Load data based on what's available
        setupUIForSelection()
        
        // Load students if needed
        if (studentId == null) {
            loadStudents()
        }
        
        // Load courses if needed
        if (courseId == null) {
            loadCourses()
        }
        
        // Add text watcher to validate grade input
        editTextGrade.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: android.text.Editable?) {
                val gradeText = s.toString().trim().uppercase()
                if (gradeText.isNotEmpty() && !gradeText.matches(Regex("[A-F]"))) {
                    editTextGrade.error = getString(R.string.error_invalid_grade)
                    buttonSaveGrade.isEnabled = false
                } else {
                    editTextGrade.error = null
                    buttonSaveGrade.isEnabled = true
                }
            }
        })

        // Set up selection buttons
        buttonSelectStudent.setOnClickListener {
            if (spinnerStudents.selectedItemPosition >= 0 && spinnerStudents.selectedItemPosition < studentList.size) {
                val selectedStudent = studentList[spinnerStudents.selectedItemPosition]
                studentId = selectedStudent.id
                studentName = selectedStudent.name
                
                // Update UI to show selected student
                editTextStudentId.setText(selectedStudent.name)
                editTextStudentId.isEnabled = false
                spinnerStudents.visibility = View.GONE
                buttonSelectStudent.visibility = View.GONE
            }
        }
        
        buttonSelectCourse.setOnClickListener {
            if (spinnerCourses.selectedItemPosition >= 0 && spinnerCourses.selectedItemPosition < courseList.size) {
                val selectedCourse = courseList[spinnerCourses.selectedItemPosition]
                courseId = selectedCourse.id
                courseName = selectedCourse.name
                
                // Update UI to show selected course
                editTextCourseCode.setText(selectedCourse.name)
                editTextCourseCode.isEnabled = false
                spinnerCourses.visibility = View.GONE
                buttonSelectCourse.visibility = View.GONE
            }
        }

        // Set up save button
        buttonSaveGrade.setOnClickListener {
            saveGrade()
        }
    }
    
    private fun setupUIForSelection() {
        // Configure student selection UI
        if (studentId != null && studentName != null) {
            // Student already selected, show in EditText
            editTextStudentId.setText(studentName)
            editTextStudentId.isEnabled = false
            spinnerStudents.visibility = View.GONE
            buttonSelectStudent.visibility = View.GONE
        } else {
            // Need to select student, show spinner
            editTextStudentId.visibility = View.GONE
            spinnerStudents.visibility = View.VISIBLE
            buttonSelectStudent.visibility = View.VISIBLE
        }
        
        // Configure course selection UI
        if (courseId != null && courseName != null) {
            // Course already selected, show in EditText
            editTextCourseCode.setText(courseName)
            editTextCourseCode.isEnabled = false
            spinnerCourses.visibility = View.GONE
            buttonSelectCourse.visibility = View.GONE
        } else {
            // Need to select course, show spinner
            editTextCourseCode.visibility = View.GONE
            spinnerCourses.visibility = View.VISIBLE
            buttonSelectCourse.visibility = View.VISIBLE
        }
    }
    
    private fun loadStudents() {
        progressBar.visibility = View.VISIBLE
        
        FirebaseUtil.getStudentsCollection()
            .get()
            .addOnSuccessListener { documents ->
                studentList.clear()
                
                for (document in documents) {
                    val student = document.toObject<Student>()
                    student.id = document.id
                    studentList.add(student)
                }
                
                // Set up spinner with student names
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    studentList.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerStudents.adapter = adapter
                
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading students: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadCourses() {
        progressBar.visibility = View.VISIBLE
        
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                courseList.clear()
                
                for (document in documents) {
                    val course = document.toObject<Course>()
                    course.id = document.id
                    courseList.add(course)
                }
                
                // Set up spinner with course names
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    courseList.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCourses.adapter = adapter
                
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading courses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGrade() {
        val gradeText = editTextGrade.text.toString().trim().uppercase()
        
        // Validate grade
        if (gradeText.isEmpty() || !gradeText.matches(Regex("[A-F]"))) {
            Toast.makeText(this, getString(R.string.error_invalid_grade), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if we have student and course
        if (studentId == null || courseId == null) {
            Toast.makeText(this, getString(R.string.error_select_student_course), Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        // First check if the student already has a grade for this course
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                val newScore = convertGradeToScore(gradeText)
                
                if (documents.isEmpty) {
                    // No existing grade, add new one
                    addNewGrade(newScore, gradeText)
                } else {
                    // Existing grade found - check if new grade is higher
                    val existingGrade = documents.documents[0].toObject<Grade>()
                    val existingScore = existingGrade?.score ?: 0.0
                    
                    if (newScore > existingScore) {
                        // New grade is higher, update existing grade
                        val gradeId = documents.documents[0].id
                        updateExistingGrade(gradeId, newScore, gradeText)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.grade_not_higher), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_NO_CHANGE)
                        finish()
                    }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_checking_grades, e.message), Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun addNewGrade(score: Double, gradeText: String) {
        // Create grade object
        val grade = Grade(
            studentId = studentId!!,
            studentName = studentName!!,
            courseId = courseId!!,
            courseName = courseName!!,
            grade = gradeText,
            score = score,
            date = Timestamp.now()
        )
        
        // Save to Firestore
        FirebaseUtil.getGradesCollection()
            .add(grade.toMap())
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.grade_added_success), Toast.LENGTH_SHORT).show()
                
                // Create result intent with information about the added grade
                val resultIntent = Intent()
                resultIntent.putExtra("GRADE_LETTER", gradeText)
                resultIntent.putExtra("STUDENT_NAME", studentName)
                resultIntent.putExtra("COURSE_NAME", courseName)
                
                // Set result to indicate grade was added
                setResult(RESULT_GRADE_ADDED, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_adding_grade, e.message), Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateExistingGrade(gradeId: String, score: Double, gradeText: String) {
        val updates = mapOf(
            "grade" to gradeText,
            "score" to score,
            "date" to Timestamp.now()
        )
        
        FirebaseUtil.getGradesCollection().document(gradeId)
            .update(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.grade_updated_success), Toast.LENGTH_SHORT).show()
                
                // Create result intent with information about the updated grade
                val resultIntent = Intent()
                resultIntent.putExtra("GRADE_LETTER", gradeText)
                resultIntent.putExtra("STUDENT_NAME", studentName)
                resultIntent.putExtra("COURSE_NAME", courseName)
                
                // Set result to indicate grade was updated
                setResult(RESULT_GRADE_UPDATED, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_updating_grade, e.message), Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun convertGradeToScore(grade: String): Double {
        return when (grade) {
            "A" -> 5.0
            "B" -> 4.0
            "C" -> 3.0
            "D" -> 2.0
            "E" -> 1.0
            "F" -> 0.0
            else -> 0.0
        }
    }
}
