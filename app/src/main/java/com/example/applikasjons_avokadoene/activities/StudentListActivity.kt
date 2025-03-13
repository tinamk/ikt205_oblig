package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.AddEditStudentActivity.Companion.RESULT_STUDENT_ADDED
import com.example.applikasjons_avokadoene.activities.AddEditStudentActivity.Companion.RESULT_STUDENT_UPDATED
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
    private lateinit var editTextSearch: EditText
    private lateinit var progressBar: ProgressBar
    private val allStudents = mutableListOf<Student>() // Store all students for filtering

    companion object {
        // Constants for passing data between activities using Intent
        const val EXTRA_STUDENT_ID = "STUDENT_ID"
        const val EXTRA_STUDENT_NAME = "STUDENT_NAME"
        const val EXTRA_STUDENT_ID_NUMBER = "STUDENT_ID_NUMBER"
        const val EXTRA_STUDENT_EMAIL = "STUDENT_EMAIL"
        const val EXTRA_STUDENT_PHONE = "STUDENT_PHONE"
        
        // Request codes for startActivityForResult
        const val REQUEST_ADD_STUDENT = 1
        const val REQUEST_EDIT_STUDENT = 2
        const val REQUEST_ADD_GRADE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)
        title = getString(R.string.student_list)

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAddStudent = findViewById(R.id.btnAddStudent)
        editTextSearch = findViewById(R.id.editTextSearchStudent)
        progressBar = findViewById(R.id.progressBarStudents)

        // Initialize adapter with click handlers for different actions
        studentAdapter = StudentAdapter(studentList, ::onEditClick, ::onDeleteClick, ::onAddGradeClick)
        recyclerView.adapter = studentAdapter

        // Set up add student button click listener
        btnAddStudent.setOnClickListener {
            val intent = Intent(this, AddEditStudentActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_STUDENT)
        }
        
        // Set up search functionality
        setupSearch()

        // Load students from Firebase when the activity is created
        loadStudentsFromFirebase()
    }

    private fun setupSearch() {
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filterStudents(s.toString())
            }
        })
    }
    
    private fun filterStudents(query: String) {
        val filteredList = if (query.isEmpty()) {
            allStudents
        } else {
            allStudents.filter { student ->
                student.name.contains(query, ignoreCase = true)
            }
        }
        
        studentList.clear()
        studentList.addAll(filteredList)
        studentAdapter.updateStudents(studentList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Handle results from Add/Edit activities
        if (requestCode == REQUEST_ADD_STUDENT && resultCode == RESULT_STUDENT_ADDED) {
            // Student was added successfully
            loadStudentsFromFirebase()
            showSuccessDialog(getString(R.string.student_added_title), getString(R.string.student_added_message))
        } else if (requestCode == REQUEST_EDIT_STUDENT && resultCode == RESULT_STUDENT_UPDATED) {
            // Student was updated successfully
            loadStudentsFromFirebase()
        } else if (requestCode == REQUEST_ADD_GRADE) {
            if (resultCode == AddGradeActivity.RESULT_GRADE_ADDED || 
                resultCode == AddGradeActivity.RESULT_GRADE_UPDATED) {
                // Grade was added or updated - refresh data
                loadStudentsFromFirebase()
                
                // Show confirmation message
                val studentName = data?.getStringExtra("STUDENT_NAME") ?: "Student"
                val courseName = data?.getStringExtra("COURSE_NAME") ?: "course"
                val gradeLetter = data?.getStringExtra("GRADE_LETTER") ?: ""
                
                val message = if (resultCode == AddGradeActivity.RESULT_GRADE_ADDED) {
                    getString(R.string.added_grade_format, gradeLetter, studentName, courseName)
                } else {
                    getString(R.string.updated_grade_format, gradeLetter, studentName, courseName)
                }
                
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Loads all students from Firebase Firestore database
     * Updates the RecyclerView with the loaded data
     */
    private fun loadStudentsFromFirebase() {
        // Show progress bar
        progressBar.visibility = View.VISIBLE
        
        FirebaseUtil.getStudentsCollection()
            .get()
            .addOnSuccessListener { documents ->
                allStudents.clear()
                studentList.clear()
                
                for (document in documents) {
                    val student = document.toObject<Student>()
                    student.id = document.id
                    allStudents.add(student)
                }
                
                // Apply any active filter
                val currentFilter = editTextSearch.text.toString()
                if (currentFilter.isEmpty()) {
                    studentList.addAll(allStudents)
                } else {
                    filterStudents(currentFilter)
                }
                
                studentAdapter.updateStudents(studentList)
                
                // Hide progress bar
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Hide progress bar
                progressBar.visibility = View.GONE
                
                // Show error message
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
        startActivityForResult(intent, REQUEST_EDIT_STUDENT)
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
        // Show progress bar
        progressBar.visibility = View.VISIBLE
        
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
                                // Hide progress bar
                                progressBar.visibility = View.GONE
                                
                                // Show a success notification message (Toast)
                                Toast.makeText(this, getString(R.string.student_and_grades_deleted), Toast.LENGTH_SHORT).show()
                                // Remove from local lists and update adapter
                                allStudents.remove(student)
                                studentList.remove(student)
                                studentAdapter.updateStudents(studentList)
                            }
                            .addOnFailureListener { e ->
                                // Hide progress bar
                                progressBar.visibility = View.GONE
                                
                                // Show an error notification if student deletion fails
                                Toast.makeText(this, getString(R.string.error_deleting_student, e.message), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        // Hide progress bar
                        progressBar.visibility = View.GONE
                        
                        // Show an error notification if grades deletion fails
                        Toast.makeText(this, getString(R.string.error_deleting_grades, e.message), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                // Hide progress bar
                progressBar.visibility = View.GONE
                
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
        startActivityForResult(intent, REQUEST_ADD_GRADE)
    }

    private fun showSuccessDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }
}
