package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.Screen
import com.example.data.LabNexViewModel
import com.example.ui.LoginScreen
import com.example.ui.SignUpScreen
import com.example.ui.WelcomeScreen
import com.example.ui.WorkerOnboardingScreen
import com.example.ui.WorkerDashboardScreen
import com.example.ui.ContractorOnboardingScreen
import com.example.ui.ContractorDashboardScreen
import com.example.ui.ChatRoomScreen
import com.example.ui.JobDetailsScreen
import com.example.ui.WorkerProfileDetailsScreen
import com.example.ui.TransactionsScreen
import com.example.ui.ContractorProfileDetailsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel by lazy { LabNexViewModel(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color(0xFF191624)
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        label = "MainFlowNavigationCrossfade"
                    ) { screen ->
                        when (screen) {
                            is Screen.Welcome -> WelcomeScreen(viewModel)
                            is Screen.Login -> LoginScreen(viewModel)
                            is Screen.SignUp -> SignUpScreen(viewModel)
                            is Screen.WorkerOnboarding -> WorkerOnboardingScreen(viewModel)
                            is Screen.ContractorOnboarding -> ContractorOnboardingScreen(viewModel)
                            is Screen.WorkerMain -> WorkerDashboardScreen(
                                viewModel = viewModel,
                                onBidTap = { jobId ->
                                    viewModel.navigateTo(Screen.JobDetails(jobId))
                                }
                            )
                            is Screen.ContractorMain -> ContractorDashboardScreen(
                                viewModel = viewModel,
                                onViewChatTap = { workerId ->
                                    viewModel.navigateTo(Screen.ChatRoom(workerId))
                                }
                            )
                            is Screen.ChatRoom -> ChatRoomScreen(
                                viewModel = viewModel,
                                otherUserId = screen.otherUserId
                            )
                            is Screen.JobDetails -> JobDetailsScreen(
                                viewModel = viewModel,
                                jobId = screen.jobId
                            )
                            is Screen.WorkerProfileDetails -> WorkerProfileDetailsScreen(
                                viewModel = viewModel,
                                workerId = screen.workerId
                            )
                            is Screen.Transactions -> TransactionsScreen(
                                viewModel = viewModel,
                                jobId = screen.jobId
                            )
                            is Screen.ContractorProfileDetails -> ContractorProfileDetailsScreen(
                                viewModel = viewModel,
                                contractorId = screen.contractorId
                            )
                        }
                    }
                }
            }
        }
    }
}
