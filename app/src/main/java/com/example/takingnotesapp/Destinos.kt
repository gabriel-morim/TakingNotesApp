package com.example.takingnotesapp

sealed class Destino(val route: String, val icon: Int, val title: String) {
    object EcraLoginFirebase : Destino(route = "EcraLoginFirebase", icon = R.drawable.baseline_4g_mobiledata_24, title = "Login")
    object EcraRegisterToFirebase : Destino(route = "EcraRegisterToFirebase", icon = R.drawable.baseline_4g_mobiledata_24, title = "Register")
    object EcraSettings : Destino(route = "noteCreation", icon = R.drawable.baseline_app_settings_alt_24, title = "Settings")
    object NoteList1 : Destino(route = "noteList1", icon = R.drawable.baseline_notes_24, title = "Notes 1")
    object NoteList2 : Destino(route = "noteList2", icon = R.drawable.baseline_notes_24, title = "Notes 2")
    object NoteCreation : Destino(route = "EcraSettings", icon = R.drawable.baseline_settings_24, title = "Create Note")


    companion object {
        val toList = listOf(NoteList1, NoteList2, NoteCreation, EcraSettings)
    }
}