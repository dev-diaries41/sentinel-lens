package com.fpf.sentinellens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.fpf.sentinellens.ui.components.nav.BottomNavigationBar
import com.fpf.sentinellens.navigation.NavDataKeys
import com.fpf.sentinellens.navigation.Routes
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.ui.components.UpdatePopUp
import com.fpf.sentinellens.ui.screens.donate.DonateScreen
import com.fpf.sentinellens.ui.screens.help.HelpScreen
import com.fpf.sentinellens.ui.screens.log.DetectionLogScreen
import com.fpf.sentinellens.ui.screens.watchlist.person.AddPersonScreen
import com.fpf.sentinellens.ui.screens.watchlist.WatchlistScreen
import com.fpf.sentinellens.ui.screens.settings.SettingsScreen
import com.fpf.sentinellens.ui.screens.settings.SettingsViewModel
import com.fpf.sentinellens.ui.screens.test.TestFaceIdScreen
import com.fpf.sentinellens.ui.screens.settings.SettingsDetailScreen
import com.fpf.sentinellens.ui.screens.surveillance.SurveillanceScreen
import org.koin.compose.viewmodel.koinViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val topBarState = remember { mutableStateOf(TopBarState()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val mainViewModel: MainViewModel = koinViewModel()
    val isUpdatePopUpVisible by mainViewModel.isUpdatePopUpVisible.collectAsState()

    if (isUpdatePopUpVisible) {
        UpdatePopUp(
            isVisible = true,
            updates = mainViewModel.getUpdates(),
            onClose = { mainViewModel.closeUpdatePopUp() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(topBarState.value.title)
                    },
                    navigationIcon = {
                        topBarState.value.navigationIcon?.invoke()
                    },
                    actions = {
                        topBarState.value.actions?.invoke(this)
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.SURVEILLANCE,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Routes.SURVEILLANCE) {
                    SurveillanceScreen(
                        onTopBarChange = { topBarState.value = it },
                        onViewLogs = {navController.navigate(Routes.LOG)},
                        onTestSurveillance = {navController.navigate(Routes.TEST)},
                    )
                }
                composable(Routes.WATCHLIST) {
                    WatchlistScreen(
                        onManagePerson = { faceId: String? ->
                            val encoded = faceId?.let {
                                URLEncoder.encode(
                                    it,
                                    StandardCharsets.UTF_8.toString()
                                )
                            } ?: ""
                            navController.navigate(Routes.addPerson(encoded))
                        },
                        onTopBarChange = { topBarState.value = it },
                        onViewLogs = {navController.navigate(Routes.LOG)},
                        onTestSurveillance = {navController.navigate(Routes.TEST)},
                        )
                }

                composable(
                    Routes.ADD_PERSON,
                    arguments = listOf(navArgument(NavDataKeys.FACE_ID) { type = NavType.StringType }
                    )
                ) { navBackStackEntry ->
                    val faceId = navBackStackEntry.arguments?.getString(NavDataKeys.FACE_ID)?.let {
                        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    }
                    AddPersonScreen(
                        faceId = faceId,
                        onTopBarChange = { topBarState.value = it },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.LOG) {
                    DetectionLogScreen(
                        onTopBarChange = { topBarState.value = it },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.DONATE) {
                    DonateScreen(
                        onTopBarChange = { topBarState.value = it },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.HELP) {
                    HelpScreen(
                        onTopBarChange = { topBarState.value = it },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onTopBarChange = { topBarState.value = it },
                        onNavigate = { route: String -> navController.navigate(route) }
                    )
                }
                composable(
                    route = Routes.SETTINGS_DETAIL,
                    arguments = listOf(navArgument(NavDataKeys.SETTINGS_TYPE) { type = NavType.StringType })
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString(NavDataKeys.SETTINGS_TYPE) ?: ""
                    SettingsDetailScreen(
                        onTopBarChange = { topBarState.value = it },
                        type = type,
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.TEST) {
                    TestFaceIdScreen(
                        onTopBarChange = { topBarState.value = it },
                        onBack = { navController.popBackStack() },
                        appSettings = settingsViewModel.appSettings
                    )
                }
            }
        }
    }
}
