package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.adapters.StudentAdapter
import com.example.applikasjons_avokadoene.models.Student

class StudentListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private val studentList = mutableListOf<Student>() // Mutable list to store students

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_list)
        title = "Student List"

        recyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter
        studentAdapter = StudentAdapter(studentList, ::onEditClick, ::onDeleteClick, ::onAddGradeClick)
        recyclerView.adapter = studentAdapter

        // Load sample data (Replace with database logic)
        loadSampleStudents()
    }

    private fun loadSampleStudents() {
        studentList.add(Student("John Doe", "12345", "johndoe@email.com"))
        studentList.add(Student("Alice Smith", "67890", "alice@email.com"))
        studentList.add(Student("Bob Johnson", "54321", "bob@email.com"))

        // Update RecyclerView with new data
        studentAdapter.updateStudents(studentList)
    }

    private fun onEditClick(student: Student) {
        // TODO: Open edit student activity
    }

    private fun onDeleteClick(student: Student) {
        // TODO: Delete student
    }

    private fun onAddGradeClick(student: Student) {
        // TODO: Open add grade activity
    }
}
