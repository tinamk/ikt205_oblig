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
import com.example.applikasjons_avokadoene.activities.AddEditCourseActivity.Companion.RESULT_COURSE_ADDED
import com.example.applikasjons_avokadoene.activities.AddEditCourseActivity.Companion.RESULT_COURSE_UPDATED
import com.example.applikasjons_avokadoene.adapters.CourseAdapter
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.firestore.ktx.toObject

/**
 * CourseListActivity displays a list of all courses in the database.
 * It allows users to add, edit, delete courses and add grades to courses.
 */
class CourseListActivity : AppCompatActivity() {

    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private val courseList = mutableListOf<Course>()
    private lateinit var btnAddNewCourse: Button
    private lateinit var editTextSearch: EditText
    private val allCourses = mutableListOf<Course>() // Store all courses for filtering
    private lateinit var progressBar: ProgressBar

    companion object {
        // Constants for activity results and intent extras
        const val REQUEST_ADD_COURSE = 1 // Request code for adding a course (used with startActivityForResult)
        const val REQUEST_EDIT_COURSE = 2 // Request code for editing a course
        const val EXTRA_NEW_COURSE = "NEW_COURSE"
        const val EXTRA_COURSE = "COURSE"
        const val EXTRA_COURSE_ID = "COURSE_ID"
        const val EXTRA_COURSE_NAME = "COURSE_NAME"
        const val REQUEST_ADD_GRADE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        title = getString(R.string.course_list)

        // Initialize RecyclerView and other UI components
        recyclerView = findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAddNewCourse = findViewById(R.id.btn_add_new_course)
        editTextSearch = findViewById(R.id.editTextSearchCourse)
        progressBar = findViewById(R.id.progressBarCourses)

        // Initialize adapter with click handlers for different actions
        courseAdapter = CourseAdapter(
            courseList,
            ::editCourse,
            ::deleteCourse,
            ::addCourse,
            ::gradeCourse
        )
        recyclerView.adapter = courseAdapter

        // Load courses from Firebase when the activity is created
        fetchCoursesFromFirestore()

        // Set up add course button click listener
        btnAddNewCourse.setOnClickListener {
            val intent = Intent(this, AddEditCourseActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_COURSE)
        }
        
        // Set up search functionality
        setupSearch()
    }
    
    private fun setupSearch() {
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filterCourses(s.toString())
            }
        })
    }
    
    private fun filterCourses(query: String) {
        val filteredList = if (query.isEmpty()) {
            allCourses
        } else {
            allCourses.filter { course ->
                course.name.contains(query, ignoreCase = true) || 
                course.code.contains(query, ignoreCase = true)
            }
        }
        
        courseList.clear()
        courseList.addAll(filteredList)
        courseAdapter.updateCourseList(courseList)
    }

    /**
     * Fetch courses from Firestore database and update the list
     * This method gets all courses from Firebase and displays them in the RecyclerView
     */
    private fun fetchCoursesFromFirestore() {
        // Show progress bar
        progressBar.visibility = View.VISIBLE
        
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                allCourses.clear() // Clear list before adding new data
                courseList.clear()
                
                for (document in documents) {
                    val course = document.toObject<Course>().apply {
                        id = document.id // Assign Firestore document ID
                    }
                    allCourses.add(course)
                }
                
                // Apply any active filter
                val currentFilter = editTextSearch.text.toString()
                if (currentFilter.isEmpty()) {
                    courseList.addAll(allCourses)
                } else {
                    filterCourses(currentFilter)
                }
                
                courseAdapter.updateCourseList(courseList) // Update the adapter with new data
                
                // Hide progress bar
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Hide progress bar
                progressBar.visibility = View.GONE
                
                // Show error message
                Toast.makeText(this, getString(R.string.error_fetching_courses, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handle result from AddEditCourseActivity
     * This is called when returning from the add/edit course screen
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Refresh data based on result type
        if (requestCode == REQUEST_ADD_COURSE && resultCode == RESULT_COURSE_ADDED) {
            // Course was added
            fetchCoursesFromFirestore()
            showSuccessDialog(getString(R.string.course_added_title), getString(R.string.course_added_message))
        } else if (requestCode == REQUEST_EDIT_COURSE && resultCode == RESULT_COURSE_UPDATED) {
            // Course was updated
            fetchCoursesFromFirestore()
        } else if (requestCode == REQUEST_ADD_GRADE) {
            if (resultCode == AddGradeActivity.RESULT_GRADE_ADDED || 
                resultCode == AddGradeActivity.RESULT_GRADE_UPDATED) {
                // Grade was added or updated, refresh data
                fetchCoursesFromFirestore()
                
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
     * Edit course functionality
     * Opens the AddEditCourseActivity with the course's data
     */
    private fun editCourse(course: Course) {
        val intent = Intent(this, AddEditCourseActivity::class.java)
        intent.putExtra(EXTRA_COURSE, course)
        startActivityForResult(intent, REQUEST_EDIT_COURSE)
    }

    /**
     * Delete course functionality
     * Shows a confirmation dialog before deleting
     */
    private fun deleteCourse(course: Course) {
        // Show confirmation dialog using AlertDialog
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_course))
            .setMessage(getString(R.string.confirm_delete_course, course.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteCourseAndGrades(course)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * Delete course and all related grades from Firebase
     * Uses a batch operation to ensure all related data is deleted
     */
    private fun deleteCourseAndGrades(course: Course) {
        // Show progress bar
        progressBar.visibility = View.VISIBLE
        
        // First delete all grades for this course
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("courseId", course.id)
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
                        // Now delete the course
                        FirebaseUtil.getCoursesCollection().document(course.id)
                            .delete()
                            .addOnSuccessListener {
                                // Hide progress bar
                                progressBar.visibility = View.GONE
                                
                                // Show a success notification message
                                Toast.makeText(this, getString(R.string.course_and_grades_deleted), Toast.LENGTH_SHORT).show()
                                // Remove from local lists and update adapter
                                allCourses.remove(course)
                                courseList.remove(course)
                                courseAdapter.updateCourseList(courseList) // Update the adapter with new data
                            }
                            .addOnFailureListener { e ->
                                // Hide progress bar
                                progressBar.visibility = View.GONE
                                
                                // Show an error notification if course deletion fails
                                Toast.makeText(this, getString(R.string.error_deleting_course, e.message), Toast.LENGTH_SHORT).show()
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
     * Add course functionality
     * Opens the AddEditCourseActivity to create a new course
     */
    private fun addCourse(course: Course) {
        val intent = Intent(this, AddEditCourseActivity::class.java)
        startActivityForResult(intent, REQUEST_ADD_COURSE)
    }
    
    /**
     * Add grade to course functionality
     * Opens the AddGradeActivity with the course's ID and name
     */
    private fun gradeCourse(course: Course) {
        val intent = Intent(this, AddGradeActivity::class.java)
        intent.putExtra(EXTRA_COURSE_ID, course.id)
        intent.putExtra(EXTRA_COURSE_NAME, course.name)
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
