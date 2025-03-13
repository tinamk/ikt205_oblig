package com.example.applikasjons_avokadoene.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.AddGradeActivity
import com.example.applikasjons_avokadoene.adapters.CourseAdapter
import com.example.applikasjons_avokadoene.models.Course
import com.example.applikasjons_avokadoene.models.Grade
import com.example.applikasjons_avokadoene.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject

class CourseFragment : Fragment() {

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var recyclerView: RecyclerView
    private val courseList: MutableList<Course> = mutableListOf()
    private lateinit var progressBar: ProgressBar

    companion object {
        const val REQUEST_ADD_GRADE = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        progressBar = view.findViewById(R.id.progressBarCourses)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadCoursesFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the fragment
        loadCoursesFromFirebase()
    }

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

    private fun loadCoursesFromFirebase() {
        progressBar.visibility = View.VISIBLE
        
        val tag = "CourseFragment"
        android.util.Log.d(tag, "Starting to fetch courses from Firestore")
        
        FirebaseUtil.getCoursesCollection()
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d(tag, "Got ${documents.size()} courses from Firestore")
                courseList.clear()
                
                // Load all courses immediately
                for (document in documents) {
                    try {
                        val course = document.toObject<Course>().apply {
                            id = document.id // Assign Firestore document ID
                        }
                        courseList.add(course)
                        android.util.Log.d(tag, "Added course to courseList: ${course.name}")
                    } catch (e: Exception) {
                        android.util.Log.e(tag, "Error converting document to Course: ${e.message}")
                    }
                }
                
                // Update UI immediately with courses (without grades yet)
                courseAdapter.updateCourseList(courseList)
                android.util.Log.d(tag, "Updated UI with ${courseList.size} courses (before loading grades)")
                
                // Then load grades for each course
                loadGradesForCourses()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.error_fetching_courses, e.message), Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadGradesForCourses() {
        val tag = "CourseFragment"
        
        if (courseList.isEmpty()) {
            progressBar.visibility = View.GONE
            return
        }
        
        // Keep track of processed courses
        var processedCourses = 0
        
        // Load grades for each course
        for (course in courseList) {
            android.util.Log.d(tag, "Loading grades for course: ${course.name} (${course.id})")
            
            FirebaseUtil.getGradesCollection()
                .whereEqualTo("courseId", course.id)
                .get()
                .addOnSuccessListener { gradeDocuments ->
                    android.util.Log.d(tag, "Found ${gradeDocuments.size()} grades for course ${course.name}")
                    
                    // Process grades
                    course.grades.clear() // Clear existing grades
                    for (gradeDoc in gradeDocuments) {
                        val grade = gradeDoc.getString("grade") ?: ""
                        if (grade.isNotEmpty()) {
                            course.grades.add(grade)
                            android.util.Log.d(tag, "Added grade $grade to course ${course.name}")
                        }
                    }
                    
                    // Calculate average grade
                    course.calculateAverageGrade()
                    android.util.Log.d(tag, "Calculated average grade for ${course.name}: ${course.averageGrade}")
                    
                    // Increment processed counter
                    processedCourses++
                    android.util.Log.d(tag, "Processed $processedCourses/${courseList.size} courses")
                    
                    // If all courses processed, update UI again with grade information
                    if (processedCourses >= courseList.size) {
                        android.util.Log.d(tag, "All course grades processed, updating UI")
                        courseAdapter.updateCourseList(courseList)
                        progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e(tag, "Error fetching grades for course ${course.name}: ${e.message}")
                    
                    // Count this course as processed even on failure
                    processedCourses++
                    android.util.Log.d(tag, "Processed (after error) $processedCourses/${courseList.size} courses")
                    
                    // If all courses processed, update UI
                    if (processedCourses >= courseList.size) {
                        android.util.Log.d(tag, "All course grades processed (some with errors), updating UI")
                        progressBar.visibility = View.GONE
                    }
                    
                    Toast.makeText(context, "Error fetching grades: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showEditCourseDialog(course: Course) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_course, null)
        val editCourseName = dialogLayout.findViewById<EditText>(R.id.editCourseName)
        val editCourseCode = dialogLayout.findViewById<EditText>(R.id.editCourseCode)
        val editInstructor = dialogLayout.findViewById<EditText>(R.id.editInstructor)
        val editStudentsEnrolled = dialogLayout.findViewById<EditText>(R.id.editStudentsEnrolled)

        editCourseName.setText(course.name)
        editCourseCode.setText(course.code)
        editInstructor.setText(course.instructor)
        editStudentsEnrolled.setText(course.studentsEnrolled.toString())

        builder.setView(dialogLayout)
            .setPositiveButton(getString(R.string.save)) { dialog, which ->
                val newName = editCourseName.text.toString()
                val newCode = editCourseCode.text.toString()
                val newInstructor = editInstructor.text.toString()
                val newStudentsEnrolled = editStudentsEnrolled.text.toString().toIntOrNull() ?: 0

                updateCourseInFirebase(course, newName, newCode, newInstructor, newStudentsEnrolled)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateCourseInFirebase(course: Course, newName: String, newCode: String, newInstructor: String, newStudentsEnrolled: Int) {
        progressBar.visibility = View.VISIBLE
        
        val updatedCourse = Course(
            id = course.id,
            name = newName,
            code = newCode,
            instructor = newInstructor,
            studentsEnrolled = newStudentsEnrolled,
            updatedAt = Timestamp.now()
        )

        FirebaseUtil.getCoursesCollection().document(course.id)
            .update(updatedCourse.toMap())
            .addOnSuccessListener {
                Toast.makeText(context, "Course updated successfully", Toast.LENGTH_SHORT).show()
                loadCoursesFromFirebase()  // Reload the courses after update
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error updating course: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCourse(course: Course) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.delete_course))
            .setMessage(getString(R.string.confirm_delete_course, course.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteCourseFromFirebase(course)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteCourseFromFirebase(course: Course) {
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
                                Toast.makeText(context, getString(R.string.course_and_grades_deleted), Toast.LENGTH_SHORT).show()
                                loadCoursesFromFirebase()  // Reload the data
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Toast.makeText(context, getString(R.string.error_deleting_course, e.message), Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, getString(R.string.error_deleting_grades, e.message), Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.error_finding_grades, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    private fun addCourse(course: Course) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_course, null)
        val addCourseName = dialogLayout.findViewById<EditText>(R.id.addCourseName)
        val addCourseCode = dialogLayout.findViewById<EditText>(R.id.addCourseCode)
        val addInstructor = dialogLayout.findViewById<EditText>(R.id.addInstructor)
        val addStudentsEnrolled = dialogLayout.findViewById<EditText>(R.id.addStudentsEnrolled)

        builder.setView(dialogLayout)
            .setPositiveButton(getString(R.string.add_course)) { dialog, which ->
                val newName = addCourseName.text.toString()
                val newCode = addCourseCode.text.toString()
                val newInstructor = addInstructor.text.toString()
                val newStudentsEnrolled = addStudentsEnrolled.text.toString().toIntOrNull() ?: 0

                addCourseToFirebase(newName, newCode, newInstructor, newStudentsEnrolled)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addCourseToFirebase(name: String, code: String, instructor: String, studentsEnrolled: Int) {
        if (name.isEmpty() || code.isEmpty() || instructor.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        
        val newCourse = Course(
            name = name,
            code = code,
            instructor = instructor,
            studentsEnrolled = studentsEnrolled,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        FirebaseUtil.getCoursesCollection()
            .add(newCourse.toMap())
            .addOnSuccessListener {
                Toast.makeText(context, getString(R.string.course_added_success), Toast.LENGTH_SHORT).show()
                loadCoursesFromFirebase()  // Reload data after adding
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.failed_to_add_course, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    private fun showGradeDialog(course: Course) {
        // Launch the proper AddGradeActivity instead of using the dialog with dummy data
        val intent = Intent(requireActivity(), AddGradeActivity::class.java)
        intent.putExtra("COURSE_ID", course.id)
        intent.putExtra("COURSE_NAME", course.name)
        startActivityForResult(intent, REQUEST_ADD_GRADE)
    }
    
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