package com.example.applikasjons_avokadoene.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.CourseDetailActivity
import com.example.applikasjons_avokadoene.activities.CourseListActivity
import com.example.applikasjons_avokadoene.models.Course

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
        val btnEditCourse: ImageButton = itemView.findViewById(R.id.btn_edit_course)
        val btnDeleteCourse: ImageButton = itemView.findViewById(R.id.btn_delete_course)
        val btnAddCourse: ImageButton = itemView.findViewById(R.id.btn_add_course)
        val btnGradeCourse: ImageButton = itemView.findViewById(R.id.btn_grade_course)
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
        // Get the course at the current position
        val currentCourse = courseList[position]
        val context = holder.itemView.context
        
        // Set the text for each TextView using string resources for formatting
        holder.textCourseName.text = currentCourse.name
        holder.textCourseCode.text = context.getString(R.string.course_code_format, currentCourse.code)
        holder.textInstructorName.text = context.getString(R.string.instructor_format, currentCourse.instructor)
        holder.textStudentCount.text = context.getString(R.string.students_format, currentCourse.studentsEnrolled)
        holder.textAverageGrade.text = context.getString(R.string.average_grade_format, currentCourse.averageGrade)

        // Set click listeners for all buttons
        holder.btnEditCourse.setOnClickListener { onEditCourseClick(currentCourse) }
        holder.btnDeleteCourse.setOnClickListener { onDeleteCourseClick(currentCourse) }
        holder.btnAddCourse.setOnClickListener { onAddCourseClick(currentCourse) }
        holder.btnGradeCourse.setOnClickListener { onGradeCourseClick(currentCourse) }
        
        // Add click listener to the entire item view to navigate to CourseDetailActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, CourseDetailActivity::class.java)
            intent.putExtra(CourseListActivity.EXTRA_COURSE_ID, currentCourse.id)
            intent.putExtra(CourseListActivity.EXTRA_COURSE_NAME, currentCourse.name)
            context.startActivity(intent)
        }
    }

    /**
     * Returns the total number of items in the data set
     */
    override fun getItemCount() = courseList.size

    /**
     * Updates the course list with new data and refreshes the display
     * This is called when the data changes (e.g., after adding, editing, or deleting a course)
     */
    fun updateCourseList(newList: List<Course>) {
        android.util.Log.d("CourseAdapter", "Updating course list with ${newList.size} courses")
        courseList.clear()  // Clear the existing list
        courseList.addAll(newList)  // Add all items from the new list
        android.util.Log.d("CourseAdapter", "Course list after update: ${courseList.size} courses")
        
        for (course in courseList) {
            android.util.Log.d("CourseAdapter", "Course in list: ${course.name} (${course.id})")
        }
        
        notifyDataSetChanged()  // Tell the RecyclerView to refresh its display
        android.util.Log.d("CourseAdapter", "Called notifyDataSetChanged")
    }
}