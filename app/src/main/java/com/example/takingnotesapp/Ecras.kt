package com.example.takingnotesapp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class NoteViewModel {
    private val db = Firebase.firestore
    val notes = mutableStateOf<List<Note>>(emptyList())

    init {
        fetchNotes()
    }

    fun fetchNotes() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            db.collection("notes")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    notes.value = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Note::class.java)?.copy(id = doc.id)
                    }
                }
                .addOnFailureListener { e ->
                    println("Error fetching notes: ${e.message}")
                }
        }
    }

    fun saveNote(note: Note) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val noteData = hashMapOf(
                "title" to note.title,
                "content" to note.content,
                "userId" to currentUser.uid,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("notes")
                .add(noteData)
                .addOnSuccessListener {
                    fetchNotes()
                }
                .addOnFailureListener { e ->
                    println("Error saving note: ${e.message}")
                }
        } else {
            println("User not authenticated")
        }
    }

    fun deleteNote(noteId: String) {
        db.collection("notes")
            .document(noteId)
            .delete()
            .addOnSuccessListener {
                fetchNotes()
            }
            .addOnFailureListener { e ->
                println("Error deleting note: ${e.message}")
            }
    }
}

@Composable
fun ExpandableNoteCard(
    note: Note,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded }
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                    }
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun NoteList1Screen(viewModel: NoteViewModel) {
    val notes by viewModel.notes
    LazyColumn {
        items(notes) { note ->
            ExpandableNoteCard(
                note = note,
                onDeleteClick = { viewModel.deleteNote(note.id) }
            )
        }
    }
}

@Composable
fun NoteList2Screen(viewModel: NoteViewModel) {
    val notes by viewModel.notes
    LazyColumn {
        items(notes) { note ->
            ExpandableNoteCard(
                note = note,
                onDeleteClick = { viewModel.deleteNote(note.id) }
            )
        }
    }
}

@Composable
fun NoteCreationScreen(viewModel: NoteViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Note Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Note Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.saveNote(Note(title = title, content = content))
                    title = ""
                    content = ""
                }
            }
        ) {
            Text("Save Note")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp() {
    val viewModel = remember { NoteViewModel() }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notes App") })
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                NoteCreationScreen(viewModel = viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                NoteList1Screen(viewModel = viewModel)
                NoteList2Screen(viewModel = viewModel)
            }
        }
    )
}