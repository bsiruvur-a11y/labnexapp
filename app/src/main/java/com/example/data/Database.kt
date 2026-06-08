package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val role: String, // "WORKER" or "CONTRACTOR"
    val fullName: String,
    val phone: String,
    val isProfileComplete: Boolean = false,

    // Worker Onboarding details
    val skills: String = "", // Comma-separated: "Carpentry, Electrical"
    val yearsExperience: Int = 0,
    val pricingType: String = "Hourly", // "Hourly", "Fixed", "Experience-based"
    val hourlyRate: Double = 0.0,
    val city: String = "",
    val radiusSec: Int = 15, // in miles
    val workHoursPerWeek: Int = 40,
    val certifications: String = "", // Comma-separated
    val portfolioDesc: String = "",
    val portfolioPhotoUri: String = "", // Uri path string
    val rating: Double = 4.8,
    val reviewsCount: Int = 3,
    val totalEarnings: Double = 0.0,

    // Contractor Onboarding details
    val companyName: String = "",
    val industry: String = "",
    val companySize: String = "1-10 employees",
    val workTypesNeeded: String = "", // Comma-separated
    val budgetRange: String = ""
)

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contractorId: Int,
    val contractorName: String,
    val title: String,
    val description: String,
    val skillsRequired: String, // Comma-separated
    val budgetMin: Double,
    val budgetMax: Double,
    val timeline: String,
    val status: String, // "OPEN", "IN_PROGRESS", "COMPLETED"
    val timestamp: Long = System.currentTimeMillis(),
    val workerId: Int = 0, // Hired worker ID
    val completedHours: Double = 0.0,
    val isPaid: Boolean = false,
    val invoiceDownloadPath: String = "",
    val milestonesJoined: String = "",
    val filesJoined: String = ""
)

@Entity(tableName = "applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val workerId: Int,
    val workerName: String,
    val workerSkills: String,
    val workerExperience: Int,
    val workerRate: Double,
    val bidAmount: Double,
    val coverLetter: String,
    val matchPercentage: Int, // Auto calculated
    val status: String, // "PENDING", "ACCEPTED" (Hired), "REJECTED", "WITHDRAWN"
    val timestamp: Long = System.currentTimeMillis(),
    val counterAmount: Double = 0.0,
    val counterNote: String = "",
    val lastCounterBy: String = "" // "WORKER", "CONTRACTOR"
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: Int,
    val receiverId: Int,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val jobId: Int = 0, // Associated chat context
    val attachmentUri: String = "" // Simulated file transfer
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val reviewerId: Int,
    val revieweeId: Int,
    val reviewerName: String,
    val rating: Float,
    val reviewText: String,
    val response: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdFlow(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE role = 'WORKER' AND isProfileComplete = 1")
    fun getSkilledWorkersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY timestamp DESC")
    fun getAllJobsFlow(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE contractorId = :contractorId ORDER BY timestamp DESC")
    fun getJobsByContractorFlow(contractorId: Int): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getJobsByWorkerFlow(workerId: Int): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Int): Job?

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun getJobByIdFlow(id: Int): Flow<Job?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job): Long

    @Update
    suspend fun updateJob(job: Job)
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun getApplicationsForJobFlow(jobId: Int): Flow<List<JobApplication>>

    @Query("SELECT * FROM applications WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getApplicationsByWorkerFlow(workerId: Int): Flow<List<JobApplication>>

    @Query("SELECT * FROM applications WHERE id = :id LIMIT 1")
    suspend fun getApplicationById(id: Int): JobApplication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplication): Long

    @Update
    suspend fun updateApplication(application: JobApplication)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getChatMessagesFlow(user1: Int, user2: Int): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE senderId = :id OR receiverId = :id ORDER BY timestamp DESC")
    fun getAllMyMessagesFlow(id: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY timestamp DESC")
    fun getReviewsForUserFlow(userId: Int): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)
}

// --- Database Configuration ---

@Database(
    entities = [User::class, Job::class, JobApplication::class, Message::class, Review::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val jobDao: JobDao
    abstract val applicationDao: ApplicationDao
    abstract val messageDao: MessageDao
    abstract val reviewDao: ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "labnex_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
