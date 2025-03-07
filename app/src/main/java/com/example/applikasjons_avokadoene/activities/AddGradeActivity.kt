package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R

class AddGradeActivity : AppCompatActivity() {

    private lateinit var editTextStudentId: EditText
    private lateinit var editTextCourseCode: EditText
    private lateinit var editTextGrade: EditText
    private lateinit var buttonSaveGrade: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grade)

        editTextStudentId = findViewById(R.id.editTextStudentId)
        editTextCourseCode = findViewById(R.id.editTextCourseCode)
        editTextGrade = findViewById(R.id.editTextGrade)
        buttonSaveGrade = findViewById(R.id.buttonSaveGrade)

        buttonSaveGrade.setOnClickListener {
            saveGrade()
        }
    }

    private fun saveGrade() {
        val studentId = editTextStudentId.text.toString()
        val courseCode = editTextCourseCode.text.toString()
        val grade = editTextGrade.text.toString()

        // TODO: Save grade to the database
    }
}
