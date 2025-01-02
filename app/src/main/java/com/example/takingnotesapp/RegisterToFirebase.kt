package com.example.takingnotesapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EcraRegisterToFirebase(navController: NavController) {
    val oContexto = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf(false) }
    val passwordError = remember { mutableStateOf(false) }
    val confirmPasswordError = remember { mutableStateOf(false) }
    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = { result ->
            val aMensagem = oContexto.getString(R.string.firebase_login_success)
            Log.d("SignInSuccess", aMensagem)
            Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
            navController.navigate(Destino.NoteList1.route)
        },
        onAuthError = { error ->
            val aMensagem = "signInWithGoogle:failure$error"
            Log.w("SignInFail", aMensagem)
            Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
        }
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
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = passwordConfirmation,
            onValueChange = {
                passwordConfirmation = it
                confirmPasswordError.value = !isValidPassword(it)
            },
            label = { Text(text = stringResource(id= R.string.firebase_confirm_password)) },
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
            onClick = { performSignUp(oContexto, email, password, passwordConfirmation, emailError, passwordError, confirmPasswordError, navController) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
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
fun performSignUp(
    oContexto: Context,
    email: String,
    password: String,
    confirmPassword: String,
    emailErrorState: MutableState<Boolean>,
    passwordErrorState: MutableState<Boolean>,
    confirmPasswordErrorState: MutableState<Boolean>,
    navController: NavController
) {
    val isEmailValid = isValidEmail(email)
    val isPasswordValid = isValidPassword(password)
    val isConfirmPasswordValid = password == confirmPassword
    emailErrorState.value = !isEmailValid
    passwordErrorState.value = !isPasswordValid
    confirmPasswordErrorState.value = !isConfirmPasswordValid
    if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
        val auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If sign in fails, display a message to the user.
                    val aMensagem = oContexto.getString(R.string.firebase_login_ok)
                    Log.d("SignUpSuccess", aMensagem)
                    Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
                    navController.navigate(Destino.NoteList1.route)
                } else {
                    val aMensagem = oContexto.getString(R.string.firebase_email_password_login_error)
                    Log.d("SignUpFailed", aMensagem, task.exception)
                    Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
                }
            }
    } else {
        if (!isEmailValid) {
            val aMensagem = oContexto.getString(R.string.firebase_invalid_email)
            Log.d("SignUpFailed", aMensagem)
            Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
        } else if (!isPasswordValid) {
            val aMensagem = oContexto.getString(R.string.firebase_invalid_password)
            Log.d("SignUpFailed", aMensagem)
            Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
        } else if (!isConfirmPasswordValid) {
            val aMensagem = oContexto.getString(R.string.firebase_invalid_password_confirmation)
            Log.d("SignUpFailed", aMensagem)
            Toast.makeText(oContexto, aMensagem, Toast.LENGTH_SHORT).show()
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}