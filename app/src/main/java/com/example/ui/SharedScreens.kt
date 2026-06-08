package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch

// ==========================================
// 1. CHAT ROOM (Real-time Simulation)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: LabNexViewModel,
    otherUserId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val messages by viewModel.activeChatMessages.collectAsState()

    var otherUserName by remember { mutableStateOf("User Chat") }

    // Fetch details of other User
    LaunchedEffect(otherUserId) {
        viewModel.loadChatMessages(otherUserId)
        val other = viewModel.repository.getUserById(otherUserId)
        if (other != null) {
            otherUserName = other.fullName
        }
    }

    var textInput by remember { mutableStateOf("") }
    var selectedSimAttachment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(otherUserName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Online • Direct Secure Line", color = Color(0xFF00FFC4), fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Go back to main
                        if (user?.role == "WORKER") {
                            viewModel.navigateTo(Screen.WorkerMain)
                        } else {
                            viewModel.navigateTo(Screen.ContractorMain)
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Initiating secure voice call...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = "Call", tint = Color(0xFFD499FF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF191624))
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // If there's an attachment staged, show a thumbnail of it above the keyboard input box
                if (selectedSimAttachment.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2F35))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF00FFC4))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(selectedSimAttachment, color = Color.White, fontSize = 12.sp)
                            }
                            IconButton(onClick = { selectedSimAttachment = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Keyboard input field
                Surface(
                    color = Color(0xFF1E2F35),
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Simulated attachment menu icon button triggers staging
                        IconButton(onClick = {
                            val options = listOf("construction_blueprint_framing.dwg", "weld_quality_approval.jpg", "on-site_dimensions.pdf")
                            selectedSimAttachment = options.random()
                            Toast.makeText(context, "Attachment staged!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Attach", tint = Color(0xFF00FFC4))
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Write safe offer agreements...", color = Color.Gray, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field"),
                            textStyle = TextStyle(color = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFC4),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank() || selectedSimAttachment.isNotEmpty()) {
                                    viewModel.sendChatMessage(otherUserId, 0, textInput, selectedSimAttachment)
                                    textInput = ""
                                    selectedSimAttachment = ""
                                }
                            },
                            modifier = Modifier.testTag("chat_send_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF00FFC4))
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0F2027),
        modifier = modifier
    ) { paddingValues ->
        // Chat History List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            reverseLayout = false
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All bids agreement negotiation are locked & encrypted.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    val isMyMsg = msg.senderId == user?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = if (isMyMsg) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMyMsg) Color(0xFF00FFC4).copy(alpha = 0.15f) else Color(0xFF1E2F35)
                            ),
                            border = BorderStroke(1.dp, if (isMyMsg) Color(0xFF00FFC4).copy(alpha = 0.4f) else Color.Transparent),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMyMsg) 16.dp else 0.dp,
                                bottomEnd = if (isMyMsg) 0.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMyMsg) {
                                    Text(msg.senderName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FFC4), modifier = Modifier.padding(bottom = 2.dp))
                                }

                                if (msg.content.isNotEmpty()) {
                                    Text(msg.content, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                                }

                                if (msg.attachmentUri.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.2f))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(msg.attachmentUri, color = Color.White, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. JOB DETAILS DEEP VIEW / APPLY FORM & PROJECT DASHBOARD
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    viewModel: LabNexViewModel,
    jobId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val jobFlow = remember(jobId) { viewModel.repository.getJobByIdFlow(jobId) }
    val job by jobFlow.collectAsState(initial = null)

    var showApplyDialog by remember { mutableStateOf(false) }
    var bidValueInput by remember { mutableStateOf("") }
    var bidCoverLetter by remember { mutableStateOf("") }

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    val isProjectActive = job!!.status == "IN_PROGRESS" || job!!.status == "COMPLETED"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isProjectActive) "Project Management Hub" else "Collab Scope Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (user?.role == "CONTRACTOR") {
                            viewModel.navigateTo(Screen.ContractorMain)
                        } else {
                            viewModel.navigateTo(Screen.WorkerMain)
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F2027))
            )
        },
        containerColor = Color(0xFF0F2027),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isProjectActive) {
                // Render Project Dashboard Workspace directly
                Text(
                    text = job!!.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Project active with: ${if (user?.role == "WORKER") "Contractor: ${job!!.contractorName}" else "Worker: ${job!!.workerId}"}",
                    fontSize = 12.sp,
                    color = Color(0xFF90A4AE),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProjectDashboardSection(
                    job = job!!,
                    currentUser = user,
                    viewModel = viewModel
                )
            } else {
                // Render Standard Job Description with Proposal Place Bidding CTA
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "CONTRACT WORK ORDER", fontSize = 10.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = job!!.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        
                        // Clickable Contractor name to display Contractor detailed profile
                        Text(
                            text = "Posted by ${job!!.contractorName} • 🏢 View Profile",
                            fontSize = 12.sp,
                            color = Color(0xFF00FFC4),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.navigateTo(Screen.ContractorProfileDetails(job!!.contractorId)) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Detailed specifications:", fontSize = 11.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        Text(
                            text = job!!.description,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFFEEF2F6)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("BUDGET PARAMS", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                                Text("$${job!!.budgetMin.toInt()} - $${job!!.budgetMax.toInt()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text("EXPECTED TIMELINE", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                                Text(job!!.timeline, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SKILLS TAXONOMY PREFERRED", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                        Text(job!!.skillsRequired, color = Color(0xFF00FFC4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (user?.role == "WORKER") {
                    Button(
                        onClick = {
                            bidValueInput = job!!.budgetMin.toInt().toString()
                            bidCoverLetter = ""
                            showApplyDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("trigger_apply_bid_button")
                    ) {
                        Icon(Icons.Default.SendTimeExtension, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Place Custom Bidding Proposal", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Browsing position details as Contractor.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Submit Proposal Quote", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Place your custom bid amount based on the contractor's specs.", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = bidValueInput,
                        onValueChange = { bidValueInput = it },
                        label = { Text("Your Bid Amount ($)", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FFC4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("dialog_bid_amount")
                    )

                    OutlinedTextField(
                        value = bidCoverLetter,
                        onValueChange = { bidCoverLetter = it },
                        label = { Text("Cover letter to Contractor", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FFC4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("dialog_bid_letter")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = bidValueInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0 && bidCoverLetter.isNotBlank()) {
                            viewModel.submitJobBid(job!!.id, job!!.title, amount, bidCoverLetter) {
                                showApplyDialog = false
                                Toast.makeText(context, "Bid Proposal Submitted Successfully!", Toast.LENGTH_LONG).show()
                                viewModel.navigateTo(Screen.WorkerMain)
                            }
                        } else {
                            Toast.makeText(context, "Please enter valid proposal details.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                    modifier = Modifier.testTag("dialog_confirm_bid_button")
                ) {
                    Text("Submit Quote", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E2F35)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDashboardSection(
    job: Job,
    currentUser: User?,
    viewModel: LabNexViewModel
) {
    val context = LocalContext.current
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var milestoneName by remember { mutableStateOf("") }
    var milestoneDeadline by remember { mutableStateOf("") }
    
    var showShareFileDialog by remember { mutableStateOf(false) }
    var inProjectMessageText by remember { mutableStateOf("") }
    
    // Parse Milestones
    val milestones = remember(job.milestonesJoined) {
        if (job.milestonesJoined.isBlank()) emptyList()
        else job.milestonesJoined.split(";").filter { it.isNotBlank() }.mapIndexed { index, m ->
            val parts = m.split("|")
            val name = parts.getOrNull(0) ?: "Milestone $index"
            val deadline = parts.getOrNull(1) ?: "No deadline"
            val isCompleted = parts.getOrNull(2) == "1"
            Triple(index, name, Triple(deadline, isCompleted, m))
        }
    }
    
    // Parse Files
    val sharedFiles = remember(job.filesJoined) {
        if (job.filesJoined.isBlank()) emptyList()
        else job.filesJoined.split(";").filter { it.isNotBlank() }.map { f ->
            val parts = f.split("|")
            val name = parts.getOrNull(0) ?: "File"
            val sender = parts.getOrNull(1) ?: "Contractor"
            val dateStr = parts.getOrNull(2) ?: "Jun 08"
            Triple(name, sender, dateStr)
        }
    }
    
    // Observe messages filtered by jobId for live context-logging
    val myMessages by viewModel.myMessages.collectAsState()
    val projectMessages = remember(myMessages, job.id) {
        myMessages.filter { it.jobId == job.id }
    }

    val progressPercent = remember(milestones) {
        if (milestones.isEmpty()) 0f
        else {
            val completed = milestones.count { it.third.second }
            completed.toFloat() / milestones.size.toFloat()
        }
    }

    Column {
        // Performance Progress Indicators
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "PROJECT PERFORMANCE & METRICS", fontSize = 10.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Development Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${(progressPercent * 100).toInt()}% Done", color = Color(0xFF00FFC4), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressPercent },
                    color = Color(0xFF00FFC4),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("TOTAL REGISTERED TIME", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                        Text("${job.completedHours} hrs logged", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text("PROJECT BUDGET VALUE", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
                        Text("$${job.budgetMin.toInt()}", color = Color(0xFF00FFC4), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        
        // Checklist milestones
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "COLLABORATION MILESTONES", fontSize = 12.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showAddMilestoneDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add milestone", tint = Color(0xFF00FFC4))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (milestones.isEmpty()) {
                    Text("No milestones set yet. Click '+' to add one.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    milestones.forEach { (index, name, meta) ->
                        val (deadline, isCompleted, _) = meta
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = { viewModel.toggleMilestoneStatus(job.id, index) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00FFC4), uncheckedColor = Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = name,
                                        color = if (isCompleted) Color(0xFF90A4AE) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text("Due: $deadline", color = Color(0xFF90A4AE), fontSize = 11.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCompleted) Color(0xFF00FFC4).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (isCompleted) "Completed" else "Pending",
                                    color = if (isCompleted) Color(0xFF00FFC4) else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Shared files tracker
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "SHARED PROJECT FILE EXCHANGE", fontSize = 12.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showShareFileDialog = true }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Upload file", tint = Color(0xFF00FFC4))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (sharedFiles.isEmpty()) {
                    Text("No files shared yet on this workspace.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    sharedFiles.forEach { (name, sender, date) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Shared by $sender • $date", color = Color(0xFF90A4AE), fontSize = 11.sp)
                                }
                            }
                            IconButton(onClick = {
                                Toast.makeText(context, "✓ Downloading $name from secure workspace cloud...", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Escrow funds / Settlement approval state
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "SECURE WORKSPACE PAYOUT", fontSize = 12.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (job.isPaid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("PAYMENT RECEIVED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("All escrow balance has been released to worker Wallet.", color = Color(0xFF90A4AE), fontSize = 11.sp)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ESCROW BALANCE LOCKED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Payout value of $${job.budgetMin.toInt()} will release upon contractor finalization.", color = Color(0xFF90A4AE), fontSize = 11.sp)
                        }
                    }
                    if (currentUser?.role == "CONTRACTOR") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.approveAndPayJob(job.id, job.budgetMin) {
                                    Toast.makeText(context, "✓ Payout approved! Released $${job.budgetMin.toInt()} from lock escrow.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Complete & Release Escrow", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub-chat module context messages
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "PROJECT WORKSPACE CONVERSATIONS", fontSize = 12.sp, color = Color(0xFF00FFC4), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    if (projectMessages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No communication logged for this project yet.", color = Color.Gray, fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(projectMessages) { msg ->
                                val isMe = msg.senderId == currentUser?.id
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isMe) Color(0xFF00FFC4).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isMe) "You" else msg.senderName,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00FFC4),
                                                fontSize = 10.sp
                                            )
                                            Text(msg.content, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inProjectMessageText,
                        onValueChange = { inProjectMessageText = it },
                        placeholder = { Text("Post communication update...", color = Color.Gray, fontSize = 12.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FFC4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (inProjectMessageText.isNotBlank()) {
                                val otherUserId = if (currentUser?.role == "WORKER") job.contractorId else job.workerId
                                viewModel.sendChatMessage(otherUserId, job.id, inProjectMessageText)
                                inProjectMessageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF00FFC4), shape = RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF0F2027), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    if (showAddMilestoneDialog) {
        AlertDialog(
            onDismissRequest = { showAddMilestoneDialog = false },
            title = { Text("Add Project Milestone", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = milestoneName,
                        onValueChange = { milestoneName = it },
                        label = { Text("Milestone Goal Description", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = milestoneDeadline,
                        onValueChange = { milestoneDeadline = it },
                        label = { Text("Expected Completion (e.g., Jun 21)", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (milestoneName.isNotBlank() && milestoneDeadline.isNotBlank()) {
                            viewModel.addProjectMilestone(job.id, milestoneName, milestoneDeadline)
                            milestoneName = ""
                            milestoneDeadline = ""
                            showAddMilestoneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027))
                ) {
                    Text("Add Milestone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMilestoneDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E2F35)
        )
    }

    if (showShareFileDialog) {
        val simFiles = listOf("elevations_framed_site.png", "material_delivery_slip.xlsx", "revised_layouts_v2.dwg", "signed_inspection_report.pdf")
        AlertDialog(
            onDismissRequest = { showShareFileDialog = false },
            title = { Text("Upload File to Project", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a simulated asset to upload to this project channel secure escrow feed:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    simFiles.forEach { file ->
                        Card(
                            onClick = {
                                viewModel.shareProjectFile(job.id, file, currentUser?.fullName ?: "Member")
                                showShareFileDialog = false
                                Toast.makeText(context, "$file shared successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Color(0xFF00FFC4))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(file, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showShareFileDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E2F35)
        )
    }
}

// ==========================================
// 3. SKILLED WORKER PROFILE DETAILS
// ==========================================

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileDetailsScreen(
    viewModel: LabNexViewModel,
    workerId: Int,
    modifier: Modifier = Modifier
) {
    val rawWorkerFlow = remember(workerId) { viewModel.repository.getUserByIdFlow(workerId) }
    val worker by rawWorkerFlow.collectAsState(initial = null)
    val reviews by viewModel.repository.getReviewsForUser(workerId).collectAsState(emptyList())

    if (worker == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Skilled Worker Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.ContractorMain) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F2027))
            )
        },
        containerColor = Color(0xFF0F2027),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // General Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FFC4).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(worker!!.fullName.take(1), color = Color(0xFF00FFC4), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(worker!!.fullName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
                    Text("Trade: ${worker!!.skills}", color = Color(0xFF00FFC4), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Base rate: $${worker!!.hourlyRate.toInt()}/hr • ${worker!!.city}", color = Color(0xFF90A4AE), fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", worker!!.rating)} (${worker!!.reviewsCount} completed jobs reviews)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text("BIOGRAPHICAL PORTFOLIO SUMMARY", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Text(
                worker!!.portfolioDesc,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Certified Tick list
            Text("VERIFIED CREDENTIALS BADGES", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            if (worker!!.certifications.isBlank()) {
                Text("No verified certifications listed.", color = Color.Gray, fontSize = 13.sp)
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                    worker!!.certifications.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { cert ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cert, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Direct chat shortcut
            Button(
                onClick = { viewModel.navigateTo(Screen.ChatRoom(worker!!.id)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Chat & Discuss Work", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reviews List
            Text("WHAT PREVIOUS CONTRACTORS SAYS", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
            if (reviews.isEmpty()) {
                Text("No feedback recorded yet on this profile.", color = Color.Gray, fontSize = 13.sp)
            } else {
                reviews.forEach { r ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
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
                            Text("Job: ${r.jobTitle}", color = Color(0xFF90A4AE), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(r.reviewText, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. TRANSACTION BILL COMPLETION & RATING DIALOG
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: LabNexViewModel,
    jobId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val jobFlow = remember(jobId) { viewModel.repository.getJobByIdFlow(jobId) }
    val job by jobFlow.collectAsState(initial = null)

    var showRatingPanel by remember { mutableStateOf(false) }
    var ratingStarsSelected by remember { mutableStateOf(5) }
    var reviewTextInput by remember { mutableStateOf("") }
    var isSubmittedReview by remember { mutableStateOf(false) }

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    // Calculated billing values
    val totalRateDue = remember(job) {
        val total = job!!.completedHours * 50.0 // Custom mock average rate
        if (total <= 0) job!!.budgetMin else total
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Invoice Billing Scope", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (user?.role == "WORKER") {
                            viewModel.navigateTo(Screen.WorkerMain)
                        } else {
                            viewModel.navigateTo(Screen.ContractorMain)
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F2027))
            )
        },
        containerColor = Color(0xFF0F2027),
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
            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF00FFC4), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Digital Project Settled", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp)
            Text("All requirements completed & locked on blockchain ledger.", color = Color(0xFF90A4AE), fontSize = 12.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            // Invoice Summary card layout
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("OFFICIAL INVOICE SUMMARY", color = Color(0xFF00FFC4), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Project Name:", color = Color.LightGray, fontSize = 13.sp)
                        Text(job!!.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.widthIn(max = 160.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Allocated Worker:", color = Color.LightGray, fontSize = 13.sp)
                        Text(job!!.contractorName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Registered Hours:", color = Color.LightGray, fontSize = 13.sp)
                        Text("${job!!.completedHours} hrs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL VALUE RELEASED:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("$${totalRateDue.toInt()}.00", color = Color(0xFF00FFC4), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }

            // CTAs
            // Contractor can pay & close, or both can rate & review!
            if (user?.role == "CONTRACTOR" && job!!.status == "IN_PROGRESS") {
                Button(
                    onClick = {
                        viewModel.approveAndPayJob(job!!.id, totalRateDue) {
                            showRatingPanel = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("approve_pay_button")
                ) {
                    Icon(Icons.Default.CreditScore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Approve Work & Transfer $${totalRateDue.toInt()}", fontWeight = FontWeight.Bold)
                }
            } else {
                // If already completed or paid, show Download Invoice and Rate buttons
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Downloading digital INV-2026-X${job!!.id}.pdf...", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Invoice PDF", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showRatingPanel = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.RateReview, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSubmittedReview) "Feedback Stored!" else "Rate & Review Trade Partner", color = Color.White)
                }
            }
        }
    }

    if (showRatingPanel) {
        val targetRevieweeId = if (user?.role == "WORKER") job!!.contractorId else job!!.workerId

        AlertDialog(
            onDismissRequest = { showRatingPanel = false },
            title = { Text("Rate & Review Collaborator", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Submit stars feedback rating based on trade safety, speed compliance, and quality alignment.", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 16.dp))

                    Row {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= ratingStarsSelected) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$i Stars",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { ratingStarsSelected = i }
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = reviewTextInput,
                        onValueChange = { reviewTextInput = it },
                        label = { Text("What went well or need audit?", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FFC4),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewTextInput.isNotBlank()) {
                            viewModel.submitReviewRating(job!!.id, job!!.title, targetRevieweeId, ratingStarsSelected.toFloat(), reviewTextInput) {
                                isSubmittedReview = true
                                showRatingPanel = false
                                Toast.makeText(context, "Thank you for the review feedback!", Toast.LENGTH_SHORT).show()
                                if (user?.role == "WORKER") {
                                    viewModel.navigateTo(Screen.WorkerMain)
                                } else {
                                    viewModel.navigateTo(Screen.ContractorMain)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFC4), contentColor = Color(0xFF0F2027))
                ) {
                    Text("Publish Review", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingPanel = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E2F35)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractorProfileDetailsScreen(
    viewModel: LabNexViewModel,
    contractorId: Int,
    modifier: Modifier = Modifier
) {
    val contractorFlow = remember(contractorId) { viewModel.repository.getUserByIdFlow(contractorId) }
    val contractor by contractorFlow.collectAsState(initial = null)
    
    val jobsFlow = remember(contractorId) { viewModel.repository.getJobsByContractor(contractorId) }
    val jobs by jobsFlow.collectAsState(initial = emptyList())

    if (contractor == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00FFC4))
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contractor Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.WorkerMain) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0F2027))
            )
        },
        containerColor = Color(0xFF0F2027),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Company Card Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FFC4).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contractor!!.fullName.take(1),
                            color = Color(0xFF00FFC4),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(contractor!!.fullName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("Industry Sector: ${contractor!!.skills.ifBlank { "General Construction & Trade Logistics" }}", color = Color(0xFF00FFC4), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("🏢 Corp size: 10-50 Trade experts • ${contractor!!.city}", color = Color(0xFF90A4AE), fontSize = 12.sp)
                }
            }

            // Company Bio details
            Text("CORPORATE OVERVIEW", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = contractor!!.portfolioDesc.ifBlank { "Licensed commercial contractor supervising active logistics, carpentry assemblies, smart electrical fittings, and HVAC trades with blockchain integrated accounting." },
                        color = Color(0xFFEEF2F6),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Project Listing history
            Text("PROJECT GENERAL HISTORY (${jobs.size} Posted)", color = Color(0xFF90A4AE), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            
            if (jobs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No projects submitted by this contractor yet.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                jobs.forEach { job ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2F35)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { viewModel.navigateTo(Screen.JobDetails(job.id)) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(job.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Budget: $${job.budgetMin.toInt()} - $${job.budgetMax.toInt()}", color = Color(0xFF90A4AE), fontSize = 12.sp)
                                Text("Preferred skills: ${job.skillsRequired}", color = Color(0xFF00FFC4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Project status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (job.status) {
                                            "COMPLETED" -> Color(0xFF00FFC4).copy(alpha = 0.15f)
                                            "IN_PROGRESS" -> Color(0xFFFFB300).copy(alpha = 0.15f)
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = job.status,
                                    color = when (job.status) {
                                        "COMPLETED" -> Color(0xFF00FFC4)
                                        "IN_PROGRESS" -> Color(0xFFFFB300)
                                        else -> Color.LightGray
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
