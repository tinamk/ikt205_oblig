package com.example.applikasjons_avokadoene.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.adapters.StudentAdapter
import com.example.applikasjons_avokadoene.models.Student
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.firestore.ktx.toObject

/**
 * StudentListActivity displays a list of all students in the database.
 * It allows users to add, edit, delete students and add grades to students.
 */
class StudentListActivity : AppCompatActivity() {
    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private val studentList = mutableListOf<Student>() // Mutable list to store students
    private lateinit var btnAddStudent: Button

    companion object {
        // Constants for passing data between activities using Intent
        const val EXTRA_STUDENT_ID = "STUDENT_ID"
        const val EXTRA_STUDENT_NAME = "STUDENT_NAME"
        const val EXTRA_STUDENT_ID_NUMBER = "STUDENT_ID_NUMBER"
        const val EXTRA_STUDENT_EMAIL = "STUDENT_EMAIL"
        const val EXTRA_STUDENT_PHONE = "STUDENT_PHONE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)
        title = getString(R.string.student_list)

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAddStudent = findViewById(R.id.btnAddStudent)

        // Initialize adapter with click handlers for different actions
        studentAdapter = StudentAdapter(studentList, ::onEditClick, ::onDeleteClick, ::onAddGradeClick)
        recyclerView.adapter = studentAdapter

        // Set up add student button click listener
        btnAddStudent.setOnClickListener {
            val intent = Intent(this, AddEditStudentActivity::class.java)
            startActivity(intent)
        }

        // Load students from Firebase when the activity is created
        loadStudentsFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity (e.g., after adding/editing a student)
        loadStudentsFromFirebase()
    }

    /**
     * Loads all students from Firebase Firestore database
     * Updates the RecyclerView with the loaded data
     */
    private fun loadStudentsFromFirebase() {
        FirebaseUtil.getStudentsCollection()
            .get()
            .addOnSuccessListener { documents ->
                studentList.clear()
                for (document in documents) {
                    val student = document.toObject<Student>()
                    student.id = document.id
                    studentList.add(student)
                }
                studentAdapter.updateStudents(studentList)
            }
            .addOnFailureListener { e ->
                // Show a notification message (Toast) if there's an error loading students
                // Toast messages are small pop-up notifications that appear at the bottom of the screen
                // and automatically disappear after a short time
                Toast.makeText(this, getString(R.string.error_loading_students, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handles the edit button click for a student
     * Opens the AddEditStudentActivity with the student's data
     */
    private fun onEditClick(student: Student) {
        val intent = Intent(this, AddEditStudentActivity::class.java)
        // Pass student data to the edit activity using Intent extras
        intent.putExtra(EXTRA_STUDENT_ID, student.id)
        intent.putExtra(EXTRA_STUDENT_NAME, student.name)
        intent.putExtra(EXTRA_STUDENT_ID_NUMBER, student.studentId)
        intent.putExtra(EXTRA_STUDENT_EMAIL, student.email)
        intent.putExtra(EXTRA_STUDENT_PHONE, student.phone)
        startActivity(intent)
    }

    /**
     * Handles the delete button click for a student
     * Shows a confirmation dialog before deleting
     */
    private fun onDeleteClick(student: Student) {
        // Show confirmation dialog using AlertDialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_student))
            .setMessage(getString(R.string.confirm_delete_student, student.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteStudentAndGrades(student)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Deletes a student and all their grades from Firebase
     * Uses a batch operation to ensure all related data is deleted
     */
    private fun deleteStudentAndGrades(student: Student) {
        // First delete all grades for this student
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("studentId", student.id)
            .get()
            .addOnSuccessListener { gradeDocuments ->
                // Create a batch to delete all grades at once
                val batch = FirebaseUtil.db.batch()
                for (gradeDoc in gradeDocuments) {
                    batch.delete(gradeDoc.reference)
                }
                
                // Execute the batch delete
                batch.commit()
                    .addOnSuccessListener {
                        // Now delete the student
                        FirebaseUtil.getStudentsCollection().document(student.id)
                            .delete()
                            .addOnSuccessListener {
                                // Show a success notification message (Toast)
                                Toast.makeText(this, getString(R.string.student_and_grades_deleted), Toast.LENGTH_SHORT).show()
                                // Remove from local list and update adapter
                                studentList.remove(student)
                                studentAdapter.updateStudents(studentList)
                            }
                            .addOnFailureListener { e ->
                                // Show an error notification if student deletion fails
                                Toast.makeText(this, getString(R.string.error_deleting_student, e.message), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Show an error notification if grades deletion fails
                        Toast.makeText(this, getString(R.string.error_deleting_grades, e.message), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                // Show an error notification if finding grades fails
                Toast.makeText(this, getString(R.string.error_finding_grades, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handles the add grade button click for a student
     * Opens the AddGradeActivity with the student's ID and name
     */
    private fun onAddGradeClick(student: Student) {
        val intent = Intent(this, AddGradeActivity::class.java)
        intent.putExtra(EXTRA_STUDENT_ID, student.id)
        intent.putExtra(EXTRA_STUDENT_NAME, student.name)
        startActivity(intent)
    }
}
