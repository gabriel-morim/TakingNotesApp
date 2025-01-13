package com.example.takingnotesapp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val expandedNotes = mutableStateOf<Set<String>>(setOf())

    init {
        fetchNotes()
    }

    fun toggleNoteExpansion(noteId: String) {
        expandedNotes.value = if (expandedNotes.value.contains(noteId)) {
            expandedNotes.value - noteId
        } else {
            expandedNotes.value + noteId
        }
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    singleLine: Boolean = false,
    isTitle: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        Box {
            if (value.isEmpty() && !isFocused) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = LocalTextStyle.current.copy(
                        fontSize = if (isTitle) 24.sp else 16.sp,
                        fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }

            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(newValue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (isTitle) 24.sp else 16.sp,
                    fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                keyboardOptions = KeyboardOptions(
                    imeAction = if (singleLine) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )
        }
        Divider(
            color = if (isFocused)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

@Composable
fun ExpandableNoteCard(
    note: Note,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onExpandToggle() }
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
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = { onExpandToggle() }) {
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                    }
                }
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun NoteList1Screen(viewModel: NoteViewModel) {
    val notes by viewModel.notes
    val expandedNotes by viewModel.expandedNotes

    LazyColumn {
        items(notes) { note ->
            ExpandableNoteCard(
                note = note,
                isExpanded = expandedNotes.contains(note.id),
                onExpandToggle = { viewModel.toggleNoteExpansion(note.id) },
                onDeleteClick = { viewModel.deleteNote(note.id) }
            )
        }
    }
}

@Composable
fun NoteList2Screen(viewModel: NoteViewModel) {
    val notes by viewModel.notes
    val expandedNotes by viewModel.expandedNotes

    LazyColumn {
        items(notes) { note ->
            ExpandableNoteCard(
                note = note,
                isExpanded = expandedNotes.contains(note.id),
                onExpandToggle = { viewModel.toggleNoteExpansion(note.id) },
                onDeleteClick = { viewModel.deleteNote(note.id) }
            )
        }
    }
}

@Composable
fun NoteCreationScreen(viewModel: NoteViewModel) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = "Note Title",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isTitle = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = content,
            onValueChange = {
                content = it
                errorMessage = null
            },
            label = "Note Content",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.saveNote(Note(title = title, content = content))
                    title = ""
                    content = ""
                    errorMessage = null
                } else {
                    errorMessage = "Title and content cannot be empty"
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
            TopAppBar(
                title = { Text("Notes App") }
            )
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