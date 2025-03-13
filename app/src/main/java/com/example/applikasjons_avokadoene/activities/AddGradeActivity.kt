package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject

class AddGradeActivity : AppCompatActivity() {

    private lateinit var editTextStudentId: EditText
    private lateinit var editTextCourseCode: EditText
    private lateinit var editTextGrade: EditText
    private lateinit var buttonSaveGrade: Button
    
    private var studentId: String? = null
    private var studentName: String? = null
    private var courseId: String? = null
    private var courseName: String? = null
    
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
        editTextCourseCode = findViewById(R.id.editTextCourseCode)
        editTextGrade = findViewById(R.id.editTextGrade)
        buttonSaveGrade = findViewById(R.id.buttonSaveGrade)

        // Get data from intent
        studentId = intent.getStringExtra("STUDENT_ID")
        studentName = intent.getStringExtra("STUDENT_NAME")
        courseId = intent.getStringExtra("COURSE_ID")
        courseName = intent.getStringExtra("COURSE_NAME")
        
        // Pre-fill fields if data is available
        if (studentId != null && studentName != null) {
            editTextStudentId.setText(studentName)
            editTextStudentId.isEnabled = false
        }
        
        if (courseId != null && courseName != null) {
            editTextCourseCode.setText(courseName)
            editTextCourseCode.isEnabled = false
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

        // Set up save button
        buttonSaveGrade.setOnClickListener {
            saveGrade()
        }
    }

    private fun saveGrade() {
        val gradeText = editTextGrade.text.toString().trim().uppercase()
        
        // Validate grade
        if (gradeText.isEmpty() || !gradeText.matches(Regex("[A-F]"))) {
            Toast.makeText(this, getString(R.string.error_invalid_grade), Toast.LENGTH_SHORT).show()
            return
        }
        
        // If we don't have student or course ID, we need to look them up
        if (studentId == null || courseId == null) {
            Toast.makeText(this, getString(R.string.error_select_student_course), Toast.LENGTH_SHORT).show()
            return
        }
        
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
                        Toast.makeText(this, getString(R.string.grade_not_higher), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_NO_CHANGE)
                        finish()
                    }
                }
            }
            .addOnFailureListener { e ->
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
