package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class LabNexRepository(private val db: AppDatabase) {

    // Users
    suspend fun getUserByEmail(email: String): User? = db.userDao.getUserByEmail(email)
    suspend fun getUserById(id: Int): User? = db.userDao.getUserById(id)
    fun getUserByIdFlow(id: Int): Flow<User?> = db.userDao.getUserByIdFlow(id)
    fun getSkilledWorkersFlow(): Flow<List<User>> = db.userDao.getSkilledWorkersFlow()
    suspend fun registerUser(user: User): Int = db.userDao.insertUser(user).toInt()
    suspend fun updateUser(user: User) = db.userDao.updateUser(user)

    // Jobs
    fun getAllJobs(): Flow<List<Job>> = db.jobDao.getAllJobsFlow()
    fun getJobsByContractor(contractorId: Int): Flow<List<Job>> = db.jobDao.getJobsByContractorFlow(contractorId)
    fun getJobsByWorker(workerId: Int): Flow<List<Job>> = db.jobDao.getJobsByWorkerFlow(workerId)
    fun getJobByIdFlow(jobId: Int): Flow<Job?> = db.jobDao.getJobByIdFlow(jobId)
    suspend fun getJobById(jobId: Int): Job? = db.jobDao.getJobById(jobId)
    suspend fun createJob(job: Job): Int = db.jobDao.insertJob(job).toInt()
    suspend fun updateJob(job: Job) = db.jobDao.updateJob(job)

    // Applications / Bids
    fun getApplicationsForJob(jobId: Int): Flow<List<JobApplication>> = db.applicationDao.getApplicationsForJobFlow(jobId)
    fun getApplicationsByWorker(workerId: Int): Flow<List<JobApplication>> = db.applicationDao.getApplicationsByWorkerFlow(workerId)
    suspend fun submitApplication(application: JobApplication): Int = db.applicationDao.insertApplication(application).toInt()
    suspend fun updateApplication(application: JobApplication) = db.applicationDao.updateApplication(application)
    suspend fun getApplicationById(id: Int): JobApplication? = db.applicationDao.getApplicationById(id)

    // Messages
    fun getChatMessages(sender: Int, receiver: Int): Flow<List<Message>> = db.messageDao.getChatMessagesFlow(sender, receiver)
    fun getMyMessages(id: Int): Flow<List<Message>> = db.messageDao.getAllMyMessagesFlow(id)
    suspend fun sendMessage(message: Message): Int = db.messageDao.insertMessage(message).toInt()

    // Reviews
    fun getReviewsForUser(userId: Int): Flow<List<Review>> = db.reviewDao.getReviewsForUserFlow(userId)
    suspend fun submitReview(review: Review): Int = db.reviewDao.insertReview(review).toInt()
    suspend fun updateReview(review: Review) = db.reviewDao.updateReview(review)

    // Seeding database helper
    suspend fun seedMockData() {
        // Only seed if there is no user setup yet
        val existingUser = db.userDao.getUserByEmail("alex@worker.com")
        if (existingUser != null) return

        // 1. Seed profiles
        val worker1Id = db.userDao.insertUser(
            User(
                email = "alex@worker.com",
                password = "123",
                role = "WORKER",
                fullName = "Alex Carter",
                phone = "+1 (555) 234-5678",
                isProfileComplete = true,
                skills = "Carpentry, Roofing, Framing, Welding",
                yearsExperience = 5,
                pricingType = "Hourly",
                hourlyRate = 45.0,
                city = "Chicago",
                radiusSec = 20,
                workHoursPerWeek = 40,
                certifications = "OSHA 10 Safety, Red Cross First Aid",
                portfolioDesc = "Specialized in commercial framing & welding. High precision finishes.",
                rating = 4.9,
                reviewsCount = 4,
                totalEarnings = 12450.0
            )
        ).toInt()

        val worker2Id = db.userDao.insertUser(
            User(
                email = "sarah@worker.com",
                password = "123",
                role = "WORKER",
                fullName = "Sarah Jenkins",
                phone = "+1 (555) 345-6789",
                isProfileComplete = true,
                skills = "Electrical, Wiring, Smart Home, Safety",
                yearsExperience = 8,
                pricingType = "Experience-based",
                hourlyRate = 55.0,
                city = "New York",
                radiusSec = 15,
                workHoursPerWeek = 35,
                certifications = "Master Electrician, HVAC Certified",
                portfolioDesc = "Licensed master electrician specializing in automated control panels.",
                rating = 4.8,
                reviewsCount = 12,
                totalEarnings = 34500.0
            )
        ).toInt()

        val contractorId = db.userDao.insertUser(
            User(
                email = "john@co.com",
                password = "123",
                role = "CONTRACTOR",
                fullName = "John Smith",
                phone = "+1 (555) 987-6543",
                isProfileComplete = true,
                companyName = "Apex Builders & Contractors",
                industry = "Commercial Logistics Construction",
                companySize = "11-50 employees",
                workTypesNeeded = "Electrical, Carpentry, Welding",
                budgetRange = "$5,000 - $25,000"
            )
        ).toInt()

        // 2. Seed Jobs
        val job1Id = db.jobDao.insertJob(
            Job(
                contractorId = contractorId,
                contractorName = "John Smith",
                title = "Warehouse Roof Truss Assembly",
                description = "Need raw carpentry and basic heavy welding skills to mount steel-reinforced framing trusses in our warehouse expansion project. Safety boot equipment must be provided by the worker.",
                skillsRequired = "Carpentry, Welding",
                budgetMin = 4500.0,
                budgetMax = 6500.0,
                timeline = "3 Weeks",
                status = "OPEN"
            )
        ).toInt()

        val job2Id = db.jobDao.insertJob(
            Job(
                contractorId = contractorId,
                contractorName = "John Smith",
                title = "Smart Control Panel Overhaul",
                description = "Rewiring of existing distribution panels with integration to Smart PLC endpoints. Must be certified master or experienced journeyman electrician.",
                skillsRequired = "Electrical, Wiring",
                budgetMin = 2500.0,
                budgetMax = 3800.0,
                timeline = "1 Week",
                status = "OPEN"
            )
        ).toInt()

        val job3Id = db.jobDao.insertJob(
            Job(
                contractorId = contractorId,
                contractorName = "John Smith",
                title = "Drywall Hanging & Finishing",
                description = "Hanging standard drywall boards in commercial logistics annex. Smooth joint mudding skills essential.",
                skillsRequired = "Drywall, Finishing",
                budgetMin = 1800.0,
                budgetMax = 3000.0,
                timeline = "10 Days",
                status = "COMPLETED",
                workerId = worker1Id,
                isPaid = true,
                completedHours = 45.0,
                invoiceDownloadPath = "INV-2026-003.pdf"
            )
        ).toInt()

        // 3. Seed Bids / Applications
        // Alex bid on Job 1
        db.applicationDao.insertApplication(
            JobApplication(
                jobId = job1Id,
                jobTitle = "Warehouse Roof Truss Assembly",
                workerId = worker1Id,
                workerName = "Alex Carter",
                workerSkills = "Carpentry, Roofing, Framing, Welding",
                workerExperience = 5,
                workerRate = 45.0,
                bidAmount = 4800.0,
                coverLetter = "Hi John! I have over 5 years of commercial truss hanging, solid carpentry, and layout framing experience. I carry my own standard safety gear and can start immediately on Monday.",
                matchPercentage = 95,
                status = "PENDING"
            )
        )

        // Sarah bid on Job 2
        db.applicationDao.insertApplication(
            JobApplication(
                jobId = job2Id,
                jobTitle = "Smart Control Panel Overhaul",
                workerId = worker2Id,
                workerName = "Sarah Jenkins",
                workerSkills = "Electrical, Wiring, Smart Home, Safety",
                workerExperience = 8,
                workerRate = 55.0,
                bidAmount = 3000.0,
                coverLetter = "Experienced electrical contractor here. I have wired more than 20 PLC distribution cabinets. Ready for the integration and available this weekend to minimize offline hours.",
                matchPercentage = 100,
                status = "PENDING"
            )
        )

        // 4. Seed Chat Messages
        db.messageDao.insertMessage(
            Message(
                senderId = worker1Id,
                receiverId = contractorId,
                senderName = "Alex Carter",
                content = "Hi John, I saw your post. Do you guys provide the welding rigs or should I bring mine?",
                timestamp = System.currentTimeMillis() - 3600000 * 3
            )
        )
        db.messageDao.insertMessage(
            Message(
                senderId = contractorId,
                receiverId = worker1Id,
                senderName = "John Smith",
                content = "We have double Lincoln electric rigs on-site. You just need proper protective shield and standard personal tools. If you can do Monday, that would be great.",
                timestamp = System.currentTimeMillis() - 3600000 * 2
            )
        )
        db.messageDao.insertMessage(
            Message(
                senderId = worker1Id,
                receiverId = contractorId,
                senderName = "Alex Carter",
                content = "Perfect. I submitted my bid and will be glad to join.",
                timestamp = System.currentTimeMillis() - 3600000 * 1
            )
        )

        // 5. Seed Review
        db.reviewDao.insertReview(
            Review(
                jobId = job3Id,
                jobTitle = "Drywall Hanging & Finishing",
                reviewerId = contractorId,
                revieweeId = worker1Id,
                reviewerName = "John Smith",
                rating = 5.0f,
                reviewText = "Amazing drywall hung with extremely smooth transitions. Finished 2 days ahead of schedule. Absolute professional!",
                timestamp = System.currentTimeMillis() - 3600000 * 24
            )
        )
    }
}
