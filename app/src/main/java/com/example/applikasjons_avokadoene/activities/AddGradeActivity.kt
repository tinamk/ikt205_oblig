package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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

// Activity for adding a grade
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


    // Initialize UI elements and set click listener for save button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grade)
        title = getString(R.string.add_grade)
        
        // Add back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        
        // Log received intent data
        Log.d("AddGradeActivity", "Received intent data: StudentID=$studentId, StudentName=$studentName, CourseID=$courseId, CourseName=$courseName")
        
        // Setup UI based on what was passed in the intent
        setupUIForSelection()
        
        // Setup button listeners
        buttonSaveGrade.setOnClickListener {
            saveGrade()
        }
        
        // Set up TextWatchers for automatic matching
        setupTextWatchers()
        
        // Set up selection buttons
        setupButtons()
        
        // Disable save button initially - will be enabled when validations pass
        buttonSaveGrade.isEnabled = false
        
        // Load data if needed
        if (studentId == null) {
            loadStudents()
        }
        
        if (courseId == null) {
            loadCourses()
        }
        
        // Add text change listener for grade validation
        editTextGrade.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSaveButtonState()
            }
        })
    }

    // Setup UI based on what was passed
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
            editTextStudentId.visibility = View.VISIBLE
            editTextStudentId.isEnabled = true
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
            editTextCourseCode.visibility = View.VISIBLE
            editTextCourseCode.isEnabled = true
            spinnerCourses.visibility = View.VISIBLE
            buttonSelectCourse.visibility = View.VISIBLE
        }
    }
    // Load students from Firestore
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

    // Load courses from Firestore
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

    // Save grade to Firestore
    private fun saveGrade() {
        // Check if we have required data
        if (studentId == null || courseId == null) {
            Log.e("AddGradeActivity", "Missing required data: studentId=$studentId, courseId=$courseId")
            Toast.makeText(this, getString(R.string.error_select_student_course), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get the grade letter
        val gradeLetter = editTextGrade.text.toString().trim().uppercase()
        
        // Validate the grade
        if (gradeLetter !in listOf("A", "B", "C", "D", "E", "F")) {
            Log.e("AddGradeActivity", "Invalid grade entered: '$gradeLetter'")
            Toast.makeText(this, getString(R.string.error_invalid_grade), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show progress
        progressBar.visibility = View.VISIBLE
        
        // Convert letter grade to score (used for calculations)
        val gradeScore = when (gradeLetter) {
            "A" -> 5.0
            "B" -> 4.0
            "C" -> 3.0
            "D" -> 2.0
            "E" -> 1.0
            "F" -> 0.0
            else -> 0.0
        }
        
        // Check if a grade already exists for this student/course
        checkExistingGrade(gradeLetter, gradeScore)
    }
    
    private fun checkExistingGrade(gradeLetter: String, gradeScore: Double) {
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No existing grade, add a new one
                    Log.d("AddGradeActivity", "No existing grade found, adding new grade")
                    addNewGrade(gradeLetter, gradeScore)
                } else {
                    // A grade already exists
                    val existingGrade = documents.documents[0].toObject<Grade>()
                    existingGrade?.let {
                        // Only update if the new grade is higher
                        if (gradeScore > it.score) {
                            Log.d("AddGradeActivity", "Existing grade found (${it.grade}), new grade is higher, updating")
                            updateGrade(documents.documents[0].id, gradeLetter, gradeScore)
                        } else {
                            Log.d("AddGradeActivity", "Existing grade (${it.grade}) is higher than or equal to new grade ($gradeLetter), not updating")
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, getString(R.string.grade_not_higher), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddGradeActivity", "Error checking existing grades: ${e.message}")
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_checking_grades, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    // Add a new grade
    private fun addNewGrade(gradeLetter: String, gradeScore: Double) {
        val grade = Grade(
            studentId = studentId!!,
            studentName = studentName ?: "Unknown Student",
            courseId = courseId!!,
            courseName = courseName ?: "Unknown Course",
            grade = gradeLetter,
            score = gradeScore,
            date = Timestamp.now()
        )
        
        Log.d("AddGradeActivity", "Saving new grade: $gradeLetter for student: $studentName in course: $courseName")

        // Add grade to Firestore
        FirebaseUtil.getGradesCollection()
            .add(grade.toMap())
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.grade_added_success), Toast.LENGTH_SHORT).show()
                
                // Prepare result data
                val resultIntent = Intent()
                resultIntent.putExtra("STUDENT_NAME", studentName)
                resultIntent.putExtra("COURSE_NAME", courseName)
                resultIntent.putExtra("GRADE_LETTER", gradeLetter)
                
                // Set result
                setResult(RESULT_GRADE_ADDED, resultIntent)
                
                // Finish activity
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AddGradeActivity", "Error adding grade: ${e.message}")
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_adding_grade, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    // Update an existing grade
    private fun updateGrade(gradeId: String, gradeLetter: String, gradeScore: Double) {
        val updates = hashMapOf<String, Any>(
            "grade" to gradeLetter,
            "score" to gradeScore,
            "date" to Timestamp.now()
        )
        
        Log.d("AddGradeActivity", "Updating grade to: $gradeLetter for student: $studentName in course: $courseName")
        
        FirebaseUtil.getGradesCollection().document(gradeId)
            .update(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.grade_updated_success), Toast.LENGTH_SHORT).show()
                
                // Prepare result data
                val resultIntent = Intent()
                resultIntent.putExtra("STUDENT_NAME", studentName)
                resultIntent.putExtra("COURSE_NAME", courseName)
                resultIntent.putExtra("GRADE_LETTER", gradeLetter)
                
                // Set result
                setResult(RESULT_GRADE_UPDATED, resultIntent)
                
                // Finish activity
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AddGradeActivity", "Error updating grade: ${e.message}")
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_updating_grade, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    // Helper method to update save button state
    private fun updateSaveButtonState() {
        // Log the state for debugging
        Log.d("AddGradeActivity", "Validating button state - StudentID: $studentId, CourseID: $courseId, Grade: ${editTextGrade.text}")
        
        // Enable save button only if we have both a student and a course selected, and a valid grade
        val gradeText = editTextGrade.text.toString().trim().uppercase()
        val isGradeValid = gradeText.isNotEmpty() && "ABCDEF".contains(gradeText)
        
        buttonSaveGrade.isEnabled = studentId != null && courseId != null && isGradeValid
        
        if (buttonSaveGrade.isEnabled) {
            Log.d("AddGradeActivity", "Save button ENABLED")
        } else {
            Log.d("AddGradeActivity", "Save button DISABLED")
        }
    }

    // Setup TextWatchers for automatic matching when typing
    private fun setupTextWatchers() {
        // TextWatcher for student name input
        editTextStudentId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Only try to match if we haven't already selected a student
                if (studentId == null) {
                    val searchText = s.toString().trim()
                    if (searchText.isNotEmpty() && studentList.isNotEmpty()) {
                        // Try to find matching student
                        val matchingStudent = studentList.find { 
                            it.name.contains(searchText, ignoreCase = true) 
                        }
                        
                        if (matchingStudent != null) {
                            Log.d("AddGradeActivity", "Auto-matched student: ${matchingStudent.name}")
                            // Select the matching student in spinner
                            val position = studentList.indexOf(matchingStudent)
                            if (position >= 0) {
                                spinnerStudents.setSelection(position)
                            }
                        }
                    }
                }
            }
        })
        
        // TextWatcher for course name input
        editTextCourseCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Only try to match if we haven't already selected a course
                if (courseId == null) {
                    val searchText = s.toString().trim()
                    if (searchText.isNotEmpty() && courseList.isNotEmpty()) {
                        // Try to find matching course
                        val matchingCourse = courseList.find { 
                            it.name.contains(searchText, ignoreCase = true) || 
                            it.code.contains(searchText, ignoreCase = true)
                        }
                        
                        if (matchingCourse != null) {
                            Log.d("AddGradeActivity", "Auto-matched course: ${matchingCourse.name}")
                            // Select the matching course in spinner
                            val position = courseList.indexOf(matchingCourse)
                            if (position >= 0) {
                                spinnerCourses.setSelection(position)
                            }
                        }
                    }
                }
            }
        })
    }
    
    // Set up selection buttons
    private fun setupButtons() {
        // Button for selecting student
        buttonSelectStudent.setOnClickListener {
            if (spinnerStudents.selectedItemPosition >= 0 && spinnerStudents.selectedItemPosition < studentList.size) {
                val selectedStudent = studentList[spinnerStudents.selectedItemPosition]
                studentId = selectedStudent.id
                studentName = selectedStudent.name
                
                // Log a clear message when student is selected
                Log.d("AddGradeActivity", "Student selected from spinner: ${selectedStudent.name} with ID: ${selectedStudent.id}")
                
                // Update UI to show selected student
                editTextStudentId.setText(selectedStudent.name)
                editTextStudentId.isEnabled = false
                
                // Check if we also have selected a course, and enable save button if both are selected
                updateSaveButtonState()
            }
        }
        
        // Button for selecting course
        buttonSelectCourse.setOnClickListener {
            if (spinnerCourses.selectedItemPosition >= 0 && spinnerCourses.selectedItemPosition < courseList.size) {
                val selectedCourse = courseList[spinnerCourses.selectedItemPosition]
                courseId = selectedCourse.id
                courseName = selectedCourse.name
                
                // Log a clear message when course is selected
                Log.d("AddGradeActivity", "Course selected from spinner: ${selectedCourse.name} with ID: ${selectedCourse.id}")
                
                // Update UI to show selected course
                editTextCourseCode.setText(selectedCourse.name)
                editTextCourseCode.isEnabled = false
                
                // Check if we also have selected a student, and enable save button if both are selected
                updateSaveButtonState()
            }
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
