package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object SignUp : Screen()
    object WorkerOnboarding : Screen()
    object ContractorOnboarding : Screen()
    object WorkerMain : Screen()
    object ContractorMain : Screen()
    data class JobDetails(val jobId: Int) : Screen()
    data class WorkerProfileDetails(val workerId: Int) : Screen()
    data class ChatRoom(val otherUserId: Int) : Screen()
    data class Transactions(val jobId: Int) : Screen()
    data class ContractorProfileDetails(val contractorId: Int) : Screen()
}

class LabNexViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = LabNexRepository(database)

    // Current app state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Screen navigation helpers
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Active Tab in Dashboards
    private val _workerTab = MutableStateFlow(0)
    val workerTab: StateFlow<Int> = _workerTab.asStateFlow()

    private val _contractorTab = MutableStateFlow(0)
    val contractorTab: StateFlow<Int> = _contractorTab.asStateFlow()

    fun setWorkerTab(index: Int) {
        _workerTab.value = index
    }

    fun setContractorTab(index: Int) {
        _contractorTab.value = index
    }

    // Onboarding values
    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    // Temporary user fields during onboarding modifications
    private val _tempOnboardingUser = MutableStateFlow<User?>(null)
    val tempOnboardingUser: StateFlow<User?> = _tempOnboardingUser.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedMockData()
        }
    }

    // Worker filter state
    val searchFilterQuery = MutableStateFlow("")
    val minBudgetFilter = MutableStateFlow(0f)
    val maxBudgetFilter = MutableStateFlow(10000f)
    val selectedSkillFilter = MutableStateFlow("All")

    // Skilled Workers Filter state (Contractor View)
    val workerSearchQuery = MutableStateFlow("")
    val workerSkillFilter = MutableStateFlow("All")

    // Data streams
    val allJobs: StateFlow<List<Job>> = repository.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skilledWorkers: StateFlow<List<User>> = repository.getSkilledWorkersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatMessages = MutableStateFlow<List<Message>>(emptyList())
    val activeChatMessages: StateFlow<List<Message>> = _activeChatMessages.asStateFlow()

    // Dynamic Streams based on logged-in user
    val mySubmittedApplications: StateFlow<List<JobApplication>> = _currentUser.flatMapLatest { user ->
        user?.let { repository.getApplicationsByWorker(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contractorJobs: StateFlow<List<Job>> = _currentUser.flatMapLatest { user ->
        user?.let { repository.getJobsByContractor(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workerActiveJobs: StateFlow<List<Job>> = _currentUser.flatMapLatest { user ->
        user?.let { repository.getJobsByWorker(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myMessages: StateFlow<List<Message>> = _currentUser.flatMapLatest { user ->
        user?.let { repository.getMyMessages(it.id) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication ---
    fun login(email: String, pword: String, onCompleted: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.password == pword) {
                _currentUser.value = user
                _tempOnboardingUser.value = user
                if (!user.isProfileComplete) {
                    _onboardingStep.value = 1
                    if (user.role == "WORKER") {
                        _currentScreen.value = Screen.WorkerOnboarding
                    } else {
                        _currentScreen.value = Screen.ContractorOnboarding
                    }
                } else {
                    if (user.role == "WORKER") {
                        _currentScreen.value = Screen.WorkerMain
                    } else {
                        _currentScreen.value = Screen.ContractorMain
                    }
                }
                onCompleted(true, "LoggedIn Successfully")
            } else {
                onCompleted(false, "Invalid credentials")
            }
        }
    }

    fun signUp(email: String, pword: String, name: String, phoneStr: String, roleVal: String, onCompleted: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onCompleted(false, "Account with this email already exists")
                return@launch
            }
            val newUser = User(
                email = email,
                password = pword,
                fullName = name,
                phone = phoneStr,
                role = roleVal,
                isProfileComplete = false
            )
            val newId = repository.registerUser(newUser)
            val createdUser = newUser.copy(id = newId)
            _currentUser.value = createdUser
            _tempOnboardingUser.value = createdUser
            _onboardingStep.value = 1
            if (roleVal == "WORKER") {
                _currentScreen.value = Screen.WorkerOnboarding
            } else {
                _currentScreen.value = Screen.ContractorOnboarding
            }
            onCompleted(true, "Signed up successfully")
        }
    }

    fun logout() {
        _currentUser.value = null
        _tempOnboardingUser.value = null
        _currentScreen.value = Screen.Welcome
    }

    // --- Onboarding Operations ---
    fun updateTempUser(update: (User) -> User) {
        _tempOnboardingUser.value?.let {
            _tempOnboardingUser.value = update(it)
        }
    }

    fun nextOnboardingStep(maxSteps: Int, onFinish: () -> Unit) {
        val current = _onboardingStep.value
        if (current >= maxSteps) {
            completeOnboarding()
            onFinish()
        } else {
            _onboardingStep.value = current + 1
        }
    }

    fun prevOnboardingStep() {
        val current = _onboardingStep.value
        if (current > 1) {
            _onboardingStep.value = current - 1
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _tempOnboardingUser.value?.let { user ->
                val completed = user.copy(isProfileComplete = true)
                repository.updateUser(completed)
                _currentUser.value = completed
                _tempOnboardingUser.value = completed
                if (completed.role == "WORKER") {
                    _currentScreen.value = Screen.WorkerMain
                } else {
                    _currentScreen.value = Screen.ContractorMain
                }
            }
        }
    }

    // --- Apply To Jobs (Bidding) ---
    fun submitJobBid(jobId: Int, jobTitle: String, bidAmount: Double, cover: String, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            // Standard calculate skills overlap match percentage
            val job = repository.getJobById(jobId)
            var matchPercentage = 80 // Default high quality general match
            if (job != null) {
                val workerSkills = user.skills.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                val requiredSkills = job.skillsRequired.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                if (requiredSkills.isNotEmpty()) {
                    val overlap = workerSkills.intersect(requiredSkills.toSet()).size
                    matchPercentage = ((overlap.toFloat() / requiredSkills.size.toFloat()) * 100f).toInt().coerceAtLeast(60).coerceAtMost(100)
                }
            }

            val app = JobApplication(
                jobId = jobId,
                jobTitle = jobTitle,
                workerId = user.id,
                workerName = user.fullName,
                workerSkills = user.skills,
                workerExperience = user.yearsExperience,
                workerRate = user.hourlyRate,
                bidAmount = bidAmount,
                coverLetter = cover,
                matchPercentage = matchPercentage,
                status = "PENDING"
            )
            repository.submitApplication(app)
            onCompleted()
        }
    }

    fun withdrawBid(bidId: Int, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val bid = repository.getApplicationById(bidId)
            if (bid != null) {
                repository.updateApplication(bid.copy(status = "WITHDRAWN"))
                onCompleted()
            }
        }
    }

    // --- Contractor Job Bidding acceptance (Decision hiring) ---
    fun submitCounterOffer(bidId: Int, amount: Double, note: String, senderRole: String, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val bid = repository.getApplicationById(bidId)
            if (bid != null) {
                repository.updateApplication(
                    bid.copy(
                        counterAmount = amount,
                        counterNote = note,
                        lastCounterBy = senderRole,
                        timestamp = System.currentTimeMillis()
                    )
                )
                onCompleted()
            }
        }
    }

    fun acceptApplication(bidId: Int, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val bid = repository.getApplicationById(bidId)
            if (bid != null) {
                val finalPrice = if (bid.counterAmount > 0.0) bid.counterAmount else bid.bidAmount
                // Update Bid
                repository.updateApplication(bid.copy(status = "ACCEPTED", bidAmount = finalPrice))
                // Update Job status to IN_PROGRESS
                val job = repository.getJobById(bid.jobId)
                if (job != null) {
                    val initialMilestones = "Preparation & Setup|1 Week|0;Core Tasks Accomplishment|2 Weeks|0;Final Audit & Payment|3 Weeks|0"
                    val initialFiles = "structural_blueprints.pdf|Contractor|Jun 08;contract_guidelines.docx|Contractor|Jun 08"
                    repository.updateJob(
                        job.copy(
                            status = "IN_PROGRESS",
                            workerId = bid.workerId,
                            budgetMin = finalPrice,
                            budgetMax = finalPrice,
                            milestonesJoined = initialMilestones,
                            filesJoined = initialFiles
                        )
                    )
                }
                
                // Add automated contract message inside Chat
                val contractor = _currentUser.value
                val senderId = contractor?.id ?: bid.workerId
                val senderName = contractor?.fullName ?: "Contractor"
                repository.sendMessage(
                    Message(
                        senderId = senderId,
                        receiverId = bid.workerId,
                        senderName = senderName,
                        content = "Congratulations! Custom offer of $${finalPrice} accepted for '${bid.jobTitle}'. You are hired. We can kick off work details here!",
                        jobId = bid.jobId
                    )
                )
                onCompleted()
            }
        }
    }

    fun addProjectMilestone(jobId: Int, name: String, deadline: String) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                val updatedMilestones = if (job.milestonesJoined.isBlank()) {
                    "$name|$deadline|0"
                } else {
                    "${job.milestonesJoined};$name|$deadline|0"
                }
                repository.updateJob(job.copy(milestonesJoined = updatedMilestones))
            }
        }
    }

    fun toggleMilestoneStatus(jobId: Int, milestoneIndex: Int) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                val milestones = job.milestonesJoined.split(";").toMutableList()
                if (milestoneIndex >= 0 && milestoneIndex < milestones.size) {
                    val m = milestones[milestoneIndex]
                    val parts = m.split("|")
                    if (parts.size >= 3) {
                        val isComplete = parts[2] == "1"
                        val toggled = if (isComplete) "0" else "1"
                        milestones[milestoneIndex] = "${parts[0]}|${parts[1]}|$toggled"
                        val updated = milestones.joinToString(";")
                        repository.updateJob(job.copy(milestonesJoined = updated))
                    }
                }
            }
        }
    }

    fun shareProjectFile(jobId: Int, fileName: String, senderName: String) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                val dateStr = "Jun 08"
                val fileRecord = "$fileName|$senderName|$dateStr"
                val updatedFiles = if (job.filesJoined.isBlank()) {
                    fileRecord
                } else {
                    "${job.filesJoined};$fileRecord"
                }
                repository.updateJob(job.copy(filesJoined = updatedFiles))
            }
        }
    }

    fun rejectApplication(bidId: Int) {
        viewModelScope.launch {
            val bid = repository.getApplicationById(bidId)
            if (bid != null) {
                repository.updateApplication(bid.copy(status = "REJECTED"))
            }
        }
    }

    // --- Job Post ---
    fun postNewJob(title: String, desc: String, skillsReq: String, bMin: Double, bMax: Double, timelineStr: String, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val job = Job(
                contractorId = user.id,
                contractorName = user.fullName,
                title = title,
                description = desc,
                skillsRequired = skillsReq,
                budgetMin = bMin,
                budgetMax = bMax,
                timeline = timelineStr,
                status = "OPEN"
            )
            repository.createJob(job)
            onCompleted()
        }
    }

    // --- Chat Room loading & sending ---
    fun loadChatMessages(otherUserId: Int) {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            repository.getChatMessages(current.id, otherUserId).collect {
                _activeChatMessages.value = it
            }
        }
    }

    fun sendChatMessage(recipientId: Int, contextJobId: Int, txtContact: String, optPhotoUri: String = "") {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val msg = Message(
                senderId = current.id,
                receiverId = recipientId,
                senderName = current.fullName,
                content = txtContact,
                jobId = contextJobId,
                attachmentUri = optPhotoUri
            )
            repository.sendMessage(msg)
            // Trigger quick load trigger
            loadChatMessages(recipientId)
        }
    }

    // --- Hours and payments tracking --
    fun submitHoursLogged(jobId: Int, hrs: Double, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                val newHours = job.completedHours + hrs
                repository.updateJob(job.copy(completedHours = newHours))
                onCompleted()
            }
        }
    }

    fun approveAndPayJob(jobId: Int, totalPayment: Double, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                repository.updateJob(job.copy(status = "COMPLETED", isPaid = true, invoiceDownloadPath = "INV-2026-X${jobId}.pdf"))
                
                // Add balance to worker's account
                val worker = repository.getUserById(job.workerId)
                if (worker != null) {
                    val earnings = worker.totalEarnings + totalPayment
                    repository.updateUser(worker.copy(totalEarnings = earnings))
                }
                
                // Notify via Chat automatic invoice
                val contractor = _currentUser.value ?: return@launch
                repository.sendMessage(
                    Message(
                        senderId = contractor.id,
                        receiverId = job.workerId,
                        senderName = contractor.fullName,
                        content = "Hi, I have marked the work as Completed and authorized your payment of $${totalPayment}. Your digital Invoice INV-2026-X${jobId}.pdf is now downloadable in your Wallet."
                    )
                )
                onCompleted()
            }
        }
    }

    // --- Review submitting ---
    fun submitReviewRating(jobId: Int, jobTitle: String, toUserId: Int, rating: Float, reviewText: String, onCompleted: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val reviewObj = Review(
                jobId = jobId,
                jobTitle = jobTitle,
                reviewerId = user.id,
                revieweeId = toUserId,
                reviewerName = user.fullName,
                rating = rating,
                reviewText = reviewText
            )
            repository.submitReview(reviewObj)

            // Recalculate average user rating
            repository.getReviewsForUser(toUserId).collect { reviewsList ->
                val count = reviewsList.size
                if (count > 0) {
                    val avg = reviewsList.map { it.rating }.sum() / count
                    val reviewee = repository.getUserById(toUserId)
                    if (reviewee != null) {
                        repository.updateUser(reviewee.copy(rating = avg.toDouble(), reviewsCount = count))
                    }
                }
            }
            onCompleted()
        }
    }

    // --- Profile modifications ---
    fun updateCompletedUserInfo(user: User, onCompleted: () -> Unit) {
        viewModelScope.launch {
            repository.updateUser(user)
            _currentUser.value = user
            _tempOnboardingUser.value = user
            onCompleted()
        }
    }
}
