package com.foodbridge.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.foodbridge.app.data.model.UserRole
import com.foodbridge.app.util.UiState

/** FR-1: login screen. */
@Composable
fun LoginScreen(
    authState: UiState<*>,
    onLogin: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("FoodBridge", style = MaterialTheme.typography.headlineMedium)
        Text("Share surplus food before it goes to waste.", style = MaterialTheme.typography.bodyMedium)
        Column(modifier = Modifier.padding(top = 32.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            if (authState is UiState.Error) {
                Text(
                    authState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Button(
                onClick = { onLogin(email.trim(), password) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                if (authState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text("Log in")
                }
            }
            TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
                Text("New here? Create an account")
            }
        }
    }
}

/** FR-1: registration screen — captures name, role and location permission. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authState: UiState<*>,
    onRegister: (name: String, email: String, password: String, role: UserRole) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.BOTH) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create your account", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = roleMenuExpanded,
            onExpandedChange = { roleMenuExpanded = it },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = role.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("I mostly want to...") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = roleMenuExpanded,
                onDismissRequest = { roleMenuExpanded = false }
            ) {
                UserRole.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            role = option
                            roleMenuExpanded = false
                        }
                    )
                }
            }
        }

        if (authState is UiState.Error) {
            Text(
                authState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = { onRegister(name.trim(), email.trim(), password, role) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            if (authState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Text("Register")
            }
        }
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Text("Already have an account? Log in")
        }
    }
}
