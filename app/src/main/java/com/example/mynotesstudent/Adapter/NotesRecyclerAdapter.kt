package com.example.mynotesstudent.Adapter

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesstudent.Model.Notes
import com.example.mynotesstudent.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot

class NotesRecyclerAdapter(option: FirebaseRecyclerOptions<Notes>,val noteListener: NoteListener) : FirebaseRecyclerAdapter<Notes, NotesRecyclerAdapter.NoteViewHolder>(option) {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNote: TextView = itemView.findViewById(R.id.txtNote)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val checkBoxComplete: CheckBox = itemView.findViewById(R.id.checkBoxTask)
        val viewGroup:ConstraintLayout = itemView.findViewById(R.id.layoutViewGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_row, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, note: Notes) {
        holder.txtNote.text = note.text
        holder.checkBoxComplete.isChecked = note.taskComplete!!
        val date: CharSequence = DateFormat.format("EEEE, MMM d,yyyy h:mm:ss a", note.date!!)
        holder.txtDate.text = date

        holder.checkBoxComplete.setOnCheckedChangeListener { compoundButton, b ->
        val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            Log.d("NoteRecyclerAdapter","Adapter Position : ${holder.adapterPosition}")
            noteListener.handleCheckedChange(b,dataSnapshot)
        }

        holder.viewGroup.setOnClickListener {
            val dataSnapshot = snapshots.getSnapshot(holder.adapterPosition)
            noteListener.handleEditClickListener(dataSnapshot)
        }
    }

    public fun deleteItem(position: Int){
        noteListener.handleDelete(snapshots.getSnapshot(position))
    }

    public fun editItem(position: Int){
        val dataSnapshot = snapshots.getSnapshot(position)
        noteListener.handleEditClickListener(dataSnapshot)
    }

    interface NoteListener{
        public fun handleCheckedChange(isCheck:Boolean,dataSnapshot: DataSnapshot)
        public fun handleEditClickListener(dataSnapshot: DataSnapshot)
        public fun handleDelete(dataSnapshot: DataSnapshot)
    }
}