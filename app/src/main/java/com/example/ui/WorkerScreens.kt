package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Job
import com.example.data.JobApplication
import com.example.data.LabNexViewModel
import com.example.data.Screen
import com.example.data.User
import kotlinx.coroutines.launch

// ==========================================
// 1. WORKER ONBOARDING FLOW (6 Steps)
// ==========================================

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkerOnboardingScreen(
    viewModel: LabNexViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardingStep.collectAsState()
    val tempUser by viewModel.tempOnboardingUser.collectAsState()
    val scope = rememberCoroutineScope()

    val totalSteps = 6

    if (tempUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Worker Profile Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF191624)),
                navigationIcon = {
                    if (step > 1) {
                        IconButton(onClick = { viewModel.prevOnboardingStep() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.logout() }) {
                        Text("Log Out", color = Color(0xFFFF5252), fontSize = 14.sp)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF252136),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step $step of $totalSteps",
                        color = Color(0xFF90A4AE),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = {
                            viewModel.nextOnboardingStep(totalSteps) {
                                // On finishing successful completion
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD3A4FF),
                            contentColor = Color(0xFF191624)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("onboarding_continue_button")
                    ) {
                        Text(
                            text = if (step == totalSteps) "Finish Setup" else "Continue",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF191624),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Setup step-indicator dots bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == step) 14.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (i <= step) Color(0xFF00FFC4) else Color.White.copy(alpha = 0.2f))
                    )
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                },
                label = "OnboardingTransition"
            ) { currentStep ->
                when (currentStep) {
                    1 -> SkillsSelectionStep(viewModel, tempUser!!)
                    2 -> ExperienceSelectionStep(viewModel, tempUser!!)
                    3 -> LocationStep(viewModel, tempUser!!)
                    4 -> CertificationsStep(viewModel, tempUser!!)
                    5 -> PortfolioStep(viewModel, tempUser!!)
                    6 -> PricingStep(viewModel, tempUser!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSelectionStep(viewModel: LabNexViewModel, user: User) {
    val predefinedSkills = listOf(
        "Carpentry", "Electrical", "Framing", "Welding", "Plumbing", "HVAC",
        "Roofing", "Masonry", "Drywall", "Painting", "Safety", "Wiring"
    )
    val selectedList = remember(user.skills) {
        user.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableStateList()
    }

    var customSkillInp by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "What are your skills?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Select all categories that match your professional training. Contractors prioritize matches with fully specified skills.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customSkillInp,
                onValueChange = { customSkillInp = it },
                label = { Text("Add custom skill", color = Color(0xFFB0BEC5)) },
                textStyle = TextStyle(color = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFC4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (customSkillInp.isNotBlank()) {
                        val trimmed = customSkillInp.trim()
                        if (!selectedList.contains(trimmed)) {
                            selectedList.add(trimmed)
                            viewModel.updateTempUser { it.copy(skills = selectedList.joinToString(",")) }
                        }
                        customSkillInp = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4)),
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add custom skill", tint = Color(0xFF0F2027))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            predefinedSkills.forEach { item ->
                val isSelected = selectedList.contains(item)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedList.remove(item)
                        } else {
                            selectedList.add(item)
                        }
                        viewModel.updateTempUser { it.copy(skills = selectedList.joinToString(",")) }
                    },
                    label = { Text(item, color = if (isSelected) Color(0xFF0F2027) else Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00FFC4),
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    modifier = Modifier.testTag("chip_$item")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Selected (${selectedList.size}):",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = if (selectedList.isEmpty()) "None" else selectedList.joinToString(", "),
            color = Color(0xFF00FFC4),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ExperienceSelectionStep(viewModel: LabNexViewModel, user: User) {
    val experienceOptions = listOf(
        "Under 2 years" to 1,
        "2 to 5 years" to 3,
        "5 to 10 years" to 7,
        "10+ years" to 12
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Experience Level",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Select your range of project experience. This helps estimate standard pricing rates dynamically.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        experienceOptions.forEach { (label, years) ->
            val isSelected = user.yearsExperience == years
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF00FFC4).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00FFC4) else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable {
                        viewModel.updateTempUser { it.copy(yearsExperience = years) }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = Color.White, fontWeight = FontWeight.SemiBold)
                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.updateTempUser { it.copy(yearsExperience = years) } },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00FFC4), unselectedColor = Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationStep(viewModel: LabNexViewModel, user: User) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Location & Bounds",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Specify your home city and the travel radius within which you accept contractor offers.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = user.city,
            onValueChange = { val str = it; viewModel.updateTempUser { it.copy(city = str) } },
            label = { Text("Primary City (e.g. New York)", color = Color(0xFFB0BEC5)) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Text(
            text = "Travel Radius: ${user.radiusSec} miles",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Slider(
            value = user.radiusSec.toFloat(),
            onValueChange = { viewModel.updateTempUser { u -> u.copy(radiusSec = it.toInt()) } },
            valueRange = 5f..100f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00FFC4),
                activeTrackColor = Color(0xFF00FFC4),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(
            text = "Target Weekly Working Hours: ${user.workHoursPerWeek} hrs",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Slider(
            value = user.workHoursPerWeek.toFloat(),
            onValueChange = { viewModel.updateTempUser { u -> u.copy(workHoursPerWeek = it.toInt()) } },
            valueRange = 10f..60f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00FFC4),
                activeTrackColor = Color(0xFF00FFC4),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CertificationsStep(viewModel: LabNexViewModel, user: User) {
    var certInput by remember { mutableStateOf("") }
    val certsList = remember(user.certifications) {
        user.certifications.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableStateList()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Certifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Add construction safety badges, licenses, union status, or trade-school credentials to build quick contractor trust.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = certInput,
                onValueChange = { certInput = it },
                label = { Text("Certified Badge/License Name", color = Color(0xFFB0BEC5)) },
                placeholder = { Text("e.g. OSHA 30 Certified") },
                textStyle = TextStyle(color = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFC4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (certInput.isNotBlank()) {
                        val trimmed = certInput.trim()
                        if (!certsList.contains(trimmed)) {
                            certsList.add(trimmed)
                            viewModel.updateTempUser { it.copy(certifications = certsList.joinToString(",")) }
                        }
                        certInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4)),
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add cert", tint = Color(0xFF0F2027))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Active Certifications (${certsList.size})",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (certsList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "No certifications added yet. Enter safety/trade certifications list above.",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                certsList.forEach { cert ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF00FFC4).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00FFC4), RoundedCornerShape(30.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cert, color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove cert",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        certsList.remove(cert)
                                        viewModel.updateTempUser { it.copy(certifications = certsList.joinToString(",")) }
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Simulated Badge Verification Upload Layout Block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Verification File Attachments", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Upload PDFs or JPGs of physical tickets or certificates for official LabNex verification badges.",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Button(
                    onClick = {
                        // Sim file select
                        viewModel.updateTempUser { it.copy(portfolioPhotoUri = "verified_docs_attached.pdf") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        if (user.portfolioPhotoUri.contains("pdf")) "✓ Document Attached" else "Select Document",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PortfolioStep(viewModel: LabNexViewModel, user: User) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Portfolio Setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Tell contractors about your specialties and showcase pre-existing project finishes.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = user.portfolioDesc,
            onValueChange = { txt -> viewModel.updateTempUser { it.copy(portfolioDesc = txt) } },
            label = { Text("Profile Bio / Job Portfolio Description", color = Color(0xFFB0BEC5)) },
            placeholder = { Text("Write about your commercial project specialties...") },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(bottom = 20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            maxLines = 4
        )

        Text("Photo Uploads & Gallery Preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
            "Upload image assets of previous carpentry joints, piping manifolds, weld beads, or electrical closets.",
            color = Color(0xFF90A4AE),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Simulated Image Upload Layout Grid Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable {
                    viewModel.updateTempUser { it.copy(portfolioPhotoUri = "asset_uploaded_profile.png") }
                },
            contentAlignment = Alignment.Center
        ) {
            if (user.portfolioPhotoUri.contains("png")) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("asset_uploaded_profile.png", color = Color(0xFF00FFC4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Tap to change uploaded photo asset", color = Color(0xFF90A4AE), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF90A4AE), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Select Showcase Photo", color = Color.White, fontSize = 13.sp)
                    Text("Simulate image gallery upload", color = Color(0xFF90A4AE), fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingStep(viewModel: LabNexViewModel, user: User) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Set Pricing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Select your core hourly rate or general bidding baseline. LabNex calculates market standards to verify competitiveness.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Selected rate input field
        OutlinedTextField(
            value = if (user.hourlyRate == 0.0) "" else user.hourlyRate.toInt().toString(),
            onValueChange = {
                val value = it.toDoubleOrNull() ?: 0.0
                viewModel.updateTempUser { u -> u.copy(hourlyRate = value) }
            },
            label = { Text("Hourly Billing Rate ($/hr)", color = Color(0xFFB0BEC5)) },
            textStyle = TextStyle(color = Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Slider(
            value = user.hourlyRate.toFloat(),
            onValueChange = { viewModel.updateTempUser { u -> u.copy(hourlyRate = it.toInt().toDouble()) } },
            valueRange = 15f..120f,
            steps = 105,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00FFC4),
                activeTrackColor = Color(0xFF00FFC4),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Market Comparison Guidance Box (Dynamic UI metrics)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    user.hourlyRate < 25.0 -> Color(0xFF0D5C3A) // cheap
                    user.hourlyRate <= 60.0 -> Color(0xFF084B52) // competitive
                    else -> Color(0xFF5C2B0D) // premium / high
                }
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when {
                        user.hourlyRate < 25.0 -> "Market Comparison: Budget Entry Rate"
                        user.hourlyRate <= 60.0 -> "Market Comparison: Competitive Trade Average"
                        else -> "Market Comparison: Expert Premium Rate"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        user.hourlyRate < 25.0 -> "Your rate is below standard local averages. This will attract quick volume but could undervalue your advanced tools & certifications."
                        user.hourlyRate <= 60.0 -> "Your rate matches standard trade brackets (Carpentry average $35-$48/hr; Welding average $45-$65/hr). This ensures consistent bid match percentages!"
                        else -> "Your rate is in the top 10% premium. Ensure your portfolio shows off certified commercial safety tickets and heavy tooling capabilities to validate this tier to contractors."
                    },
                    color = Color(0xFFECEFF1),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ==========================================
// 2. WORKER MAIN DASHBOARD (7 Tabs)
// ==========================================

@Composable
fun WorkerDashboardScreen(
    viewModel: LabNexViewModel,
    onBidTap: (jobId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.workerTab.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    Scaffold(
        topBar = {
            Column {
                Surface(
                    color = Color(0xFF0F2027),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back, Worker",
                                fontSize = 12.sp,
                                color = Color(0xFF90A4AE),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user!!.fullName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Compact header indicators
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00FFC4).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF00FFC4), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified", color = Color(0xFF00FFC4), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E2F35),
                windowInsets = WindowInsets.navigationBars,
                tonalElevation = 10.dp
            ) {
                val tabs = listOf(
                    Triple(0, Icons.Filled.Feed, Icons.Outlined.Feed),
                    Triple(1, Icons.Filled.Search, Icons.Outlined.Search),
                    Triple(2, Icons.Filled.FolderOpen, Icons.Outlined.FolderOpen),
                    Triple(3, Icons.Filled.Schedule, Icons.Outlined.Schedule),
                    Triple(4, Icons.Filled.Chat, Icons.Outlined.Chat),
                    Triple(5, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                    Triple(6, Icons.Filled.Person, Icons.Outlined.Person)
                )

                tabs.forEach { (index, filled, outlined) ->
                    val isSelected = activeTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setWorkerTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) filled else outlined,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF191624) else Color.White
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFD3A4FF),
                            unselectedTextColor = Color(0xFF90A4AE),
                            selectedTextColor = Color(0xFFD3A4FF)
                        ),
                        modifier = Modifier.testTag("nav_worker_$index")
                    )
                }
            }
        },
        containerColor = Color(0xFF191624),
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                0 -> WorkerFeedTab(viewModel, onBidTap)
                1 -> WorkerSearchTab(viewModel, onBidTap)
                2 -> WorkerApplicationsTab(viewModel)
                3 -> WorkerActiveJobsTab(viewModel)
                4 -> WorkerChatTab(viewModel)
                5 -> WorkerWalletTab(viewModel)
                6 -> WorkerProfileTab(viewModel)
            }
        }
    }
}

// ==========================================
// Worker Tab 0: Home / Job Feed
// ==========================================
@Composable
fun WorkerFeedTab(viewModel: LabNexViewModel, onBidTap: (jobId: Int) -> Unit) {
    val jobs by viewModel.allJobs.collectAsState()
    val myUser by viewModel.currentUser.collectAsState()

    val openJobs = remember(jobs) { jobs.filter { it.status == "OPEN" } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "Available Job Feed",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (openJobs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WorkOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active jobs open at the moment.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(openJobs) { job ->
                JobFeedCard(job = job, user = myUser, onBidTap = onBidTap)
            }
        }
    }
}

@Composable
fun JobFeedCard(job: Job, user: User?, onBidTap: (jobId: Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Posted by ${job.contractorName}",
                        color = Color(0xFF90A4AE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Match Badge calculation based on user skills
                val skillsMatch = remember(job.skillsRequired, user?.skills) {
                    if (user == null || job.skillsRequired.isBlank()) 0
                    else {
                        val wSkills = user.skills.split(",").map { it.trim().lowercase() }
                        val jSkills = job.skillsRequired.split(",").map { it.trim().lowercase() }
                        val overlap = wSkills.intersect(jSkills.toSet()).size
                        if (jSkills.isEmpty()) 0 else ((overlap.toFloat() / jSkills.size) * 100).toInt()
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (skillsMatch >= 75) Color(0xFF00FFC4).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$skillsMatch% Match",
                        color = if (skillsMatch >= 75) Color(0xFF00FFC4) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = job.description,
                color = Color(0xFFECEFF1),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Required Skills row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Skills required: ${job.skillsRequired}",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BUDGET RANGE", fontSize = 9.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                    Text("$${job.budgetMin.toInt()} - $${job.budgetMax.toInt()}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = { onBidTap(job.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                ) {
                    Text("Place Bid", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ==========================================
// Worker Tab 1: Search & Filter Custom Feed
// ==========================================
@Composable
fun WorkerSearchTab(viewModel: LabNexViewModel, onBidTap: (jobId: Int) -> Unit) {
    val filterQuery by viewModel.searchFilterQuery.collectAsState()
    val minBudget by viewModel.minBudgetFilter.collectAsState()
    val maxBudget by viewModel.maxBudgetFilter.collectAsState()
    val selectedSkill by viewModel.selectedSkillFilter.collectAsState()

    val jobs by viewModel.allJobs.collectAsState()
    val myUser by viewModel.currentUser.collectAsState()

    val searchList = remember(jobs, filterQuery, minBudget, maxBudget, selectedSkill) {
        jobs.filter {
            it.status == "OPEN" &&
            (filterQuery.isBlank() || it.title.contains(filterQuery, true) || it.description.contains(filterQuery, true)) &&
            it.budgetMin >= minBudget &&
            it.budgetMax <= maxBudget &&
            (selectedSkill == "All" || it.skillsRequired.contains(selectedSkill, true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "Advanced Job Search",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search text field
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { viewModel.searchFilterQuery.value = it },
                placeholder = { Text("Search by title, framing, truss, PLC wiring...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color(0xFF00FFC4)) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("worker_search_bar"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFC4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )

            // Dynamic filter options
            Text("QUICK TRADE FILTER", color = Color(0xFF00FFC4), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            val trades = listOf("All", "Carpentry", "Electrical", "Welding", "Plumbing", "Roofing", "Wiring")
            LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                items(trades) { item ->
                    val active = selectedSkill == item
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) Color(0xFF00FFC4) else Color.White.copy(alpha = 0.05f))
                            .clickable { viewModel.selectedSkillFilter.value = item }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(item, color = if (active) Color(0xFF0F2027) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Budget sliders
            Text("BUDGET SLIDER LIMIT ($${minBudget.toInt()} - $${maxBudget.toInt()})", color = Color(0xFF00FFC4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            RangeSlider(
                value = minBudget..maxBudget,
                onValueChange = {
                    viewModel.minBudgetFilter.value = it.start
                    viewModel.maxBudgetFilter.value = it.endInclusive
                },
                valueRange = 0f..10000f,
                colors = SliderDefaults.colors(
                    activeTickColor = Color(0xFF00FFC4),
                    activeTrackColor = Color(0xFF00FFC4),
                    thumbColor = Color(0xFF00FFC4)
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        if (searchList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No jobs found matching your advanced filters.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(searchList) { job ->
                JobFeedCard(job = job, user = myUser, onBidTap = onBidTap)
            }
        }
    }
}

// ==========================================
// Worker Tab 2: My Applications / Bidding Status
// ==========================================
@Composable
fun WorkerApplicationsTab(viewModel: LabNexViewModel) {
    val applications by viewModel.mySubmittedApplications.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "My Bids & Proposals",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Active bids submitted to contractors. Accepting offers marks jobs in progress and triggers chat interfaces instantly.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (applications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active bids found. Browse feed to bid!", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(applications) { app ->
                BidApplicationCard(app = app, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BidApplicationCard(app: JobApplication, viewModel: LabNexViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.jobTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Bid amount: $${app.bidAmount.toInt()}", color = Color(0xFF00FFC4), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                // Match Badge and Status Badge
                Surface(
                    color = when (app.status) {
                        "PENDING" -> Color(0xFFFFA000).copy(alpha = 0.15f)
                        "ACCEPTED" -> Color(0xFF00FFC4).copy(alpha = 0.15f)
                        "REJECTED" -> Color(0xFFFF5252).copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.05f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        app.status,
                        color = when (app.status) {
                            "PENDING" -> Color(0xFFFFA000)
                            "ACCEPTED" -> Color(0xFF00FFC4)
                            "REJECTED" -> Color(0xFFFF5252)
                            else -> Color.White
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Cover Letter:\n\"${app.coverLetter}\"",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB0BEC5),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (app.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.withdrawBid(app.id) {} },
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Withdraw Bid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// Worker Tab 3: Active Jobs Tracker
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerActiveJobsTab(viewModel: LabNexViewModel) {
    val activeJobs by viewModel.workerActiveJobs.collectAsState()
    val scope = rememberCoroutineScope()

    var showHoursDialog by remember { mutableStateOf(false) }
    var selectedJobIdForHours by remember { mutableStateOf(0) }
    var hoursInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "Active Project Tracker",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Manage framing assemblies, log billable work hours, or view current contractor milestones.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        val inProgressJobs = activeJobs.filter { it.status == "IN_PROGRESS" }

        if (inProgressJobs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Task, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active work orders right now.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(inProgressJobs) { job ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(job.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("In Progress", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("Contractor: ${job.contractorName}", color = Color(0xFFB0BEC5), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Logged Hours", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                                Text("${job.completedHours} hrs", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text("Project Budget", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                                Text("$${job.budgetMin.toInt()} - $${job.budgetMax.toInt()}", color = Color(0xFF00FFC4), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    selectedJobIdForHours = job.id
                                    hoursInput = ""
                                    showHoursDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Hours", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.navigateTo(Screen.Transactions(job.id)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Finish & Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHoursDialog) {
        AlertDialog(
            onDismissRequest = { showHoursDialog = false },
            title = { Text("Log Working Hours", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Add your registered billable hours to sync immediately with the contractor's dashboard.", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = { hoursInput = it },
                        label = { Text("Hours completed today", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FFC4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hrs = hoursInput.toDoubleOrNull() ?: 0.0
                        if (hrs > 0) {
                            viewModel.submitHoursLogged(selectedJobIdForHours, hrs) {
                                showHoursDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027))
                ) {
                    Text("Confirm Logs", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHoursDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E2F35)
        )
    }
}

// ==========================================
// Worker Tab 4: Messages / Chats Contacts
// ==========================================
@Composable
fun WorkerChatTab(viewModel: LabNexViewModel) {
    val myMessages by viewModel.myMessages.collectAsState()
    val rawUser by viewModel.currentUser.collectAsState()

    // Filter message threads (distinct channels based on user chat)
    val conversationPartners = remember(myMessages, rawUser) {
        val user = rawUser
        if (user == null) emptyList()
        else {
            val uId = user.id
            val map = mutableMapOf<Int, String>()
            myMessages.forEach { msg ->
                if (msg.senderId != uId) {
                    map[msg.senderId] = msg.senderName
                } else if (msg.receiverId != uId) {
                    // Find contractor name (since contractor info is sender context, we can lookup or fall back)
                    map[msg.receiverId] = "Contractor Thread" 
                }
            }
            map.toList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "Inbox & Offers",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Chat logs regarding active quotes, bidding adjustments, or custom payment releases.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        if (conversationPartners.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MarkChatRead, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active conversation threads. Bids create channels!", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(conversationPartners) { (partnerId, partnerName) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { viewModel.navigateTo(Screen.ChatRoom(partnerId)) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FFC4).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00FFC4))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(partnerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Click to view chat history and negotiate details", color = Color(0xFF90A4AE), fontSize = 11.sp)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00FFC4))
                    }
                }
            }
        }
    }
}

// ==========================================
// Worker Tab 5: Wallet & Cash Out Options
// ==========================================
@Composable
fun WorkerWalletTab(viewModel: LabNexViewModel) {
    val user by viewModel.currentUser.collectAsState()
    var withdrawOpen by remember { mutableStateOf(false) }
    var withdrawAmountInp by remember { mutableStateOf("") }
    var accountNoInp by remember { mutableStateOf("") }
    var transferStatus by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "My Earnings & Wallet",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Balance Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                border = BorderStroke(1.dp, Color(0xFF00FFC4).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TOTAL WALLET BALANCE", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                    Text("$${user?.totalEarnings?.toInt() ?: 0}.00", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("All payments from contractors are settled locally in USD upon digital completion agreement signatures.", color = Color(0xFF90A4AE), fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { withdrawOpen = !withdrawOpen; transferStatus = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.LocalAtm, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Withdraw to Bank Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sim cash withdrawal drawer layout
        if (withdrawOpen) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF122329)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("SIMULATED TRANSFER INITIATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FFC4))

                        Spacer(modifier = Modifier.height(10.dp))

                        if (transferStatus.isNotEmpty()) {
                            Text(transferStatus, color = Color(0xFF00FFC4), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        OutlinedTextField(
                            value = withdrawAmountInp,
                            onValueChange = { withdrawAmountInp = it },
                            label = { Text("Withdrawal Amount ($)", color = Color.White) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = accountNoInp,
                            onValueChange = { accountNoInp = it },
                            label = { Text("Local Routing/Account Number", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val currentBal = user?.totalEarnings ?: 0.0
                                    val amt = withdrawAmountInp.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0) {
                                        transferStatus = "Error: Input a valid positive number!"
                                    } else if (amt > currentBal) {
                                        transferStatus = "Error: Insufficient balance limits!"
                                    } else {
                                        transferStatus = "✓ Success! $${amt} transferred to Routing ${accountNoInp}. Safe settlement usually clears in 10 mins."
                                        val newEarn = currentBal - amt
                                        viewModel.updateCompletedUserInfo(user!!.copy(totalEarnings = newEarn)) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Transfer", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { withdrawOpen = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Custom canvas bar-graph rendering past months' cashflow! (Visual Design mandated)
        item {
            Text("6-MONTH CASHFLOW HISTORY", color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                        val values = listOf(2200f, 3500f, 1800f, 4900f, 6200f, 5400f)
                        val maxVal = 7000f

                        val spacingX = size.width / (months.size + 1)
                        val baselineY = size.height - 25f

                        // Draw baseline
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(10f, baselineY),
                            end = Offset(size.width - 10f, baselineY),
                            strokeWidth = 2f
                        )

                        // Draw columns
                        values.forEachIndexed { idx, value ->
                            val x = spacingX * (idx + 1)
                            val barHeight = (value / maxVal) * (size.height - 50f)
                            val startY = baselineY - barHeight

                            drawLine(
                                color = Color(0xFF00FFC4),
                                start = Offset(x, baselineY),
                                end = Offset(x, startY),
                                strokeWidth = 24f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun").forEach {
                            Text(it, color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Worker Tab 6: Profile & Reviews Checklist
// ==========================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileTab(viewModel: LabNexViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val reviews by user?.let { viewModel.repository.getReviewsForUser(it.id).collectAsState(emptyList()) } ?: remember { mutableStateOf(emptyList()) }

    var editMode by remember { mutableStateOf(false) }

    // local editing state
    var editName by remember { mutableStateOf(user?.fullName ?: "") }
    var editPhone by remember { mutableStateOf(user?.phone ?: "") }
    var editCity by remember { mutableStateOf(user?.city ?: "") }
    var editBio by remember { mutableStateOf(user?.portfolioDesc ?: "") }

    if (user == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Professional Profile",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )

                TextButton(onClick = {
                    if (editMode) {
                        // Save changes
                        val updated = user!!.copy(
                            fullName = editName,
                            phone = editPhone,
                            city = editCity,
                            portfolioDesc = editBio
                        )
                        viewModel.updateCompletedUserInfo(updated) {
                            editMode = false
                        }
                    } else {
                        editName = user!!.fullName
                        editPhone = user!!.phone
                        editCity = user!!.city
                        editBio = user!!.portfolioDesc
                        editMode = true
                    }
                }) {
                    Text(
                        text = if (editMode) "Save Profile" else "Edit details",
                        color = Color(0xFF00FFC4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (editMode) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)), modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = editCity,
                            onValueChange = { editCity = it },
                            label = { Text("Primary City", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Portfolio Bio", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Show User details card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FFC4).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user!!.fullName.take(1),
                            color = Color(0xFF00FFC4),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(user!!.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("${user!!.yearsExperience} Years Trade Experience • ${user!!.city}", color = Color(0xFF90A4AE), fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", user!!.rating)} (${user!!.reviewsCount} contractor reviews)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Bio Section
        item {
            Text("PORTFOLIO BIO DESCRIPTION", color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                text = if (user!!.portfolioDesc.isBlank()) "No bio provided yet." else user!!.portfolioDesc,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Skills chips list
        item {
            Text("REGISTERED COGNITIVE SKILLS", color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                user!!.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(skill, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Certifications checklist
        item {
            Text("VERIFIED CERTIFICATIONS", color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            if (user!!.certifications.isBlank()) {
                Text("No verified certifications added.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    user!!.certifications.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { cert ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cert, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Ratings review list
        item {
            Text("CONTRACTOR FEEDBACK CORNER", color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (reviews.isEmpty()) {
            item {
                Text("No contractor reviews on profile yet.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            items(reviews) { r ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(r.reviewerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(r.rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Project: ${r.jobTitle}", color = Color(0xFF90A4AE), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(r.reviewText, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}
