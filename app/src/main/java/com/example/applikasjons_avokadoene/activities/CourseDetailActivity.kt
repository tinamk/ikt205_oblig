package com.example.applikasjons_avokadoene.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var textCourseName: TextView
    private lateinit var textCourseCode: TextView
    private lateinit var textInstructor: TextView
    private lateinit var textStudentsEnrolled: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        textCourseName = findViewById(R.id.textCourseName)
        textCourseCode = findViewById(R.id.textCourseCode)
        textInstructor = findViewById(R.id.textInstructor)
        textStudentsEnrolled = findViewById(R.id.textStudentsEnrolled)

        // TODO: Fetch and display course details
    }
}
