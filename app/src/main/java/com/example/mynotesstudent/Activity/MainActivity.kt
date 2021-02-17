package com.example.mynotesstudent.Activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesstudent.Adapter.NotesRecyclerAdapter
import com.example.mynotesstudent.Model.Notes
import com.example.mynotesstudent.R
import com.example.mynotesstudent.R.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.getValue
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity(),NotesRecyclerAdapter.NoteListener {

    lateinit var fab:FloatingActionButton
    val TAG = "MainActivity"
    lateinit var recyclerView: RecyclerView
    lateinit var notesRecyclerAdapter: NotesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        setSupportActionBar(findViewById(R.id.linearLayout))

        if (FirebaseAuth.getInstance().currentUser == null){
            val intent = Intent(this, LoginRegisterActivity::class.java)
            startActivity(intent)
        }

        fab  =findViewById(id.fab)
        recyclerView = findViewById(id.recyclerView)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initRecyclerAdapter()

        var ref:DatabaseReference
        fab.setOnClickListener {
            createAlertDialog()

        }
    }

    private fun initRecyclerAdapter() {
        val query:Query = FirebaseDatabase.getInstance().reference
            .child("Notes")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)

        val option:FirebaseRecyclerOptions<Notes> = FirebaseRecyclerOptions.Builder<Notes>()
                .setQuery(query, Notes::class.java)
                .build()

        notesRecyclerAdapter = NotesRecyclerAdapter(option,this)

        recyclerView.adapter = notesRecyclerAdapter

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    var simpleCallback:ItemTouchHelper.SimpleCallback =
        object: ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            TODO("Not yet implemented")
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (direction == ItemTouchHelper.LEFT){
                Toast.makeText(this@MainActivity,"Delete",Toast.LENGTH_LONG).show()
                notesRecyclerAdapter.deleteItem(viewHolder.adapterPosition)
            }
            else{
                Toast.makeText(this@MainActivity,"Updated",Toast.LENGTH_LONG).show()
                notesRecyclerAdapter.editItem(viewHolder.adapterPosition)
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(color.sec_orange_200)
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }

    private fun createAlertDialog() {

        val edtText:EditText = EditText(this)

        AlertDialog.Builder(this)
                .setTitle("Add Notes")
                .setView(edtText)
                .setPositiveButton("Add", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        //add data to our firebase realtime database
                        Log.d(TAG, edtText.text.toString())
                        addNotesToFirebase(edtText.text.toString())
                    }

                })
                .setNegativeButton("Cancel", null)
                .create()
                .show()

    }

    private fun addNotesToFirebase(text: String) {
        val ref = FirebaseDatabase.getInstance().reference

        val notes = Notes(
                text,
                false,
                System.currentTimeMillis()
        )
        ref.child("Notes")
                .child(FirebaseAuth.getInstance().uid.toString())
                .child(UUID.randomUUID().toString())
                .setValue(notes)
                .addOnSuccessListener {
                    Log.d(TAG, "Notes added successfully")
                    Toast.makeText(this, "Notes added successfully", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.d(TAG, it.message!!)
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_action, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            id.menu_logout -> {
                AuthUI.getInstance()
                        .signOut(this)
                val intent = Intent(this, LoginRegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        notesRecyclerAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notesRecyclerAdapter.stopListening()
    }

    override fun handleCheckedChange(isCheck: Boolean, dataSnapshot: DataSnapshot) {
        Log.d(TAG,"handleCheckedChange: $isCheck")

        val mapOf = HashMap<String,Any>()
        mapOf.put("taskComplete",isCheck)
        dataSnapshot.ref.updateChildren(mapOf)
                .addOnSuccessListener {
                    Log.d(TAG,"onSuccess: Checkbox Updated")
                }
                .addOnFailureListener {
                    Log.d(TAG,"onFailure: Checkbox can not be Updated. ${it.message}")
                }
    }

    override fun handleEditClickListener(dataSnapshot: DataSnapshot) {

        val note = dataSnapshot.getValue<Notes>()

        val editText = EditText(this)
        editText.setText(note!!.text)
        editText.setSelection(note.text!!.length)

        AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setView(editText)
                .setPositiveButton("Done"){dialogInterface,I ->
                    //upadate the note
                    val newNote = editText.text.toString()

                    note.text = newNote
                    dataSnapshot.ref.setValue(note)
                            .addOnSuccessListener {
                                Log.d(TAG,"onSuccess: Note Updated")
                            }
                            .addOnFailureListener {
                                Log.d(TAG,"onFailure: ${it.message}")
                            }
                }
                .setNegativeButton("Cancel",null)
                .show()
    }

    override fun handleDelete(dataSnapshot: DataSnapshot) {
        dataSnapshot.ref.removeValue()
    }
}