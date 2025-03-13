package com.example.applikasjons_avokadoene.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.CourseDetailActivity
import com.example.applikasjons_avokadoene.models.Grade
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying grades in a RecyclerView
 */
class GradeAdapter(
    private var gradeList: List<Grade>
) : RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

    /**
     * ViewHolder for grade items
     */
    class GradeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewCourseName: TextView = view.findViewById(R.id.textViewCourseName)
        val textViewGrade: TextView = view.findViewById(R.id.textViewGrade)
        val textViewDate: TextView = view.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grade, parent, false)
        return GradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val grade = gradeList[position]
        val context = holder.itemView.context
        
        // Set grade information
        holder.textViewCourseName.text = grade.courseName
        holder.textViewGrade.text = "Grade: ${grade.grade}"
        
        // Format the date if available
        val dateStr = if (grade.date != null) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.format(grade.date.toDate())
            } catch (e: Exception) {
                "N/A"
            }
        } else {
            "N/A"
        }
        holder.textViewDate.text = "Date: $dateStr"
        
        // Make the item clickable to view course details
        holder.itemView.setOnClickListener {
            try {
                // Open course details for this grade
                val intent = Intent(context, CourseDetailActivity::class.java)
                intent.putExtra("COURSE_ID", grade.courseId)
                intent.putExtra("COURSE_NAME", grade.courseName)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening course details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = gradeList.size

    /**
     * Update the adapter with new grade data
     */
    fun updateGrades(newGrades: List<Grade>) {
        gradeList = newGrades
        notifyDataSetChanged()
    }
} 