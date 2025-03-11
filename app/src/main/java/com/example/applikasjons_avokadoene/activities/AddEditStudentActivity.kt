package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Student
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp

class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var editTextStudentName: EditText
    private lateinit var editTextStudentId: EditText
    private lateinit var editTextStudentEmail: EditText
    private lateinit var editTextStudentPhone: EditText
    private lateinit var buttonSaveStudent: Button
    
    private var isEditMode = false
    private var studentId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_student)
        
        // Set title based on mode
        title = if (intent.hasExtra("STUDENT_ID")) "Edit Student" else "Add Student"

        // Initialize views
        editTextStudentName = findViewById(R.id.editTextStudentName)
        editTextStudentId = findViewById(R.id.editTextStudentId)
        editTextStudentEmail = findViewById(R.id.editTextStudentEmail)
        editTextStudentPhone = findViewById(R.id.editTextStudentPhone)
        buttonSaveStudent = findViewById(R.id.buttonSaveStudent)

        // Check if we're in edit mode
        if (intent.hasExtra("STUDENT_ID")) {
            isEditMode = true
            studentId = intent.getStringExtra("STUDENT_ID") ?: ""
            editTextStudentName.setText(intent.getStringExtra("STUDENT_NAME"))
            editTextStudentId.setText(intent.getStringExtra("STUDENT_ID_NUMBER"))
            editTextStudentEmail.setText(intent.getStringExtra("STUDENT_EMAIL"))
            editTextStudentPhone.setText(intent.getStringExtra("STUDENT_PHONE"))
        }

        buttonSaveStudent.setOnClickListener {
            saveStudent()
        }
    }

    private fun saveStudent() {
        val name = editTextStudentName.text.toString().trim()
        val idNumber = editTextStudentId.text.toString().trim()
        val email = editTextStudentEmail.text.toString().trim()
        val phone = editTextStudentPhone.text.toString().trim()
        
        // Validate input
        if (name.isEmpty() || idNumber.isEmpty()) {
            Toast.makeText(this, "Name and ID are required", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous screen
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating student: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new student
            FirebaseUtil.getStudentsCollection()
                .add(student.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous screen
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding student: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
