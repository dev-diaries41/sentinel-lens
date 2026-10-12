package com.fpf.sentinellens.ui.screens.watchlist.person

import android.net.Uri
import com.fpf.sentinellens.surveillance.Face
import com.fpf.sentinellens.surveillance.FaceType

data class AddPersonState(
    val name: String = "",
    val faceImage: Uri? = null,
    val faceToEdit: Face? = null,
    val faceType: FaceType = FaceType.BLACKLIST
)