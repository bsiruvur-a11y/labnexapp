package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
// 1. CONTRACTOR ONBOARDING (2 Steps)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorOnboardingScreen(
    viewModel: LabNexViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardingStep.collectAsState()
    val tempUser by viewModel.tempOnboardingUser.collectAsState()

    val totalSteps = 2

    if (tempUser == null) return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contractor Profile Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                            viewModel.nextOnboardingStep(totalSteps) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD3A4FF),
                            contentColor = Color(0xFF191624)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("contractor_onboarding_continue")
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
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (i <= step) Color(0xFF00FFC4) else Color.White.copy(alpha = 0.2f))
                    )
                }
            }

            if (step == 1) {
                CompanyDetailsStep(viewModel, tempUser!!)
            } else {
                ContractorPreferencesStep(viewModel, tempUser!!)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsStep(viewModel: LabNexViewModel, user: User) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Company Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Tell us about your management enterprise or contracting firm. This will be visible on all job postings.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = user.companyName,
            onValueChange = { str -> viewModel.updateTempUser { it.copy(companyName = str) } },
            label = { Text("Company Name", color = Color(0xFFB0BEC5)) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = user.industry,
            onValueChange = { str -> viewModel.updateTempUser { it.copy(industry = str) } },
            label = { Text("Industry Category (e.g. Drywall/Framing)", color = Color(0xFFB0BEC5)) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Text("COMPANY SIZE ESTIMATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))

        val sizeOptions = listOf("1-10 employees", "11-50 employees", "51-200 employees", "200+ employees")
        sizeOptions.forEach { opt ->
            val active = user.companySize == opt
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (active) Color(0xFF00FFC4).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (active) Color(0xFF00FFC4) else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { viewModel.updateTempUser { it.copy(companySize = opt) } }
            ) {
                Text(opt, color = Color.White, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContractorPreferencesStep(viewModel: LabNexViewModel, user: User) {
    val options = listOf("Carpentry", "Electrical", "Welding", "Plumbing", "Roofing", "Wiring", "Painting", "Masonry")
    val selectedList = remember(user.workTypesNeeded) {
        user.workTypesNeeded.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableStateList()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Job Preferences",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = "Select all technical classifications of workers that you frequently need for projects. We pre-filter bids matching this taxonomy.",
            fontSize = 13.sp,
            color = Color(0xFF90A4AE),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { item ->
                val active = selectedList.contains(item)
                FilterChip(
                    selected = active,
                    onClick = {
                        if (active) {
                            selectedList.remove(item)
                        } else {
                            selectedList.add(item)
                        }
                        viewModel.updateTempUser { it.copy(workTypesNeeded = selectedList.joinToString(",")) }
                    },
                    label = { Text(item, color = if (active) Color(0xFF0F2027) else Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00FFC4),
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                )
            }
        }

        OutlinedTextField(
            value = user.budgetRange,
            onValueChange = { str -> viewModel.updateTempUser { it.copy(budgetRange = str) } },
            label = { Text("Standard Material/Work Budget Range", color = Color(0xFFB0BEC5)) },
            placeholder = { Text("e.g. $5,000 - $100,000") },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )
    }
}

// ==========================================
// 2. CONTRACTOR DASHBOARD SCREEN (8 Tabs)
// ==========================================

@Composable
fun ContractorDashboardScreen(
    viewModel: LabNexViewModel,
    onViewChatTap: (workerId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.contractorTab.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    Scaffold(
        topBar = {
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
                            text = user!!.companyName,
                            fontSize = 12.sp,
                            color = Color(0xFF00FFC4),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Admin: ${user!!.fullName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = Color.LightGray)
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
                    Triple(0, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                    Triple(1, Icons.Filled.PostAdd, Icons.Outlined.PostAdd),
                    Triple(2, Icons.Filled.ListAlt, Icons.Outlined.ListAlt),
                    Triple(3, Icons.Filled.People, Icons.Outlined.People),
                    Triple(4, Icons.Filled.ContactPage, Icons.Outlined.ContactPage),
                    Triple(5, Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
                    Triple(6, Icons.Filled.Chat, Icons.Outlined.Chat)
                )

                tabs.forEach { (index, filled, outlined) ->
                    val active = activeTab == index
                    NavigationBarItem(
                        selected = active,
                        onClick = { viewModel.setContractorTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (active) filled else outlined,
                                contentDescription = null,
                                tint = if (active) Color(0xFF191624) else Color.White
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFD3A4FF),
                            unselectedTextColor = Color(0xFF90A4AE),
                            selectedTextColor = Color(0xFFD3A4FF)
                        ),
                        modifier = Modifier.testTag("nav_contractor_$index")
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
                0 -> ContractorDashboardTab(viewModel, onViewChatTap)
                1 -> ContractorPostJobTab(viewModel)
                2 -> ContractorMyJobsTab(viewModel)
                3 -> ContractorApplicationsTab(viewModel)
                4 -> ContractorSearchWorkersTab(viewModel, onViewChatTap)
                5 -> ContractorActiveWorkersTab(viewModel)
                6 -> ContractorChatTab(viewModel)
            }
        }
    }
}

// ==========================================
// Contractor Tab 0: Main Dashboard Analytics
// ==========================================
@Composable
fun ContractorDashboardTab(viewModel: LabNexViewModel, onViewChatTap: (Int) -> Unit) {
    val jobs by viewModel.contractorJobs.collectAsState()
    val workers by viewModel.skilledWorkers.collectAsState()

    val openCount = remember(jobs) { jobs.count { it.status == "OPEN" } }
    val progressCount = remember(jobs) { jobs.count { it.status == "IN_PROGRESS" } }
    val compCount = remember(jobs) { jobs.count { it.status == "COMPLETED" } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                "Operational Dashboard",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic Metric Grid
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("OPEN COLLABS", fontSize = 9.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                        Text(openCount.toString(), fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ACTIVE JOBS", fontSize = 9.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                        Text(progressCount.toString(), fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("COMPLETED", fontSize = 9.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                        Text(compCount.toString(), fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Text(
                "AVAILABLE TRADESMAN PROFILES",
                color = Color(0xFF90A4AE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (workers.isEmpty()) {
            item {
                Text("No certified tradesman online yet. Browse search workers tab.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            items(workers.take(3)) { worker ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { viewModel.navigateTo(Screen.WorkerProfileDetails(worker.id)) }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FFC4).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(worker.fullName.take(1), color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(worker.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${worker.yearsExperience} yrs • ${worker.skills}", color = Color(0xFF90A4AE), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("$${worker.hourlyRate.toInt()}/hr", color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// Contractor Tab 1: Post a Job
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorPostJobTab(viewModel: LabNexViewModel) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var skillsRequired by remember { mutableStateOf("") }
    var budgetMin by remember { mutableStateOf("") }
    var budgetMax by remember { mutableStateOf("") }
    var timeline by remember { mutableStateOf("") }

    var feedbackMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Commission New Collab",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (feedbackMsg.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00FFC4).copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Text(feedbackMsg, color = Color(0xFF00FFC4), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Project Title (e.g. smart beam framing)", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("job_post_title"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = skillsRequired,
            onValueChange = { skillsRequired = it },
            label = { Text("Skills Required (comma-separated: e.g. Electrical, Welding)", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("job_post_skills"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Detailed Specifications Description", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().height(110.dp).padding(bottom = 12.dp).testTag("job_post_desc"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            )
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = budgetMin,
                onValueChange = { budgetMin = it },
                label = { Text("Min Budget ($)", color = Color.White, fontSize = 11.sp) },
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("job_post_budget_min"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFC4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
            OutlinedTextField(
                value = budgetMax,
                onValueChange = { budgetMax = it },
                label = { Text("Max Budget ($)", color = Color.White, fontSize = 11.sp) },
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("job_post_budget_max"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFC4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = timeline,
            onValueChange = { timeline = it },
            label = { Text("Estimated Project Timeline (e.g. 3 Weeks)", color = Color.White) },
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).testTag("job_post_timeline"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00FFC4),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Button(
            onClick = {
                val minVal = budgetMin.toDoubleOrNull() ?: 0.0
                val maxVal = budgetMax.toDoubleOrNull() ?: 0.0
                if (title.isBlank() || desc.isBlank() || skillsRequired.isBlank() || minVal <= 0.0 || maxVal <= 0.0) {
                    feedbackMsg = "Error: Please complete all operational fields."
                    return@Button
                }
                viewModel.postNewJob(title, desc, skillsRequired, minVal, maxVal, timeline) {
                    title = ""
                    desc = ""
                    skillsRequired = ""
                    budgetMin = ""
                    budgetMax = ""
                    timeline = ""
                    feedbackMsg = "✓ Success! Project posted. Review Bids on Applications menu."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_job_post_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publish Post", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// Contractor Tab 2: My Jobs (Status Filters)
// ==========================================
@Composable
fun ContractorMyJobsTab(viewModel: LabNexViewModel) {
    val jobs by viewModel.contractorJobs.collectAsState()
    var statusSelection by remember { mutableStateOf("ALL") } // ALL, OPEN, IN_PROGRESS, COMPLETED

    val filteredList = remember(jobs, statusSelection) {
        if (statusSelection == "ALL") jobs
        else jobs.filter { it.status == statusSelection }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Posted Projects", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }

        // Horizontal Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "OPEN", "IN_PROGRESS", "COMPLETED")
            filters.forEach { filter ->
                val active = statusSelection == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (active) Color(0xFF00FFC4) else Color.White.copy(alpha = 0.05f))
                        .clickable { statusSelection = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(filter, color = if (active) Color(0xFF0F2027) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Text("No projects match the selected status filter.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredList) { job ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(job.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (job.status) {
                                                "OPEN" -> Color(0xFF00FFC4).copy(alpha = 0.15f)
                                                "IN_PROGRESS" -> Color(0xFFFFB300).copy(alpha = 0.15f)
                                                else -> Color.White.copy(alpha = 0.05f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        job.status,
                                        color = when (job.status) {
                                            "OPEN" -> Color(0xFF00FFC4)
                                            "IN_PROGRESS" -> Color(0xFFFFB300)
                                            else -> Color.LightGray
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("Timeline: ${job.timeline} • Budget: $${job.budgetMin.toInt()}-$${job.budgetMax.toInt()}", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(job.description, color = Color.LightGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Contractor Tab 3: Applications / Bid Releases
// ==========================================
@Composable
fun ContractorApplicationsTab(viewModel: LabNexViewModel) {
    val jobs by viewModel.contractorJobs.collectAsState()

    // Gather all applications for this contractor's jobs dynamically
    val bidsList = remember(jobs) { mutableStateListOf<JobApplication>() }
    val scope = rememberCoroutineScope()

    // Fetch and combine applications from and collect their flow
    LaunchedEffect(jobs) {
        bidsList.clear()
        jobs.forEach { job ->
            viewModel.repository.getApplicationsForJob(job.id).collect { apps ->
                val filtered = apps.filter { it.status == "PENDING" }
                // Remove existing to replace/add
                bidsList.removeAll { b -> b.jobId == job.id }
                bidsList.addAll(filtered)
            }
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
                "Received Bid Proposals",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Review custom quotes placed by workers. Accept offers to mark projects in progress and kick off direct chats immediately.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (bidsList.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No pending bid proposals received yet.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(bidsList) { bid ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(bid.jobTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Bid Proposal from ${bid.workerName}", color = Color(0xFF90A4AE), fontSize = 12.sp)
                            }

                            // Overlap Match % Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00FFC4).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("${bid.matchPercentage}% Skills match", color = Color(0xFF00FFC4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bid details
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Worker rate: $${bid.workerRate.toInt()}/hr", color = Color.LightGray, fontSize = 12.sp)
                            Text("PROPOSAL COUNTER: $${bid.bidAmount.toInt()}", color = Color(0xFF00FFC4), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Cover message:\n\"${bid.coverLetter}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB0BEC5),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.rejectApplication(bid.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Ignore", color = Color.LightGray, fontSize = 12.sp)
                            }

                            // Accepting offer hire action triggers chat release
                            Button(
                                onClick = { viewModel.acceptApplication(bid.id) {} },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.2f).height(38.dp)
                            ) {
                                Text("Accept & Hire", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Contractor Tab 4: Search Workers List
// ==========================================
@Composable
fun ContractorSearchWorkersTab(viewModel: LabNexViewModel, onViewChatTap: (Int) -> Unit) {
    val searchVal by viewModel.workerSearchQuery.collectAsState()
    val filterSkill by viewModel.workerSkillFilter.collectAsState()

    val workers by viewModel.skilledWorkers.collectAsState()

    val filteredWorkers = remember(workers, searchVal, filterSkill) {
        workers.filter {
            (searchVal.isBlank() || it.fullName.contains(searchVal, true) || it.skills.contains(searchVal, true)) &&
            (filterSkill == "All" || it.skills.contains(filterSkill, true))
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
                "Search Skilled Workers",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = searchVal,
                onValueChange = { viewModel.workerSearchQuery.value = it },
                placeholder = { Text("Search by name, welding, wiring, drywall...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00FFC4)) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("contractor_search_workers"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFC4),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true
            )

            // Trades Filter Row
            val trades = listOf("All", "Carpentry", "Electrical", "Welding", "Plumbing", "Masonry")
            LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                items(trades) { item ->
                    val active = filterSkill == item
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) Color(0xFF00FFC4) else Color.White.copy(alpha = 0.05f))
                            .clickable { viewModel.workerSkillFilter.value = item }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(item, color = if (active) Color(0xFF0F2027) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (filteredWorkers.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("No skilled worker profiles match your filters currently.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredWorkers) { worker ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { viewModel.navigateTo(Screen.WorkerProfileDetails(worker.id)) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FFC4).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(worker.fullName.take(1), color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(worker.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${String.format("%.1f", worker.rating)} (${worker.reviewsCount} reviews)",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text("$${worker.hourlyRate.toInt()}/hr", color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(worker.portfolioDesc, color = Color.LightGray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Skills: ${worker.skills}", color = Color(0xFF90A4AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(Screen.WorkerProfileDetails(worker.id)) },
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("View Profile", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onViewChatTap(worker.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                                modifier = Modifier.weight(1.2f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Discuss Job", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Contractor Tab 5: Active Workers (Hours Appr)
// ==========================================
@Composable
fun ContractorActiveWorkersTab(viewModel: LabNexViewModel) {
    val jobs by viewModel.contractorJobs.collectAsState()
    val scope = rememberCoroutineScope()

    val activeJobs = remember(jobs) { jobs.filter { it.status == "IN_PROGRESS" } }

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
                "Confirm registered working hours, audit milestone updates, or authorize digital payments triggers.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (activeJobs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active job collaborations current running.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(activeJobs) { job ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(job.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("In Progress", color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("LOGGED PROGRESS CODE", fontSize = 9.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                                Text("${job.completedHours} hrs logged", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text("APPROVED MAXIMUM", fontSize = 9.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                                Text("$${job.budgetMax.toInt()}", color = Color(0xFF00FFC4), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Direct Chat discussed
                            Button(
                                onClick = { viewModel.navigateTo(Screen.ChatRoom(job.workerId)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Message Hired", color = Color.White, fontSize = 11.sp)
                            }

                            // Completion Release
                            Button(
                                onClick = { viewModel.navigateTo(Screen.Transactions(job.id)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                                modifier = Modifier.weight(1.4f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Complete & Pay", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Contractor Tab 6: Messages / Chat logs
// ==========================================
@Composable
fun ContractorChatTab(viewModel: LabNexViewModel) {
    val myMessages by viewModel.myMessages.collectAsState()
    val rawUser by viewModel.currentUser.collectAsState()

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
                    map[msg.receiverId] = "Worker"
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
                "Inbox & Agreements",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Direct safe-coordination logs regarding active jobs details, schedules, and custom bid rates.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        if (conversationPartners.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No messaging threads found.", color = Color.Gray, fontSize = 13.sp)
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
                            Text("Click to continue discussing work details", color = Color(0xFF90A4AE), fontSize = 11.sp)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00FFC4))
                    }
                }
            }
        }
    }
}
