package com.example.applikasjons_avokadoene.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Course
import android.widget.ImageButton


class CourseAdapter(private val courseList: List<Course>,
                    private val onEditCourseClick: (Course) -> Unit,
                    private val onDeleteCourseClick: (Course) -> Unit,
                    private val onAddCourseClick: (Course) -> Unit) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCourseName: TextView = itemView.findViewById(R.id.textCourseName)
        val textCourseCode: TextView = itemView.findViewById(R.id.textCourseCode)
        val textInstructor: TextView = itemView.findViewById(R.id.textInstructorName)
        val textStudentsEnrolled: TextView = itemView.findViewById(R.id.textStudentCount)
        val btnEditCourse: ImageButton = itemView.findViewById(R.id.btn_edit_course)
        val btnDeleteCourse: ImageButton = itemView.findViewById(R.id.btn_delete_course)
        val btnAddCourse: ImageButton = itemView.findViewById(R.id.btn_add_course)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.textCourseName.text = course.name
        holder.textCourseCode.text = "Code: ${course.code}"
        holder.textInstructor.text = "Instructor: ${course.instructor}"
        holder.textStudentsEnrolled.text = "Students: ${course.studentsEnrolled}"


        holder.btnEditCourse.setOnClickListener {
            onEditCourseClick(course)
            }

        holder.btnDeleteCourse.setOnClickListener {
            onDeleteCourseClick(course)
        }

        holder.btnAddCourse.setOnClickListener {
            onAddCourseClick(course)
        }
    }

    override fun getItemCount(): Int = courseList.size

    fun updateCourseList(newCourseList: List<Course>) {
        courseList.clear()
        courseList.addAll(newCourseList)
        diffResult.dispatchUpdatesTo(this)
    }

}
