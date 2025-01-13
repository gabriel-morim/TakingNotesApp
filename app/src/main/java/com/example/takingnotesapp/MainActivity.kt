package com.example.takingnotesapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.takingnotesapp.ui.theme.TakingNotesAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            TakingNotesAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProgramaPrincipal()
                }
            }
        }
    }
}

@Composable
fun ProgramaPrincipal() {
    val viewModel = remember { NoteViewModel() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val routesWithoutBottomBar = listOf(
        Destino.EcraLoginFirebase.route,
        Destino.EcraRegisterToFirebase.route,
        "${Destino.NoteEditScreen.route}/{noteId}"
    )

    val showBottomBar = !routesWithoutBottomBar.contains(currentRoute)
            && !currentRoute.orEmpty().startsWith(Destino.NoteEditScreen.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    appItems = Destino.toList
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(
                bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp,
                top = padding.calculateTopPadding(),
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                end = padding.calculateEndPadding(LocalLayoutDirection.current)
            )
        ) {
            AppNavigation(navController, viewModel)
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: NoteViewModel) {
    NavHost(navController, startDestination = Destino.EcraLoginFirebase.route) {
        composable(Destino.EcraLoginFirebase.route) {
            EcraLoginFirebase(navController)
        }
        composable(Destino.EcraRegisterToFirebase.route) {
            EcraRegisterToFirebase(navController)
        }
        composable(Destino.EcraSettings.route) {
            EcraSettings(navController)
        }
        composable(Destino.NoteList1.route) {
            NoteList1Screen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate("${Destino.NoteEditScreen.route}/$noteId")
                }
            )
        }
        composable(Destino.NoteList2.route) {
            NoteList2Screen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate("${Destino.NoteEditScreen.route}/$noteId")
                }
            )
        }
        composable(Destino.NoteCreation.route) {
            NoteCreationScreen(
                viewModel = viewModel,
                onNoteCreated = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "${Destino.NoteEditScreen.route}/{noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            NoteEditScreen(
                noteId = noteId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, appItems: List<Destino>) {
    BottomNavigation(
        backgroundColor = colorResource(id = R.color.purple_700),
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        appItems.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painterResource(id = item.icon),
                        contentDescription = item.title,
                        tint = if(currentRoute == item.route) Color.White else Color.White.copy(.4F)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if(currentRoute == item.route) Color.White else Color.White.copy(.4F)
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White.copy(0.4f),
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
