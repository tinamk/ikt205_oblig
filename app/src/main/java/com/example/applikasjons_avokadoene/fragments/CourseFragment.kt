package com.example.applikasjons_avokadoene.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.adapters.CourseAdapter
import com.example.applikasjons_avokadoene.models.Course

class CourseFragment : Fragment() {

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var recyclerView: RecyclerView
    private val courseList: MutableList<Course> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        addDummyCourses()
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

                val index = courseList.indexOf(course)
                if (index != -1) {
                    courseList[index] = Course(
                        id = course.id,
                        name = newName,
                        code = newCode,
                        instructor = newInstructor,
                        studentsEnrolled = newStudentsEnrolled,
                        grades = course.grades,
                        averageGrade = course.averageGrade
                    )
                    courseAdapter.updateCourseList(courseList)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteCourse(course: Course) {
        val index = courseList.indexOf(course)
        if (index != -1) {
            courseList.removeAt(index)
            courseAdapter.updateCourseList(courseList)
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

                val newCourse = Course(
                    name = newName,
                    code = newCode,
                    instructor = newInstructor,
                    studentsEnrolled = newStudentsEnrolled
                )
                courseList.add(newCourse)
                courseAdapter.updateCourseList(courseList)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showGradeDialog(course: Course) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_grade_course, null)
        
        // UI elements
        val textViewSelectedGrade = dialogLayout.findViewById<android.widget.TextView>(R.id.textViewSelectedGrade)
        
        // Get references to all grade buttons
        val btnGradeA = dialogLayout.findViewById<android.widget.Button>(R.id.btnGradeA)
        val btnGradeB = dialogLayout.findViewById<android.widget.Button>(R.id.btnGradeB)
        val btnGradeC = dialogLayout.findViewById<android.widget.Button>(R.id.btnGradeC)
        val btnGradeD = dialogLayout.findViewById<android.widget.Button>(R.id.btnGradeD)
        val btnGradeE = dialogLayout.findViewById<android.widget.Button>(R.id.btnGradeE)
        val btnGradeF = dialogLayout.findViewById<android.widget.Button>(R.id.btnGradeF)
        
        // Variable to store the selected grade
        var selectedGrade: String? = null
        
        // Create the dialog with null listeners for buttons
        val dialog = builder.setView(dialogLayout)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        
        // Helper function to update selected grade
        val setSelectedGrade = { grade: String ->
            selectedGrade = grade
            textViewSelectedGrade.text = getString(R.string.selected_grade_format, grade)
            
            // Update button appearance for selected state
            val buttons = listOf(btnGradeA, btnGradeB, btnGradeC, btnGradeD, btnGradeE, btnGradeF)
            buttons.forEach { button ->
                button.alpha = if (button.text.toString() == grade) 1.0f else 0.6f
            }
        }
        
        // Set click listeners for each grade button
        btnGradeA.setOnClickListener { setSelectedGrade(getString(R.string.grade_a)) }
        btnGradeB.setOnClickListener { setSelectedGrade(getString(R.string.grade_b)) }
        btnGradeC.setOnClickListener { setSelectedGrade(getString(R.string.grade_c)) }
        btnGradeD.setOnClickListener { setSelectedGrade(getString(R.string.grade_d)) }
        btnGradeE.setOnClickListener { setSelectedGrade(getString(R.string.grade_e)) }
        btnGradeF.setOnClickListener { setSelectedGrade(getString(R.string.grade_f)) }
        
        // Show the dialog
        dialog.show()
        
        // Set up the positive button click listener after dialog is shown
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (selectedGrade != null) {
                // Add the grade and update the UI
                course.grades.add(selectedGrade!!)
                course.calculateAverageGrade()
                courseAdapter.updateCourseList(courseList)
                dialog.dismiss()
            } else {
                // Prompt user to select a grade
                Toast.makeText(context, getString(R.string.error_invalid_grade), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addDummyCourses() {
        courseList.add(Course(name = "Mathematics", code = "MATH101", instructor = "Dr. Smith", studentsEnrolled = 30))
        courseList.add(Course(name = "Physics", code = "PHYS201", instructor = "Prof. Johnson", studentsEnrolled = 25))
        courseList.add(Course(name = "Chemistry", code = "CHEM101", instructor = "Dr. Williams", studentsEnrolled = 35))
        courseList.add(Course(name = "Biology", code = "BIO101", instructor = "Prof. Davis", studentsEnrolled = 40))
        courseList.add(Course(name = "Computer Science", code = "CS101", instructor = "Dr. Brown", studentsEnrolled = 50))
        courseAdapter.updateCourseList(courseList)
    }
}