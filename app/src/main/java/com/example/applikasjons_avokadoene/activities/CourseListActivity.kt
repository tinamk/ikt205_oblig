package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
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

    companion object {
        // Constants for activity results and intent extras
        const val REQUEST_ADD_COURSE = 1 // Request code for adding a course (used with startActivityForResult)
        const val EXTRA_NEW_COURSE = "NEW_COURSE"
        const val EXTRA_COURSE = "COURSE"
        const val EXTRA_COURSE_ID = "COURSE_ID"
        const val EXTRA_COURSE_NAME = "COURSE_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        title = getString(R.string.course_list)

        // Initialize RecyclerView and other UI components
        recyclerView = findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAddNewCourse = findViewById(R.id.btn_add_new_course)

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
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity (e.g., after editing a course)
        fetchCoursesFromFirestore()
    }

    /**
     * Fetch courses from Firestore database and update the list
     * This method gets all courses from Firebase and displays them in the RecyclerView
     */
    private fun fetchCoursesFromFirestore() {
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                courseList.clear() // Clear list before adding new data
                for (document in documents) {
                    val course = document.toObject<Course>().apply {
                        id = document.id // Assign Firestore document ID
                    }
                    courseList.add(course)
                }
                courseAdapter.updateCourseList(courseList) // Update the adapter with new data
            }
            .addOnFailureListener { e ->
                // Show a notification message (Toast) if there's an error fetching courses
                // Toast messages are small pop-up notifications that appear at the bottom of the screen
                // and automatically disappear after a short time
                Toast.makeText(this, getString(R.string.error_fetching_courses, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handle result from AddEditCourseActivity
     * This is called when returning from the add/edit course screen
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the result is from adding a course and was successful
        if (requestCode == REQUEST_ADD_COURSE && resultCode == Activity.RESULT_OK) {
            val newCourse = data?.getParcelableExtra<Course>(EXTRA_NEW_COURSE)
            if (newCourse != null) {
                // Save the new course to Firebase
                FirebaseUtil.getCoursesCollection()
                    .add(newCourse.toMap()) // Save to Firestore
                    .addOnSuccessListener { documentRef ->
                        newCourse.id = documentRef.id
                        courseList.add(newCourse)
                        courseAdapter.updateCourseList(courseList) // Update the adapter with new data
                        // Show a success notification message
                        Toast.makeText(this, getString(R.string.course_added_success), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Show an error notification if adding the course fails
                        Toast.makeText(this, getString(R.string.failed_to_add_course, e.message), Toast.LENGTH_SHORT).show()
                    }
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
        startActivity(intent)
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
                                // Show a success notification message
                                Toast.makeText(this, getString(R.string.course_and_grades_deleted), Toast.LENGTH_SHORT).show()
                                // Remove from local list and update adapter
                                courseList.remove(course)
                                courseAdapter.updateCourseList(courseList) // Update the adapter with new data
                            }
                            .addOnFailureListener { e ->
                                // Show an error notification if course deletion fails
                                Toast.makeText(this, getString(R.string.error_deleting_course, e.message), Toast.LENGTH_SHORT).show()
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
     * Add course functionality
     * Opens the AddEditCourseActivity to create a new course
     */
    private fun addCourse(course: Course) {
        val intent = Intent(this, AddEditCourseActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * Add grade to course functionality
     * Opens the AddGradeActivity with the course's ID and name
     */
    private fun gradeCourse(course: Course) {
        val intent = Intent(this, AddGradeActivity::class.java)
        intent.putExtra(EXTRA_COURSE_ID, course.id)
        intent.putExtra(EXTRA_COURSE_NAME, course.name)
        startActivity(intent)
    }
}
