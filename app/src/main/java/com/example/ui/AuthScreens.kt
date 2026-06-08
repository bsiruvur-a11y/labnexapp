package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabNexViewModel
import com.example.data.Screen

@Composable
fun WelcomeScreen(
    viewModel: LabNexViewModel,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf("WORKER") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF191624),
                        Color(0xFF27223D),
                        Color(0xFF332D4E)
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // LabNex Custom Visual Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFD3A4FF).copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = "LabNex Logo Icon",
                    tint = Color(0xFFD3A4FF),
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LabNex",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Connecting Craft to Enterprise",
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Role selection overview
            Text(
                text = "I WANT TO ENTER AS:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD3A4FF),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedRole == "WORKER") Color(0xFFD3A4FF).copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { selectedRole = "WORKER" }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Worker Role",
                            tint = if (selectedRole == "WORKER") Color(0xFFD3A4FF) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Skilled Worker",
                            color = if (selectedRole == "WORKER") Color(0xFFD3A4FF) else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedRole == "CONTRACTOR") Color(0xFFD3A4FF).copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { selectedRole = "CONTRACTOR" }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Contractor Role",
                            tint = if (selectedRole == "CONTRACTOR") Color(0xFFD3A4FF) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contractor",
                            color = if (selectedRole == "CONTRACTOR") Color(0xFFD3A4FF) else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Information Card describing selected role's capabilities
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (selectedRole == "WORKER") "✓ Access Local Bidding Feed\n✓ Showcase Skills & Certifications\n✓ Track Project Milestones & Rates\n✓ Coordinate directly via instant messaging"
                        else "✓ Post Projects with budget parameters\n✓ Filter profiles by Skill Match\n✓ Review offers & accept bids instantly\n✓ Approve logs & release secure payments",
                        color = Color(0xFFECEFF1),
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // CTAs
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3A4FF),
                    contentColor = Color(0xFF191624)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("welcome_login_button")
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.SignUp) },
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("welcome_signup_button")
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LabNexViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("alex@worker.com") } // Pre-filled for easy testing flow
    var password by remember { mutableStateOf("123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF191624))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252136)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color(0xFF00FFC4),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Enter your details to coordinate project matches",
                    fontSize = 13.sp,
                    color = Color(0xFF90A4AE),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF5252).copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("Email Address", color = Color(0xFFB0BEC5)) },
                    placeholder = { Text("email@domain.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFD3A4FF)) },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("login_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD3A4FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFFD3A4FF)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("Password", color = Color(0xFFB0BEC5)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD3A4FF)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFFD3A4FF)
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color.White),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("login_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD3A4FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFFD3A4FF)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                // Demo account options quick-select buttons helper
                Text(
                    text = "DEMO ACCOUNTS (TAP QUICK LOGIN):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD3A4FF),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SuggestionChip(
                        onClick = {
                            email = "alex@worker.com"
                            password = "123"
                        },
                        label = { Text("Alex (Worker)", color = Color.White, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.05f))
                    )
                    SuggestionChip(
                        onClick = {
                            email = "sarah@worker.com"
                            password = "123"
                        },
                        label = { Text("Sarah (Worker)", color = Color.White, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.05f))
                    )
                    SuggestionChip(
                        onClick = {
                            email = "john@co.com"
                            password = "123"
                        },
                        label = { Text("John (Contractor)", color = Color.White, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.05f))
                    )
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please fill in all details."
                            return@Button
                        }
                        isLoading = true
                        viewModel.login(email, password) { success, msg ->
                            isLoading = false
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD3A4FF),
                        contentColor = Color(0xFF191624)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_login_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF191624), modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.navigateTo(Screen.SignUp) }) {
                    Text(
                        text = "Don't have an account? Sign Up",
                        color = Color(0xFFD3A4FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                TextButton(onClick = { viewModel.navigateTo(Screen.Welcome) }) {
                    Text(
                        text = "← Back to Welcome Selection",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: LabNexViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("WORKER") } // WORKER or CONTRACTOR
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF191624))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252136)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Color(0xFF00FFC4),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Welcome to LabNex workspace",
                    fontSize = 13.sp,
                    color = Color(0xFF90A4AE),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF5252).copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Role selection toggle
                Text(
                    text = "SELECT YOUR CORE ROLE:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FFC4),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(3.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (role == "WORKER") Color(0xFF00FFC4).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { role = "WORKER" }
                    ) {
                        Text(
                            "Skilled Worker",
                            color = if (role == "WORKER") Color(0xFF00FFC4) else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (role == "CONTRACTOR") Color(0xFF00FFC4).copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { role = "CONTRACTOR" }
                    ) {
                        Text(
                            "Contractor",
                            color = if (role == "CONTRACTOR") Color(0xFF00FFC4) else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    label = { Text("Full Name", color = Color(0xFFB0BEC5)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00FFC4)) },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("signup_fullname_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFC4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFF00FFC4)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = ""
                    },
                    label = { Text("Phone Number", color = Color(0xFFB0BEC5)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF00FFC4)) },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("signup_phone_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFC4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFF00FFC4)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("Email Address", color = Color(0xFFB0BEC5)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00FFC4)) },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("signup_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFC4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFF00FFC4)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("Password", color = Color(0xFFB0BEC5)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00FFC4)) },
                    textStyle = TextStyle(color = Color.White),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("signup_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FFC4),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFF00FFC4)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || name.isBlank() || phone.isBlank()) {
                            errorMessage = "Please fill in all fields."
                            return@Button
                        }
                        if (password.length < 3) {
                            errorMessage = "Password too short."
                            return@Button
                        }
                        isLoading = true
                        viewModel.signUp(email, password, name, phone, role) { success, msg ->
                            isLoading = false
                            if (!success) {
                                errorMessage = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFC4),
                        contentColor = Color(0xFF0F2027)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_signup_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF0F2027), modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.navigateTo(Screen.Login) }) {
                    Text(
                        text = "Already have an account? Sign In",
                        color = Color(0xFF00FFC4),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                TextButton(onClick = { viewModel.navigateTo(Screen.Welcome) }) {
                    Text(
                        text = "← Back to Welcome",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
