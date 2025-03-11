package com.example.applikasjons_avokadoene.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Course
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var textCourseName: TextView
    private lateinit var textCourseCode: TextView
    private lateinit var textInstructor: TextView
    private lateinit var textStudentsEnrolled: TextView
    private lateinit var barChart: BarChart
    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        // Initialize views
        textCourseName = findViewById(R.id.textCourseName)
        textCourseCode = findViewById(R.id.textCourseCode)
        textInstructor = findViewById(R.id.textInstructor)
        textStudentsEnrolled = findViewById(R.id.textStudentsEnrolled)
        barChart = findViewById(R.id.barChartGrades)

        // Get course from intent
        course = intent.getParcelableExtra("course") ?: return

        // Display course details
        displayCourseDetails()
        
        // Setup grade distribution chart
        setupGradeDistributionChart()
    }
    
    private fun displayCourseDetails() {
        textCourseName.text = "Course Name: ${course.name}"
        textCourseCode.text = "Course Code: ${course.code}"
        textInstructor.text = "Instructor: ${course.instructor}"
        textStudentsEnrolled.text = "Enrolled Students: ${course.studentsEnrolled}"
    }
    
    private fun setupGradeDistributionChart() {
        // Count grades
        val gradeCount = mutableMapOf(
            "A" to 0,
            "B" to 0,
            "C" to 0,
            "D" to 0,
            "E" to 0,
            "F" to 0
        )
        
        // Count each grade
        course.grades.forEach { grade ->
            gradeCount[grade] = (gradeCount[grade] ?: 0) + 1
        }
        
        // Create bar entries
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, gradeCount["A"]?.toFloat() ?: 0f))
        entries.add(BarEntry(1f, gradeCount["B"]?.toFloat() ?: 0f))
        entries.add(BarEntry(2f, gradeCount["C"]?.toFloat() ?: 0f))
        entries.add(BarEntry(3f, gradeCount["D"]?.toFloat() ?: 0f))
        entries.add(BarEntry(4f, gradeCount["E"]?.toFloat() ?: 0f))
        entries.add(BarEntry(5f, gradeCount["F"]?.toFloat() ?: 0f))
        
        // Create dataset
        val dataSet = BarDataSet(entries, "Grade Distribution")
        dataSet.colors = listOf(
            Color.rgb(76, 175, 80),  // A - Green
            Color.rgb(139, 195, 74), // B - Light Green
            Color.rgb(255, 235, 59), // C - Yellow
            Color.rgb(255, 152, 0),  // D - Orange
            Color.rgb(255, 87, 34),  // E - Deep Orange
            Color.rgb(244, 67, 54)   // F - Red
        )
        
        // Create bar data
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f
        
        // Configure chart
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.setFitBars(true)
        
        // Configure X axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("A", "B", "C", "D", "E", "F"))
        
        // Configure Y axis
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        
        // Animate and refresh
        barChart.animateY(1000)
        barChart.invalidate()
    }
}
