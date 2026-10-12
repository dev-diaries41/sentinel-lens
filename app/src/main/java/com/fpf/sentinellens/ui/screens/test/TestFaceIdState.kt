package com.fpf.sentinellens.ui.screens.test

import android.graphics.Bitmap
import android.net.Uri
import com.fpf.sentinellens.surveillance.FaceIdResult

data class TestFaceIdState(
    val blacklistResult: FaceIdResult? = null,
    val whitelistResult: FaceIdResult? = null,
    val selectedImage: Uri? = null,
    val selectedImageBoundedBoxes: Bitmap? = null,
    val loading: Boolean = false
)