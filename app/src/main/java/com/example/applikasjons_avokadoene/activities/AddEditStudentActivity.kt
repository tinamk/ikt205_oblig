package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R

class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var editTextStudentName: EditText
    private lateinit var editTextStudentId: EditText
    private lateinit var buttonSaveStudent: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_student)

        editTextStudentName = findViewById(R.id.editTextStudentName)
        editTextStudentId = findViewById(R.id.editTextStudentId)
        buttonSaveStudent = findViewById(R.id.buttonSaveStudent)

        buttonSaveStudent.setOnClickListener {
            saveStudent()
        }
    }

    private fun saveStudent() {
        val studentName = editTextStudentName.text.toString()
        val studentId = editTextStudentId.text.toString()

        // TODO: Save student data
    }
}
