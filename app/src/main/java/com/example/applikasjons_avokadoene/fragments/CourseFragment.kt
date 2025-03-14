package com.example.applikasjons_avokadoene.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.AddEditCourseActivity
import com.example.applikasjons_avokadoene.activities.AddGradeActivity
import com.example.applikasjons_avokadoene.activities.CourseListActivity
import com.example.applikasjons_avokadoene.adapters.CourseAdapter
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject

// Fragment for displaying a list of courses
class CourseFragment : Fragment() {

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var recyclerView: RecyclerView
    private val courseList: MutableList<Course> = mutableListOf()
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewNoCourses: TextView

    companion object {
        const val REQUEST_ADD_GRADE = 3
    }

    // Inflate the layout for this fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        progressBar = view.findViewById(R.id.progressBarCourses)
        textViewNoCourses = view.findViewById(R.id.textViewNoCourses)
        return view
    }

    // Initialize the RecyclerView and load courses from Firebase
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadCoursesFromFirebase()
    }

    // Reload data when the fragment is resumed
    override fun onResume() {
        super.onResume()
        // Legg til ekstra logging
        android.util.Log.d("CourseFragment", "onResume called, courseList has ${courseList.size} items")
        android.util.Log.d("CourseFragment", "RecyclerView visibility: ${if (recyclerView.visibility == View.VISIBLE) "VISIBLE" else "GONE"}")
        android.util.Log.d("CourseFragment", "TextView visibility: ${if (textViewNoCourses.visibility == View.VISIBLE) "VISIBLE" else "GONE"}")
        
        // Ekstra synlighetssjekk - forsikre seg om at recyclerView er synlig
        if (courseList.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            textViewNoCourses.visibility = View.GONE
        }
        
        // Refresh data when returning to the fragment
        loadCoursesFromFirebase()
    }

    // Setup the RecyclerView
    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(
            courseList,
            onEditCourseClick = { course ->
                showEditCourseDialog(course)
            },
            onDeleteCourseClick = { course ->
                deleteCourse(course)
            },
            onAddCourseClick = { course ->
                addCourse(course)
            },
            onGradeCourseClick = { course ->
                showGradeDialog(course)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = courseAdapter
    }

    // Load courses from Firebase
    private fun loadCoursesFromFirebase() {
        progressBar.visibility = View.VISIBLE
        textViewNoCourses.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        
        val tag = "CourseFragment"
        android.util.Log.d(tag, "Starting to load courses")

        // add logging for RecyclerView visibility
        android.util.Log.d(tag, "Before loading - RecyclerView visibility: ${if (recyclerView.visibility == View.VISIBLE) "VISIBLE" else "GONE"}")
        
        // solution for loading courses in background
        val tempCourseList = mutableListOf<Course>()


        // get courses from Firestore
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d(tag, "Got ${documents.size()} course documents from Firestore")
                
                // empty the temporary list
                tempCourseList.clear()
                
                if (documents.isEmpty) {
                    // no courses found
                    android.util.Log.d(tag, "No courses found in database")
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    textViewNoCourses.visibility = View.VISIBLE
                    android.util.Log.d(tag, "Set textViewNoCourses to VISIBLE")
                    return@addOnSuccessListener
                }
                
                // add courses to temporary list
                for (document in documents) {
                    try {
                        val course = document.toObject(Course::class.java)
                        course.id = document.id
                        tempCourseList.add(course)
                        android.util.Log.d(tag, "Added course: ${course.name} (${course.id})")
                    } catch (e: Exception) {
                        android.util.Log.e(tag, "Error adding course: ${e.message}")
                    }
                }
                
                android.util.Log.d(tag, "After processing, tempCourseList has ${tempCourseList.size} items")
                
                // Update the course list
                courseList.clear()
                courseList.addAll(tempCourseList)
                
                // create a new adapter with the updated list
                val newAdapter = CourseAdapter(
                    courseList,
                    onEditCourseClick = { course ->
                        showEditCourseDialog(course)
                    },
                    onDeleteCourseClick = { course ->
                        deleteCourse(course)
                    },
                    onAddCourseClick = { course ->
                        addCourse(course)
                    },
                    onGradeCourseClick = { course ->
                        showGradeDialog(course)
                    }
                )
                
                // update the RecyclerView
                recyclerView.adapter = newAdapter
                
                // update the UI based on the number of courses
                if (courseList.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    textViewNoCourses.visibility = View.GONE
                    android.util.Log.d(tag, "Set recyclerView to VISIBLE with ${courseList.size} courses")
                } else {
                    recyclerView.visibility = View.GONE
                    textViewNoCourses.visibility = View.VISIBLE
                    android.util.Log.d(tag, "No courses after processing, showing textViewNoCourses")
                }
                
                // Hide progress bar
                progressBar.visibility = View.GONE
                
                // Load grades in background
                if (courseList.isNotEmpty()) {
                    loadGradesInBackground()
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e(tag, "Error loading courses: ${e.message}")
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                textViewNoCourses.visibility = View.VISIBLE
                Toast.makeText(requireContext(), getString(R.string.error_fetching_courses, e.message), Toast.LENGTH_SHORT).show()
            }
    }
    
    // Separate method for loading grades
    private fun loadGradesInBackground() {
        val tag = "CourseFragment"
        android.util.Log.d(tag, "Loading grades in background")
        
        for (course in courseList) {
            loadGradesForCourse(course)
        }
    }
    
    // Load grades for a specific course
    private fun loadGradesForCourse(course: Course) {
        val tag = "CourseFragment"
        
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
                
                // update the adapter
                activity?.runOnUiThread {
                    android.util.Log.d(tag, "Updating UI after loading grades for course ${course.name}")
                    courseAdapter.updateCourseList(courseList)
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e(tag, "Error loading grades for course ${course.name}: ${e.message}")
            }
    }
    
    // edit course
    private fun showEditCourseDialog(course: Course) {
        android.util.Log.d("CourseFragment", "Edit button clicked for course: ${course.name}")
        try {
            val intent = Intent(requireContext(), AddEditCourseActivity::class.java)
            intent.putExtra("COURSE", course)
            startActivityForResult(intent, CourseListActivity.REQUEST_EDIT_COURSE)
        } catch (e: Exception) {
            android.util.Log.e("CourseFragment", "Error starting edit activity: ${e.message}")
            Toast.makeText(context, "Error opening edit form: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Method add student in course
    private fun addCourse(course: Course) {
        android.util.Log.d("CourseFragment", "Add student button clicked for course: ${course.name}")
        // Implement logic for adding student to course
        Toast.makeText(requireContext(), "Add student to course feature coming soon", Toast.LENGTH_SHORT).show()
    }

    // Method add grade
    private fun showGradeDialog(course: Course) {
        android.util.Log.d("CourseFragment", "Grade button clicked for course: ${course.name}")
        try {
            val intent = Intent(requireContext(), AddGradeActivity::class.java)
            intent.putExtra("COURSE_ID", course.id)
            intent.putExtra("COURSE_NAME", course.name)
            startActivityForResult(intent, CourseFragment.REQUEST_ADD_GRADE)
        } catch (e: Exception) {
            android.util.Log.e("CourseFragment", "Error starting grade activity: ${e.message}")
            Toast.makeText(requireContext(), "Error opening grade form: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Method delete course
    private fun deleteCourse(course: Course) {
        android.util.Log.d("CourseFragment", "Delete button clicked for course: ${course.name}")
        
        // confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_course)
            .setMessage(getString(R.string.confirm_delete_course, course.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteConfirmed(course)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    // deleting course
    private fun deleteConfirmed(course: Course) {
        progressBar.visibility = View.VISIBLE

        // delete grades first
        FirebaseUtil.getGradesCollection()
            .whereEqualTo("courseId", course.id)
            .get()
            .addOnSuccessListener { gradeDocuments ->
                val batch = FirebaseUtil.db.batch()
                
                // Add all grade deletions to batch
                for (gradeDoc in gradeDocuments) {
                    batch.delete(gradeDoc.reference)
                }
                
                // Execute batch grade delete
                batch.commit()
                    .addOnSuccessListener {
                        // Now delete the course
                        FirebaseUtil.getCoursesCollection().document(course.id)
                            .delete()
                            .addOnSuccessListener {
                                // Remove course from list and update UI
                                courseList.remove(course)
                                courseAdapter.updateCourseList(courseList)
                                progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), getString(R.string.course_and_grades_deleted), Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), getString(R.string.error_deleting_course, e.message), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), getString(R.string.error_deleting_grades, e.message), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.error_finding_grades, e.message), Toast.LENGTH_SHORT).show()
            }
    }


    // Handle result from AddGradeActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_ADD_GRADE) {
            if (resultCode == AddGradeActivity.RESULT_GRADE_ADDED || 
                resultCode == AddGradeActivity.RESULT_GRADE_UPDATED) {
                // Refresh the list after a grade is added or updated
                loadCoursesFromFirebase()
                
                // Show confirmation message
                val studentName = data?.getStringExtra("STUDENT_NAME") ?: "Student"
                val courseName = data?.getStringExtra("COURSE_NAME") ?: "course"
                val gradeLetter = data?.getStringExtra("GRADE_LETTER") ?: ""
                
                val message = if (resultCode == AddGradeActivity.RESULT_GRADE_ADDED) {
                    getString(R.string.added_grade_format, gradeLetter, studentName, courseName)
                } else {
                    getString(R.string.updated_grade_format, gradeLetter, studentName, courseName)
                }
                
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}