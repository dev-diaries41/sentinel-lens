package com.fpf.sentinellens.ui.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RequestPermissions(
    onPermissionsResult: (
        notificationGranted: Boolean,
        cameraGranted: Boolean,
        audioGranted: Boolean
    ) -> Unit
) {
    val permissionsToRequest = mutableListOf<String>()

    permissionsToRequest.add(Manifest.permission.CAMERA)
    permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val notificationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.POST_NOTIFICATIONS] == true
            } else {
                true
            }

        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true

        onPermissionsResult(notificationGranted, cameraGranted, audioGranted)
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            onPermissionsResult(true, false, false)
        }
    }
}