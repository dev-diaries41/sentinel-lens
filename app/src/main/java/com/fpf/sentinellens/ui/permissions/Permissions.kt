package com.fpf.sentinellens.ui.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect


@Composable
fun RequestPermissions(
    onPermissionsResult: (notificationGranted: Boolean, storageGranted: Boolean, cameraGranted: Boolean) -> Unit
) {
    val permissionsToRequest = mutableListOf<String>()

    permissionsToRequest.add(Manifest.permission.CAMERA)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            true
        }

        val storageGranted = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        } else {
            true
        }

        val cameraGranted = permissions[Manifest.permission.CAMERA] == true

        onPermissionsResult(notificationGranted, storageGranted, cameraGranted)
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            onPermissionsResult(true, true, false)
        }
    }
}
