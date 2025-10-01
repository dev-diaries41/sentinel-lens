package com.fpf.sentinellens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import com.fpf.sentinellens.ui.screens.surveillance.SurveillanceViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val typeVal = navBackStackEntry?.arguments?.getString("type")
    val settingsViewModel: SettingsViewModel = viewModel()
    val surveillanceViewModel: SurveillanceViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()
    val showBackButton =
        currentRoute?.startsWith("settingsDetail") == true || currentRoute == "test" || currentRoute?.startsWith(
            "addPerson"
        ) == true
    val isUpdatePopUpVisible by mainViewModel.isUpdatePopUpVisible.collectAsState()


    val headerTitle = when {
        currentRoute == "settings" -> stringResource(R.string.title_settings)
        currentRoute == "donate" -> stringResource(R.string.title_donate)
        currentRoute == "help" -> stringResource(R.string.title_help)
        currentRoute == "test" -> stringResource(R.string.title_face_id)
        currentRoute == "watchlist" -> stringResource(R.string.title_watchlist)
        currentRoute?.startsWith("addPerson") == true -> stringResource(R.string.title_add_person)
        currentRoute == "surveillance" -> stringResource(R.string.title_surveillance)
        currentRoute == "log" -> stringResource(R.string.title_log)
        currentRoute?.startsWith("settingsDetail") == true -> when (typeVal) {
            "threshold" -> stringResource(R.string.setting_similarity_threshold)
            "telegram" -> stringResource(R.string.setting_telegram_config)
            else -> ""
        }

        else -> ""
    }

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
                    title = { Text(text = headerTitle) },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentRoute != "log") {
                            IconButton(onClick = { navController.navigate("log") }) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = "Detection Log Screen"
                                )
                            }
                        }
                        if (currentRoute != "test") {
                            IconButton(onClick = { navController.navigate("test") }) {
                                Icon(
                                    imageVector = Icons.Filled.Science,
                                    contentDescription = "Face Id Screen"
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "surveillance",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("surveillance") {
                    SurveillanceScreen(viewModel = surveillanceViewModel)
                }
                composable("watchlist") {
                    WatchlistScreen(
                        onNavigate = { faceId: String? ->
                            val encoded = faceId?.let {
                                URLEncoder.encode(
                                    it,
                                    StandardCharsets.UTF_8.toString()
                                )
                            } ?: ""
                            navController.navigate("addPerson/$encoded")
                        }
                    )
                }

                composable(
                    "addPerson/{faceId}",
                    arguments = listOf(navArgument("faceId") { type = NavType.StringType }
                    )
                ) { navBackStackEntry ->
                    val faceId = navBackStackEntry.arguments?.getString("faceId")?.let {
                        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    }
                    AddPersonScreen(faceId = faceId)
                }
                composable("log") {
                    DetectionLogScreen()
                }
                composable("donate") {
                    DonateScreen()
                }
                composable("help") {
                    HelpScreen()
                }
                composable("settings") {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigate = { route: String -> navController.navigate(route) }
                    )
                }
                composable(
                    route = "settingsDetail/{type}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType })
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type") ?: ""
                    SettingsDetailScreen(
                        type = type,
                        viewModel = settingsViewModel,
                    )
                }
                composable("test") {
                    TestFaceIdScreen(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}
