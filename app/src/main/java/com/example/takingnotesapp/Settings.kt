package com.example.takingnotesapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun EcraSettings(navController: NavController) {
    val oContexto = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val authMethod = getCurrentUserAuthMethod(currentUser)
    val showReauthenPassword = remember { mutableStateOf(false) }

    // For Google Authentication
    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = { result ->
            Log.d("ReauthenticatSuccess", "ReauthenticatWithGoogle:success")
            deleteAccount(oContexto, currentUser, navController)
        },
        onAuthError = { error ->
            Log.w("ReauthenticateFail", "ReauthenticatWithGoogle:failure$error")
        }
    )


    Scaffold {it: PaddingValues->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {


                Text(text = stringResource(R.string.user_logged_in))

                // This spacer will push the buttons to the bottom as it takes up all available space
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { performLogout(oContexto, navController) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("LOG OUT", color = Color.Black)
                }

                Button(
                    onClick = {
                        when (authMethod) {
                            AuthMethod.EMAIL -> {
                                println("User signed in using Email")
                                // Handle email authenticated user
                                showReauthenPassword.value = true
                            }
                            AuthMethod.GOOGLE -> {
                                println("User signed in using Google")
                                // Handle Google authenticated user
                                performGoogleReauthentication(launcher, oContexto)
                            }
                            AuthMethod.NONE -> {
                                println("No user is signed in")
                                // Handle case where no user is signed in
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("DELETE ACCOUNT", color = Color.White)
                }
            }

            if (showReauthenPassword.value) {
                ReauthenticationEmailPassWord(
                    showDialog = showReauthenPassword,
                    onReauthenticated = {
                        // Handle successful reauthentication, e.g., navigate, show message
                        logAndToast(oContexto, "info", "reauthentication successful")
                        deleteAccount(oContexto, currentUser, navController)
                    },
                    onError = { errorMsg ->
                        // Handle error, e.g., show error message
                        logAndToast(oContexto, "error", "reauthentication error:$errorMsg")
                        showReauthenPassword.value = false
                    }
                )
            }

        }
    }
}

private fun performLogout(oContexto: Context, navController: NavController) {
    Firebase.auth.signOut()
    logAndToast(oContexto, "info", "Logged out")
    navController.popBackStack(route = Destino.EcraLoginFirebase.route, inclusive = false)
}

private fun deleteAccount(oContexto: Context, user: FirebaseUser?, navController: NavController) {
    user?.let {
        it.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    logAndToast(oContexto, "info", "User account successfully deleted")
                    navController.popBackStack(route = Destino.EcraLoginFirebase.route, inclusive = false)
                } else {
                    logAndToast(oContexto, "error", "User account not deleted")
                }
            }
    }
}

enum class AuthMethod {
    EMAIL,
    GOOGLE,
    NONE   // Use this when no user is logged in or others method found
}

private fun getCurrentUserAuthMethod(user: FirebaseUser?): AuthMethod {
    user?.let {
        for (profile in it.providerData) {
            Log.d("User providerId", profile.providerId)
            return when (profile.providerId) {
                "password" -> AuthMethod.EMAIL
                "google.com" -> AuthMethod.GOOGLE
                else -> continue
            }
        }
    }
    return AuthMethod.NONE // Return this if no user is logged in or no provider data found
}

@Composable
private fun ReauthenticationEmailPassWord(showDialog: MutableState<Boolean>, onReauthenticated: () -> Unit, onError: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error = remember { mutableStateOf("") }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = { Text("Reauthenticate") },
            text = {
                Column {
                    if (error.value.isNotEmpty()) {
                        Text(error.value, color = Color.Red)
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") }
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = FirebaseAuth.getInstance().currentUser
                        val credential = EmailAuthProvider.getCredential(email, password)
                        user?.reauthenticate(credential)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    showDialog.value = false
                                    onReauthenticated()
                                } else {
                                    error.value = task.exception?.message ?: "Reauthentication failed"
                                    onError(error.value)
                                }
                            }
                    }
                ) {
                    Text("Reauthenticate")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        )
    }
}

@Composable
private fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        } catch (e: ApiException) {
            onAuthError(e)
        }
    }
}