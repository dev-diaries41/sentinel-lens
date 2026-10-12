package com.fpf.sentinellens.ui.screens.watchlist.person

import android.net.Uri
import com.fpf.sentinellens.surveillance.FaceType

sealed interface AddPersonAction {
    data class UpdateName(val name: String): AddPersonAction
    data class UpdateFaceImage(val uri: Uri): AddPersonAction
    data class UpdateFaceType(val type: FaceType): AddPersonAction
    data object SaveFace: AddPersonAction
}