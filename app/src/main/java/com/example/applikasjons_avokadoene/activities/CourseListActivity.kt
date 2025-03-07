package com.example.applikasjons_avokadoene.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.adapters.CourseAdapter
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import kotlinx.coroutines.launch

class CourseListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    private val courseList = mutableListOf<Course>()

    companion object {
        const val REQUEST_ADD_COURSE = 1 // ✅ Declare request code for adding a course
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        title = "Course List"

        lifecycleScope.launch {
            fetchCoursesFromFirestore()
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)


        courseAdapter = CourseAdapter(courseList, ::editCourse, ::deleteCourse, ::addCourse)
        recyclerView.adapter = courseAdapter


        fetchCoursesFromFirestore()

        val btnAddNewCourse: Button = findViewById(R.id.btn_add_new_course)
        btnAddNewCourse.setOnClickListener {
            val intent = Intent(this, AddEditCourseActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_COURSE)
        }
    }

    /**
     * Fetch courses from Firestore and update the list
     */
    private fun fetchCoursesFromFirestore() {
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                courseList.clear() // ✅ Clear list before adding new data
                for (document in documents) {
                    val course = document.toObject(Course::class.java).apply {
                        id = document.id // ✅ Assign Firestore document ID
                    }
                    courseList.add(course)
                }
                courseAdapter.notifyDataSetChanged() // ✅ Refresh RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching courses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Handle result from AddEditCourseActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_COURSE && resultCode == Activity.RESULT_OK) {
            val newCourse = data?.getSerializableExtra("NEW_COURSE") as? Course
            if (newCourse != null) {
                FirebaseUtil.getCoursesCollection()
                    .add(newCourse.toMap()) // ✅ Save to Firestore
                    .addOnSuccessListener { documentRef ->
                        newCourse.id = documentRef.id
                        courseList.add(newCourse)
                        courseAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add course: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    /**
     * Edit course functionality
     */
    private fun editCourse(course: Course) {
        Toast.makeText(this, "Edit Course: ${course.name}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Delete course functionality
     */
    private fun deleteCourse(course: Course) {
        Toast.makeText(this, "Delete Course: ${course.name}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Add course functionality
     */
    private fun addCourse(course: Course) {
        Toast.makeText(this, "Add Course: ${course.name}", Toast.LENGTH_SHORT).show()
    }
}
