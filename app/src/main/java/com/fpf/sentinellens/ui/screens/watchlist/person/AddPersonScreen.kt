package com.fpf.sentinellens.ui.screens.watchlist.person

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.sentinellens.R
import com.fpf.sentinellens.data.faces.DetectionTypes
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.ui.components.MediaStoreImage
import com.fpf.sentinellens.ui.components.SelectorItem
import com.fpf.sentinellens.ui.components.TextInput

@Composable
fun AddPersonScreen(viewModel: AddPersonViewModel = viewModel(), faceId: String? = null) {
    val newName by viewModel.newName.observeAsState("")
    val newFaceImage by viewModel.newFaceImage.observeAsState(null)
    val faceType by viewModel.faceType.observeAsState(FaceType.BLACKLIST)
    val error by viewModel.error.observeAsState(null)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.updateFaceImage(it) }
        }
    )

    LaunchedEffect(faceId) {
        if(!faceId.isNullOrBlank()){
            viewModel.onEditing(faceId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            if (newFaceImage != null) {
                MediaStoreImage(
                    uri = newFaceImage!!,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextInput(
            label = "Name",
            value = newName,
            onValueChange = { viewModel.updateName(it) },
            placeholder = { Text("Enter name...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SelectorItem(
            label = stringResource(id = R.string.setting_surveillance_mode),
            showLabel = false,
            selectedOption = DetectionTypes[faceType]!!,
            options = DetectionTypes.values.toList(),
            onOptionSelected = { option ->
                val selected = DetectionTypes.entries
                    .first { it.value == option }
                    .key
                viewModel.updateDetectionType(selected)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.addFace() },
            enabled = newName.isNotBlank() && newFaceImage != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add person")
        }
    }
}
