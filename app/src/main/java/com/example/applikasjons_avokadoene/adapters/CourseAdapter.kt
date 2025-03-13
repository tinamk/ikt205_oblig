package com.example.applikasjons_avokadoene.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.CourseDetailActivity
import com.example.applikasjons_avokadoene.activities.CourseListActivity
import com.example.applikasjons_avokadoene.models.Course
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * CourseAdapter is responsible for displaying a list of courses in a RecyclerView.
 * 
 * RecyclerView is an efficient way to display lists in Android. It reuses (recycles) 
 * the views that scroll off the screen instead of creating new ones, which improves performance.
 * 
 * This adapter handles:
 * 1. Creating view holders for each course item
 * 2. Binding course data to the views
 * 3. Handling click events on course items and buttons
 */
class CourseAdapter(
    private var courseList: MutableList<Course>,
    private val onEditCourseClick: (Course) -> Unit,  // Function to call when edit button is clicked
    private val onDeleteCourseClick: (Course) -> Unit,  // Function to call when delete button is clicked
    private val onAddCourseClick: (Course) -> Unit,  // Function to call when add button is clicked
    private val onGradeCourseClick: (Course) -> Unit  // Function to call when grade button is clicked
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    /**
     * ViewHolder class holds references to the views in each item layout
     * This improves performance by avoiding repeated calls to findViewById
     */
    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Find and store references to all views in the item layout
        val textCourseName: TextView = itemView.findViewById(R.id.textCourseName)
        val textCourseCode: TextView = itemView.findViewById(R.id.textCourseCode)
        val textInstructorName: TextView = itemView.findViewById(R.id.textInstructorName)
        val textStudentCount: TextView = itemView.findViewById(R.id.textStudentCount)
        val textAverageGrade: TextView = itemView.findViewById(R.id.textAverageGrade)
        val btnEditCourse: Button = itemView.findViewById(R.id.btn_edit_course)
        val btnDeleteCourse: Button = itemView.findViewById(R.id.btn_delete_course)
        val btnAddCourse: Button = itemView.findViewById(R.id.btn_add_course)
        val btnGradeCourse: Button = itemView.findViewById(R.id.btn_grade_course)
        val gradeDistributionChart: BarChart = itemView.findViewById(R.id.gradeDistributionChart)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder
     * This is where we inflate the layout for each item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        // Inflate the item layout and create a new ViewHolder
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(itemView)
    }

    /**
     * Called by RecyclerView to display data at a specified position
     * This is where we bind the course data to the views in the ViewHolder
     */
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (position >= courseList.size) {
            android.util.Log.e("CourseAdapter", "Position $position is out of bounds (size: ${courseList.size})")
            return
        }
        
        val currentCourse = courseList[position]
        val context = holder.itemView.context
        
        // Debug logging for troubleshooting
        android.util.Log.d("CourseAdapter", "Binding course at position $position: ${currentCourse.name}")
        
        try {
            // Set the text for each TextView
            holder.textCourseName.text = currentCourse.name
            holder.textCourseCode.text = context.getString(R.string.course_code_format, currentCourse.code)
            holder.textInstructorName.text = context.getString(R.string.instructor_format, currentCourse.instructor)
            holder.textStudentCount.text = context.getString(R.string.students_format, currentCourse.studentsEnrolled)
            holder.textAverageGrade.text = context.getString(R.string.average_grade_format, currentCourse.averageGrade)

            // Update grade distribution chart
            updateGradeDistributionChart(holder.gradeDistributionChart, currentCourse)
    
            // Set click listeners for buttons
            holder.btnEditCourse.setOnClickListener {
                android.util.Log.d("CourseAdapter", "Edit button clicked for: ${currentCourse.name}")
                onEditCourseClick(currentCourse)
            }
            
            holder.btnDeleteCourse.setOnClickListener {
                android.util.Log.d("CourseAdapter", "Delete button clicked for: ${currentCourse.name}")
                onDeleteCourseClick(currentCourse)
            }
            
            holder.btnAddCourse.setOnClickListener {
                android.util.Log.d("CourseAdapter", "Add button clicked for: ${currentCourse.name}")
                onAddCourseClick(currentCourse)
            }
            
            holder.btnGradeCourse.setOnClickListener {
                android.util.Log.d("CourseAdapter", "Grade button clicked for: ${currentCourse.name}")
                onGradeCourseClick(currentCourse)
            }
            
            // Make entire item clickable
            holder.itemView.setOnClickListener {
                try {
                    android.util.Log.d("CourseAdapter", "Course item clicked: ${currentCourse.name}")
                    val intent = Intent(context, CourseDetailActivity::class.java)
                    intent.putExtra(CourseListActivity.EXTRA_COURSE_ID, currentCourse.id)
                    intent.putExtra(CourseListActivity.EXTRA_COURSE_NAME, currentCourse.name)
                    intent.putExtra(CourseListActivity.EXTRA_COURSE_CODE, currentCourse.code)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("CourseAdapter", "Error opening course details: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CourseAdapter", "Error binding course view: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateGradeDistributionChart(chart: BarChart, course: Course) {
        // Grade labels
        val gradeLabels = arrayOf("A", "B", "C", "D", "E", "F")
        
        // Count occurrences of each grade
        val gradeCounts = IntArray(6) { 0 }
        for (grade in course.grades) {
            when (grade) {
                "A" -> gradeCounts[0]++
                "B" -> gradeCounts[1]++
                "C" -> gradeCounts[2]++
                "D" -> gradeCounts[3]++
                "E" -> gradeCounts[4]++
                "F" -> gradeCounts[5]++
            }
        }

        // Create bar entries
        val entries = ArrayList<BarEntry>()
        for (i in gradeCounts.indices) {
            entries.add(BarEntry(i.toFloat(), gradeCounts[i].toFloat()))
        }

        // Create dataset
        val dataSet = BarDataSet(entries, "Grade Distribution")
        dataSet.color = Color.rgb(54, 162, 235)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        // Create bar data
        val barData = BarData(dataSet)
        barData.barWidth = 0.7f

        // Configure chart
        chart.apply {
            this.data = barData
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            isHighlightFullBarEnabled = false
            setDrawBorders(false)
            setTouchEnabled(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            animateY(1000)

            // Configure X axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(gradeLabels)
                setDrawGridLines(false)
                axisMinimum = -0.5f
                axisMaximum = 5.5f
            }

            // Configure Y axis
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false
        }

        // Refresh chart
        chart.invalidate()
    }

    /**
     * Returns the total number of items in the data set
     */
    override fun getItemCount(): Int {
        android.util.Log.d("CourseAdapter", "getItemCount called, returning ${courseList.size}")
        return courseList.size
    }

    /**
     * Updates the course list with new data and refreshes the display
     * This is called when the data changes (e.g., after adding, editing, or deleting a course)
     */
    fun updateCourseList(newList: List<Course>) {
        android.util.Log.d("CourseAdapter", "updateCourseList called with ${newList.size} courses")
        
        // Create a new list to prevent reference issues
        val updatedList = ArrayList<Course>(newList)
        
        // Clear and update the list
        courseList.clear()
        courseList.addAll(updatedList)
        
        // Notify adapter about the changes
        notifyDataSetChanged()
        
        android.util.Log.d("CourseAdapter", "After update: courseList has ${courseList.size} items")
    }
}