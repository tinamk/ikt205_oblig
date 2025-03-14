package com.example.applikasjons_avokadoene.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.activities.StudentDetailActivity
import com.example.applikasjons_avokadoene.models.Student

// Adapter for displaying a list of students in a course
class StudentInCourseAdapter(
    private var studentList: List<Student>
) : RecyclerView.Adapter<StudentInCourseAdapter.StudentViewHolder>() {

    // ViewHolder for each student item
    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewStudentName: TextView = view.findViewById(R.id.textViewStudentName)
        val textViewStudentId: TextView = view.findViewById(R.id.textViewStudentId)
    }
    // Update the student list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_in_course, parent, false)
        return StudentViewHolder(view)
    }
    // Bind data to the ViewHolder, including click handling
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentList[position]
        
        // Set student information
        holder.textViewStudentName.text = student.name
        holder.textViewStudentId.text = "ID: ${student.studentId}"
        
        // Set click listener for the item
        holder.itemView.setOnClickListener {
            try {
                val context = holder.itemView.context
                val intent = Intent(context, StudentDetailActivity::class.java)
                
                // Ensure we have valid data
                if (student.id.isNotEmpty()) {
                    intent.putExtra("STUDENT_ID", student.id)
                    intent.putExtra("STUDENT_NAME", student.name)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Cannot open student details: Missing ID", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val context = holder.itemView.context
                Toast.makeText(context, "Error opening student details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = studentList.size
} 