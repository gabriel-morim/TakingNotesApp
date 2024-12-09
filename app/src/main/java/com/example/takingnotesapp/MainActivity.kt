package com.example.takingnotesapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.takingnotesapp.ui.theme.TakingNotesAppTheme
import com.example.takingnotesapp.ui.theme.TakingNotesAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            TakingNotesAppTheme() {
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

fun logAndToast(oContexto: Context, tag: String, aMensagem: String) {
    Log.d("FIRE:$tag", aMensagem)
    Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
}

@Composable
fun ProgramaPrincipal() {
    val viewModel = remember { NoteViewModel() }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                appItems = Destino.toList
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AppNavigation(navController, viewModel)
            }
        }
    )
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: NoteViewModel) {
    NavHost(navController, startDestination = Destino.NoteList1.route) {
        composable(Destino.NoteList1.route) {
            NoteList1Screen(viewModel)
        }
        composable(Destino.NoteList2.route) {
            NoteList2Screen(viewModel)
        }
        composable(Destino.NoteCreation.route) {
            NoteCreationScreen(viewModel)
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

@Composable
fun EcraLoginFirebase(navController: NavController) {
    val oContexto = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf(false) }
    val passwordError = remember { mutableStateOf(false) }
    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = {
                result -> logAndToast(oContexto, "info", oContexto.getString(R.string.firebase_login_success))
            navController.navigate(Destino.EcraSettings.route)
        },
        onAuthError = { error -> logAndToast(oContexto, "error", "signInWithGoogle:failure$error") }
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.firebase_login_title),
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Replace with the actual image resource
        Image(
            painter = painterResource(id = R.drawable.firebase),
            contentDescription = "Firebase Logo"
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError.value = !isValidEmail(it)
            },
            label = { Text(text = stringResource(id = R.string.firebase_enter_email)) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            isError = emailError.value,
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // Align content to the start
            ) {
                Text(
                    text = stringResource(id = R.string.firebase_enter_valid_email),
                    color = Color.Red
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError.value = !isValidPassword(it)
            },
            label = { Text(text = stringResource(id= R.string.firebase_enter_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            isError = passwordError.value,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // Align content to the start
            ) {
                Text(text = stringResource(id = R.string.firebase_password_min6chars),
                    color = Color.Red
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { performLogin(oContexto, email, password, emailError, passwordError, navController) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOG IN")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.firebase_no_account_question))
            TextButton(onClick = { navController.navigate(Destino.EcraRegisterToFirebase.route) }) {
                Text(text = stringResource(id = R.string.firebase_sign_up), color = Color.Blue)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(id=R.string.firebase_or))
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { performGoogleAuthentication(launcher, oContexto) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.firebase_continue_with_google), color = Color.White)
        }
    }
}

@Composable
fun EcraLoginFirebase(navController: NavController) {
    val oContexto = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf(false) }
    val passwordError = remember { mutableStateOf(false) }
    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = {
                result -> logAndToast(oContexto, "info", oContexto.getString(R.string.firebase_login_success))
            navController.navigate(Destino.EcraSettings.route)
        },
        onAuthError = { error -> logAndToast(oContexto, "error", "signInWithGoogle:failure$error") }
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.firebase_login_title),
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Replace with the actual image resource
        Image(
            painter = painterResource(id = R.drawable.firebase),
            contentDescription = "Firebase Logo"
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError.value = !isValidEmail(it)
            },
            label = { Text(text = stringResource(id = R.string.firebase_enter_email)) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            isError = emailError.value,
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // Align content to the start
            ) {
                Text(
                    text = stringResource(id = R.string.firebase_enter_valid_email),
                    color = Color.Red
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError.value = !isValidPassword(it)
            },
            label = { Text(text = stringResource(id= R.string.firebase_enter_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            isError = passwordError.value,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // Align content to the start
            ) {
                Text(text = stringResource(id = R.string.firebase_password_min6chars),
                    color = Color.Red
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { performLogin(oContexto, email, password, emailError, passwordError, navController) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOG IN")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.firebase_no_account_question))
            TextButton(onClick = { navController.navigate(Destino.EcraRegisterToFirebase.route) }) {
                Text(text = stringResource(id = R.string.firebase_sign_up), color = Color.Blue)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(id=R.string.firebase_or))
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { performGoogleAuthentication(launcher, oContexto) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.firebase_continue_with_google), color = Color.White)
        }
    }
}
