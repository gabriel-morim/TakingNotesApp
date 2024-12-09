package com.example.takingnotesapp


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class NoteViewModel {
    private val db = Firebase.firestore
    val notes1 = mutableStateOf<List<Note>>(emptyList())
    val notes2 = mutableStateOf<List<Note>>(emptyList())

    init {
        fetchNotes()
    }

    fun fetchNotes() {
        db.collection("notes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val allNotes = querySnapshot.toObjects(Note::class.java)
                notes1.value = allNotes.take(5)
                notes2.value = allNotes.drop(5)
            }
    }

    fun saveNote(note: Note) {
        db.collection("notes")
            .add(note)
            .addOnSuccessListener { fetchNotes() }
    }

    fun deleteNote(noteId: String) {
        db.collection("notes")
            .document(noteId)
            .delete()
            .addOnSuccessListener { fetchNotes() }
    }
}

@Composable
fun NoteList(viewModel: NoteViewModel, notes: State<List<Note>>, onNoteDelete: (String) -> Unit) {
    LazyColumn {
        items(notes.value) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = note.title, fontWeight = FontWeight.Bold)
                        Text(text = note.content)
                    }
                    IconButton(onClick = { onNoteDelete(note.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                    }
                }
            }
        }
    }
}

@Composable
fun NoteList1Screen(viewModel: NoteViewModel) {
    val notes = viewModel.notes1
    NoteList(
        viewModel = viewModel,
        notes = notes,
        onNoteDelete = { noteId -> viewModel.deleteNote(noteId) }
    )
}

@Composable
fun NoteList2Screen(viewModel: NoteViewModel) {
    val notes = viewModel.notes2
    NoteList(
        viewModel = viewModel,
        notes = notes,
        onNoteDelete = { noteId -> viewModel.deleteNote(noteId) }
    )
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
                viewModel.saveNote(Note(title = title, content = content))
                title = ""
                content = ""
            }
        ) {
            Text("Save Note")
        }
    }
}