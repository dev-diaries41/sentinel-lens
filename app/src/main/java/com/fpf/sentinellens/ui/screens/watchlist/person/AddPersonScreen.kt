package com.fpf.sentinellens.ui.screens.watchlist.person

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fpf.sentinellens.R
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.surveillance.DetectionTypes
import com.fpf.sentinellens.ui.components.ImageDisplay
import com.fpf.sentinellens.ui.components.common.TextInput
import com.fpf.sentinellens.ui.components.pickers.OptionPicker
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddPersonScreen(
    viewModel: AddPersonViewModel = koinViewModel(),
    faceId: Long? = null,
    onTopBarChange: (TopBarState) -> Unit,
    onBack: () -> Unit,
) {
    val newName by viewModel.newName.collectAsState()
    val newFaceImage by viewModel.newFaceImage.collectAsState()
    val faceType by viewModel.faceType.collectAsState()
    val error by viewModel.error.collectAsState()
    val title = stringResource(R.string.title_add_person)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.updateFaceImage(it) }
        }
    )

    var isSelectingType by remember { mutableStateOf(false) }

    LaunchedEffect(faceId) {
        if (faceId != null) {
            viewModel.setFace(faceId)
        }
    }

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        )
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
                ImageDisplay(
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

        OutlinedButton (
            onClick = { isSelectingType = true },
            modifier = Modifier.widthIn(max = 140.dp).align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DetectionTypes[faceType]!!,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface

                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint =MaterialTheme.colorScheme.onSurface
                )
            }
        }

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

    OptionPicker(
        isVisible = isSelectingType,
        title = stringResource(id = R.string.setting_surveillance_mode),
        selectedOption = faceType,
        options = DetectionTypes.entries.map{ it.value to it.key},
        onClose = { isSelectingType = false },
        onSelect = {
            viewModel.updateDetectionType(it)
            isSelectingType = false
        }
    )
}
