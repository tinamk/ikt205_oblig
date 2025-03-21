package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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

// Course list activity
class CourseListActivity : AppCompatActivity() {

    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private val courseList = mutableListOf<Course>()
    private val allCourses = mutableListOf<Course>() // Store all courses for filtering
    private lateinit var btnAddNewCourse: Button
    private lateinit var editTextSearch: EditText
    private lateinit var progressBar: ProgressBar

    companion object {
        // Constants for activity results and intent extras
        const val REQUEST_ADD_COURSE = 1 // Request code for adding a course (used with startActivityForResult)
        const val REQUEST_EDIT_COURSE = 2 // Request code for editing a course
        const val EXTRA_NEW_COURSE = "NEW_COURSE"
        const val EXTRA_COURSE = "COURSE"
        const val EXTRA_COURSE_ID = "COURSE_ID"
        const val EXTRA_COURSE_NAME = "COURSE_NAME"
        const val EXTRA_COURSE_CODE = "COURSE_CODE"
        const val REQUEST_ADD_GRADE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        title = getString(R.string.course_list)
        
        // Add back button to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize RecyclerView and other UI components
        recyclerView = findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAddNewCourse = findViewById(R.id.btn_add_new_course)
        editTextSearch = findViewById(R.id.editTextSearchCourse)
        progressBar = findViewById(R.id.progressBarCourses)
        
        // Initialiser CourseAdapter med knappefunksjoner
        courseAdapter = CourseAdapter(
            courseList,
            ::editCourse,
            ::deleteCourse,
            ::addCourse,
            ::gradeCourse
        )
        recyclerView.adapter = courseAdapter
        
        android.util.Log.d("CourseListActivity", "RecyclerView adapter er satt")
        
        // Set up add course button click listener
        btnAddNewCourse.setOnClickListener {
            val intent = Intent(this, AddEditCourseActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_COURSE)
        }
        
        // Set up search functionality
        setupSearch()
        
        // Load courses from Firebase
        fetchCoursesFromFirestore()
    }

    // Set up search functionality
    private fun setupSearch() {
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filterCourses(s.toString())
            }
        })
    }

    // Filter courses based on search query
    private fun filterCourses(query: String) {
        val tag = "CourseListActivity"
        android.util.Log.d(tag, "Filtering courses with query: '$query'")
        
        val filteredList = if (query.isEmpty()) {
            android.util.Log.d(tag, "Empty query, showing all courses")
            allCourses
        } else {
            allCourses.filter { course ->
                val nameMatch = course.name.contains(query, ignoreCase = true)
                val codeMatch = course.code.contains(query, ignoreCase = true)
                android.util.Log.d(tag, "Course: ${course.name} - Name match: $nameMatch, Code match: $codeMatch")
                nameMatch || codeMatch
            }
        }
        
        android.util.Log.d(tag, "Found ${filteredList.size} matching courses")
        
        courseList.clear()
        courseList.addAll(filteredList)
        courseAdapter.updateCourseList(courseList)
    }

    // Fetch courses from Firebase
    private fun fetchCoursesFromFirestore() {
        // Show progress bar
        progressBar.visibility = View.VISIBLE
        
        // Log that we are starting to fetch courses
        val tag = "CourseListActivity"
        android.util.Log.d(tag, "Starting to fetch courses from Firestore")
        
        // Temporary list to store courses
        val tempCourseList = mutableListOf<Course>()
        
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d(tag, "Got ${documents.size()} courses from Firestore")
                
                // Clear lists
                tempCourseList.clear()
                allCourses.clear()
                courseList.clear()
                
                if (documents.isEmpty) {
                    // No courses found
                    android.util.Log.d(tag, "No courses found in database")
                    progressBar.visibility = View.GONE
                    return@addOnSuccessListener
                }
                
                // Process each document
                for (document in documents) {
                    try {
                        val course = document.toObject(Course::class.java)
                        course.id = document.id
                        tempCourseList.add(course)
                        android.util.Log.d(tag, "Added course to tempList: ${course.name} (${course.id})")
                    } catch (e: Exception) {
                        android.util.Log.e(tag, "Error processing course: ${e.message}")
                    }
                }
                
                // Update lists
                allCourses.addAll(tempCourseList)
                
                // Apply filter if needed
                val currentFilter = editTextSearch.text.toString()
                if (currentFilter.isEmpty()) {
                    courseList.clear()
                    courseList.addAll(allCourses)
                } else {
                    filterCourses(currentFilter)
                }
                
                // Update the existing adapter with the new data
                courseAdapter.updateCourseList(courseList)
                
                android.util.Log.d(tag, "Updated adapter with ${courseList.size} courses")
                
                // Hide progress bar
                progressBar.visibility = View.GONE
                
                // Now load grades in the background, but don't block UI
                if (allCourses.isNotEmpty()) {
                    loadGradesInBackground()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e(tag, "Error fetching courses: ${e.message}")
                progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_fetching_courses, e.message), Toast.LENGTH_SHORT).show()
            }
    }
    
   // Load grades in the background
    private fun loadGradesInBackground() {
        val tag = "CourseListActivity"
        android.util.Log.d(tag, "Loading grades in background for ${allCourses.size} courses")
        
        val processedCourses = ArrayList<String>() // Track processed course IDs
        
        // For each course, load its grades
        for (course in allCourses) {
            FirebaseUtil.getGradesCollection()
                .whereEqualTo("courseId", course.id)
                .get()
                .addOnSuccessListener { gradeDocuments ->
                    android.util.Log.d(tag, "Found ${gradeDocuments.size()} grades for course ${course.name}")
                    
                    // Clear existing grades
                    course.grades.clear()
                    
                    // Process grades
                    for (gradeDoc in gradeDocuments) {
                        val grade = gradeDoc.getString("grade") ?: ""
                        if (grade.isNotEmpty()) {
                            course.grades.add(grade)
                        }
                    }
                    
                    // Calculate average grade
                    course.calculateAverageGrade()
                    
                    // Add this course to processed list
                    processedCourses.add(course.id)
                    
                    // If all courses processed, update UI
                    if (processedCourses.size >= allCourses.size) {
                        // Apply filter again
                        val currentFilter = editTextSearch.text.toString()
                        if (currentFilter.isEmpty()) {
                            courseList.clear()
                            courseList.addAll(allCourses)
                        } else {
                            filterCourses(currentFilter)
                        }
                        
                        // Update adapter with new grade data
                        courseAdapter.updateCourseList(courseList)
                        android.util.Log.d(tag, "Updated adapter with grade data for ${courseList.size} courses")
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e(tag, "Error loading grades for course ${course.name}: ${e.message}")
                    
                    // Add to processed even on failure
                    processedCourses.add(course.id)
                }
        }
    }

    // Handle result from AddEditCourseActivity
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

 /// Edit course functionality
    private fun editCourse(course: Course) {
        // Legg til logginfo
        android.util.Log.d("CourseListActivity", "editCourse ble kalt for kurs: ${course.name} (${course.id})")
        try {
            val intent = Intent(this, AddEditCourseActivity::class.java)
            intent.putExtra(EXTRA_COURSE, course)
            startActivityForResult(intent, REQUEST_EDIT_COURSE)
        } catch (e: Exception) {
            android.util.Log.e("CourseListActivity", "Feil ved oppstart av AddEditCourseActivity: ${e.message}")
            Toast.makeText(this, "Kunne ikke åpne redigeringsvindu: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

   // Delete course functionality
    private fun deleteCourse(course: Course) {
        // Legg til logginfo
        android.util.Log.d("CourseListActivity", "deleteCourse ble kalt for kurs: ${course.name} (${course.id})")
        
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
    
   // Delete course and grades from Firebase
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

    // Add course functionality
    private fun addCourse(course: Course) {
        // Add logging
        android.util.Log.d("CourseListActivity", "addCourse ble kalt")
        
        val intent = Intent(this, AddEditCourseActivity::class.java)
        startActivityForResult(intent, REQUEST_ADD_COURSE)
    }
    
    // Grade course functionality
    private fun gradeCourse(course: Course) {
        // Add logging
        android.util.Log.d("CourseListActivity", "gradeCourse ble kalt for kurs: ${course.name} (${course.id})")
        
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
