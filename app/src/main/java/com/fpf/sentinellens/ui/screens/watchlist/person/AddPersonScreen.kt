package com.fpf.sentinellens.ui.screens.watchlist.person

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val title = stringResource(R.string.title_add_person)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onAction(AddPersonAction.UpdateFaceImage(it)) }
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

    LaunchedEffect(Unit) {
        viewModel.error.collect {
            error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape = MaterialTheme.shapes.medium)
                .clip(shape = MaterialTheme.shapes.medium)
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            if (state.faceImage != null) {
                ImageDisplay(
                    uri = state.faceImage!!,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize(0.8f)
                    )
                    Text(
                        text= "Upload image",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextInput(
            label = "Name",
            value = state.name,
            onValueChange = { viewModel.onAction(AddPersonAction.UpdateName(it)) },
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
                    text = DetectionTypes[state.faceType]!!,
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
            onClick = { viewModel.onAction(AddPersonAction.SaveFace) },
            enabled = state.name.isNotBlank() && state.faceImage != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Save",
                modifier = Modifier.padding(end = 4.dp)
            )
            Text("Save")
        }
    }

    OptionPicker(
        isVisible = isSelectingType,
        title = stringResource(id = R.string.setting_surveillance_mode),
        selectedOption = state.faceType,
        options = DetectionTypes.entries.map{ it.value to it.key},
        onClose = { isSelectingType = false },
        onSelect = {
            viewModel.onAction(AddPersonAction.UpdateFaceType(it))
            isSelectingType = false
        }
    )
}
