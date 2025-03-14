package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Student
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp
import java.util.UUID

// Activity for adding or editing a student
class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var editTextStudentName: EditText
    private lateinit var editTextStudentId: EditText
    private lateinit var textViewStudentIdLabel: TextView
    private lateinit var editTextStudentEmail: EditText
    private lateinit var editTextStudentPhone: EditText
    private lateinit var buttonSaveStudent: Button
    
    private var isEditMode = false
    private var studentId = ""
    private var generatedStudentId = ""

    companion object {
        const val RESULT_STUDENT_ADDED = 100
        const val RESULT_STUDENT_UPDATED = 101
    }

    // Initialize UI elements and set click listener for save button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_student)
        
        // Set title based on mode
        title = if (intent.hasExtra("STUDENT_ID")) "Edit Student" else "Add Student"

        // Initialize views
        editTextStudentName = findViewById(R.id.editTextStudentName)
        editTextStudentId = findViewById(R.id.editTextStudentId)
        textViewStudentIdLabel = findViewById(R.id.textViewStudentIdLabel)
        editTextStudentEmail = findViewById(R.id.editTextStudentEmail)
        editTextStudentPhone = findViewById(R.id.editTextStudentPhone)
        buttonSaveStudent = findViewById(R.id.buttonSaveStudent)

        // Check if we're in edit mode
        if (intent.hasExtra("STUDENT_ID")) {
            isEditMode = true
            studentId = intent.getStringExtra("STUDENT_ID") ?: ""
            editTextStudentName.setText(intent.getStringExtra("STUDENT_NAME"))
            
            // Set the student ID field as non-editable but visible in edit mode
            val studentIdNumber = intent.getStringExtra("STUDENT_ID_NUMBER") ?: ""
            editTextStudentId.setText(studentIdNumber)
            editTextStudentId.isEnabled = false
            // Update the label to indicate it's read-only
            textViewStudentIdLabel.setText(R.string.student_id_readonly)
            
            editTextStudentEmail.setText(intent.getStringExtra("STUDENT_EMAIL"))
            editTextStudentPhone.setText(intent.getStringExtra("STUDENT_PHONE"))
        } else {
            // In add mode, generate a student ID and make the field non-visible
            generatedStudentId = generateStudentId()
            editTextStudentId.setText(generatedStudentId)
            editTextStudentId.visibility = View.GONE
            textViewStudentIdLabel.visibility = View.GONE
        }

        buttonSaveStudent.setOnClickListener {
            saveStudent()
        }
    }

    // Generate a unique student ID
    private fun generateStudentId(): String {
        // Generate a unique ID format like "STU-" followed by a unique number or UUID
        return "STU-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
    }

    // Save student to Firebase
    private fun saveStudent() {
        val name = editTextStudentName.text.toString().trim()
        val idNumber = if (isEditMode) editTextStudentId.text.toString().trim() else generatedStudentId
        val email = editTextStudentEmail.text.toString().trim()
        val phone = editTextStudentPhone.text.toString().trim()
        
        // Validate input
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_student_required_fields), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create student object
        val student = Student(
            id = if (isEditMode) studentId else "",
            name = name,
            studentId = idNumber,
            email = email,
            phone = phone,
            createdAt = if (isEditMode) null else Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        
        if (isEditMode) {
            // Update existing student
            FirebaseUtil.getStudentsCollection().document(studentId)
                .update(student.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.student_updated_successfully), Toast.LENGTH_SHORT).show()
                    
                    // Set result to indicate student was updated
                    setResult(RESULT_STUDENT_UPDATED)
                    finish() // Return to previous screen
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.error_updating_student, e.message), Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new student
            FirebaseUtil.getStudentsCollection()
                .add(student.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.student_added_successfully), Toast.LENGTH_SHORT).show()
                    
                    // Set result to indicate student was added
                    setResult(RESULT_STUDENT_ADDED)
                    finish() // Return to previous screen
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.error_adding_student, e.message), Toast.LENGTH_SHORT).show()
                }
        }
    }
}
