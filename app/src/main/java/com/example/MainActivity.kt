package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Brightness3
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.features.authentication.data.repository.AuthRepositoryImpl
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.core.designsystem.MindRestTheme
import com.example.core.designsystem.components.BottomNavigationBar
import com.example.core.designsystem.components.NavigationItem
import com.example.core.designsystem.showcase.DesignSystemShowcaseScreen
import com.example.core.navigation.Screen
import com.example.features.achievements.presentation.screen.AchievementsScreen
import com.example.features.mood.presentation.screen.MoodTrackingScreen
import com.example.features.authentication.presentation.screen.LoginScreen
import com.example.features.authentication.presentation.screen.OnboardingScreen
import com.example.features.authentication.presentation.screen.RegisterScreen
import com.example.features.authentication.presentation.screen.SplashScreen
import com.example.features.home.presentation.screen.HomeScreen
import com.example.features.ikigai.presentation.screen.IkigaiAssessmentScreen
import com.example.features.ikigai.presentation.screen.IkigaiDashboardScreen
import com.example.features.ikigai.presentation.screen.IkigaiReportLoadingScreen
import com.example.features.ikigai.presentation.screen.IkigaiReportScreen
import com.example.features.journal.presentation.screen.JournalHistoryScreen
import com.example.features.journal.presentation.screen.AiJournalScreen
import com.example.features.lifestyle.presentation.screen.LifestyleScreen
import com.example.features.notification.presentation.screen.NotificationScreen
import com.example.features.profile.presentation.screen.ProfileScreen
import com.example.features.relaxation.presentation.screen.AdvancedRelaxationScreen
import com.example.features.relaxation.presentation.screen.RelaxScreen
import com.example.features.reminder.presentation.screen.ReminderScreen
import com.example.features.settings.presentation.screen.SettingsScreen
import com.example.features.sleep.presentation.screen.SleepHubScreen
import com.example.features.sleep.presentation.screen.SleepTrackingScreen
import com.example.features.statistics.presentation.screen.StatisticsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDark by remember { mutableStateOf(true) }
            MindRestTheme(darkTheme = isDark) {
                MainApp(isDark = isDark, onToggleDark = { isDark = !isDark })
            }
        }
    }
}

@Composable
fun MainApp(
    isDark: Boolean = true,
    onToggleDark: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared AuthRepository instance — dipakai SplashScreen untuk initial routing
    // dan ProfileScreen untuk sign-out.
    val authRepository = remember { AuthRepositoryImpl() }
    val sessionStatus by authRepository.sessionStatus.collectAsState()
    val scope = rememberCoroutineScope()

    // Tab items mapped to main screens
    val tabItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("Sleep", Icons.Filled.Brightness3, Icons.Outlined.Brightness3),
        NavigationItem("Relax", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
        NavigationItem("Ikigai", Icons.Filled.Explore, Icons.Outlined.Explore),
        NavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val tabRoutes = listOf(
        Screen.Home.route,
        Screen.Sleep.route,
        Screen.Relaxation.route,
        Screen.Ikigai.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                val selectedIndex = tabRoutes.indexOf(currentRoute).coerceAtLeast(0)
                BottomNavigationBar(
                    items = tabItems,
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        val targetRoute = tabRoutes[index]
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                // Pantau session Supabase: kalau sudah login langsung ke Home,
                // kalau belum ke Login. Loading & RefreshError dianggap belum login.
                LaunchedEffect(sessionStatus) {
                    if (currentRoute != Screen.Splash.route) return@LaunchedEffect
                    when (sessionStatus) {
                        is SessionStatus.Authenticated -> {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        is SessionStatus.NotAuthenticated,
                        is SessionStatus.RefreshFailure -> {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                        SessionStatus.Initializing -> Unit
                    }
                }
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // Register Screen
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Tab
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToJournal = { navController.navigate(Screen.Journal.route) },
                    onNavigateToAiJournal = { navController.navigate(Screen.AiJournal.route) },
                    onNavigateToRelaxation = { navController.navigate(Screen.Relaxation.route) },
                    onNavigateToSleepTracking = { navController.navigate(Screen.Sleep.route) },
                    onNavigateToIkigai = { navController.navigate(Screen.Ikigai.route) },
                    onNavigateToMoodTracking = { navController.navigate(Screen.MoodTracking.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToRecommendations = { navController.navigate(Screen.Sleep.route) },
                    onLogSleepClick = { navController.navigate(Screen.Sleep.route) },
                    onMoodSelected = { _ -> },
                    onNavigateToLifestyle = { navController.navigate(Screen.Lifestyle.route) },
                    onNavigateToReminder = { navController.navigate(Screen.Reminder.route) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    // T-004 (FR-015): wiring callback untuk widget Ikigai Progress.
                    // Empty state CTA → onNavigateToIkigaiAssessment.
                    // Filled state CTA → onNavigateToIkigaiReport (lihat laporan terbaru).
                    onNavigateToIkigaiAssessment = { navController.navigate(Screen.IkigaiAssessment.route) },
                    onNavigateToIkigaiReport = { navController.navigate(Screen.IkigaiReport.route) },
                )
            }

            // Sleep Tab
            composable(Screen.Sleep.route) {
                SleepHubScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogSleepClick = { navController.navigate(Screen.SleepTracking.route) }
                )
            }

            // Relaxation Tab — primary route RelaxScreen (T-009 ExoPlayer audio player).
            // AdvancedRelaxationScreen (breathing + movement + audio mixer) sementara
            // tidak di-route, menunggu T-009b untuk UX integration.
            composable(Screen.Relaxation.route) {
                RelaxScreen()
            }

            // AdvancedRelaxationScreen sebagai secondary destination — akan diaktifkan
            // oleh T-009b (tombol "Mode Lanjutan" di RelaxScreen).
            composable(Screen.AdvancedRelaxation.route) {
                AdvancedRelaxationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Ikigai Tab
            composable(Screen.Ikigai.route) {
                IkigaiDashboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartAssessment = { navController.navigate(Screen.IkigaiAssessment.route) }
                )
            }

            // Profile Tab
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    onNavigateToAchievements = { navController.navigate(Screen.Achievements.route) },
                    onSignOut = {
                        scope.launch {
                            authRepository.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Journal Secondary Screen
            composable(Screen.Journal.route) {
                JournalHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onStartNewSessionClick = { navController.navigate(Screen.AiJournal.route) }
                )
            }

            // AI Journal Screen
            composable(Screen.AiJournal.route) {
                AiJournalScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Lifestyle Secondary Screen
            composable(Screen.Lifestyle.route) {
                LifestyleScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRelaxation = { navController.navigate(Screen.Relaxation.route) },
                    onNavigateToJournal = { navController.navigate(Screen.Journal.route) }
                )
            }
            
            // Notifications Secondary Screen
            composable(Screen.Notifications.route) {
                NotificationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReminderSettings = { navController.navigate(Screen.Reminder.route) }
                )
            }

            // Reminder Secondary Screen
            composable(Screen.Reminder.route) {
                ReminderScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Statistics Secondary Screen
            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings Secondary Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    dark = isDark,
                    toggleDark = onToggleDark
                )
            }

            // Achievements Secondary Screen
            composable(Screen.Achievements.route) {
                AchievementsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Mood Tracking Secondary Screen (TASK 1.2: dead code diaktifkan)
            composable(Screen.MoodTracking.route) {
                MoodTrackingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sleep Tracking Secondary Screen (TASK 1.2: dead code diaktifkan)
            composable(Screen.SleepTracking.route) {
                SleepTrackingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Ikigai Assessment (TASK 2.3: onboarding 6 pertanyaan)
            composable(Screen.IkigaiAssessment.route) {
                IkigaiAssessmentScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAssessmentSaved = { _ ->
                        navController.navigate(Screen.IkigaiReport.route) {
                            popUpTo(Screen.IkigaiAssessment.route) { inclusive = true }
                        }
                    }
                )
            }

            // Ikigai Report Loading placeholder (TASK 2.3: optional entry during
            // regeneration). Konten UI final dipasang di TASK 3.3.
            composable(Screen.IkigaiReportLoading.route) {
                IkigaiReportLoadingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Ikigai Report Display (TASK 3.3: tampilkan 4 lingkaran + laporan + rekomendasi).
            composable(Screen.IkigaiReport.route) {
                IkigaiReportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    autoTriggerFromAssessment = true,
                    onStartAssessment = {
                        navController.navigate(Screen.IkigaiAssessment.route) {
                            popUpTo(Screen.IkigaiReport.route) { inclusive = true }
                        }
                    },
                )
            }

            // Dev/QA only — design-system showcase gallery.
            // Reachable via the `mindrest://designsystem` deep link (adb am start).
            composable(
                route = Screen.DesignSystem.route,
                deepLinks = listOf(navDeepLink { uriPattern = "mindrest://designsystem" })
            ) {
                DesignSystemShowcaseScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
