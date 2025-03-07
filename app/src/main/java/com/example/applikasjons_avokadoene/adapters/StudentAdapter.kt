package com.example.applikasjons_avokadoene.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applikasjons_avokadoene.R
import com.example.applikasjons_avokadoene.models.Student

/**
 * Adapter for displaying students in a RecyclerView
 */
class StudentAdapter(
    private var students: List<Student> =listOf(),
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit,
    private val onAddGradeClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    /**
     * ViewHolder for student items
     */
    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_student_name)
        val tvStudentId: TextView = view.findViewById(R.id.tv_student_id)
        val tvEmail: TextView = view.findViewById(R.id.tv_student_email)
        val btnAddGrade: ImageButton = view.findViewById(R.id.btn_add_grade)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        // Set student information
        holder.tvName.text = student.name
        holder.tvStudentId.text = "ID: ${student.studentId}"
        holder.tvEmail.text = "Email: ${student.email}"

        // Set click listeners
        holder.btnAddGrade.setOnClickListener { onAddGradeClick(student) }
        holder.btnEdit.setOnClickListener { onEditClick(student) }
        holder.btnDelete.setOnClickListener { onDeleteClick(student) }
    }

    override fun getItemCount(): Int = students.size

    /**
     * Update the adapter with new student data
     */
    fun updateStudents(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }
}