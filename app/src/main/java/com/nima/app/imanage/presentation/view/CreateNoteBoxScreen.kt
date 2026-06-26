package com.nima.app.imanage.presentation.view

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.NoteBoxViewModel
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import com.nima.app.imanage.ui.theme.vazirFontFamily
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateNoteBoxScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    boxId: Int = -1,
    viewModel: NoteBoxViewModel = koinViewModel()
) {
    val isEdit = boxId != -1
    val createTitle = stringResource(R.string.create_note_box_title)
    val editTitle = stringResource(R.string.edit_note_box_title)

    LaunchedEffect(boxId) {
        if (isEdit) viewModel.loadBox(boxId)
    }
    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = if (isEdit) editTitle else createTitle,
                showBack = true
            )
        )
    }

    val selectedBox by viewModel.selectedBox.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedBox, isEdit) {
        if (isEdit && selectedBox != null && !loaded) {
            title = selectedBox!!.title
            description = selectedBox!!.description
            colorIndex = selectedBox!!.colorIndex
            loaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.note_box_title_label)) },
            placeholder = { Text(stringResource(R.string.note_box_title_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.note_box_description_label)) },
            placeholder = { Text(stringResource(R.string.note_box_description_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        ColorPalettePicker(
            selected = colorIndex,
            onSelect = { colorIndex = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        val scope = rememberCoroutineScope()

        Button(
            onClick = {
                scope.launch {
                    val now = System.currentTimeMillis()
                    val box = NoteBoxEntity(
                        id = if (isEdit) boxId else 0,
                        title = title.trim().ifEmpty { "Untitled" },
                        description = description.trim(),
                        colorIndex = colorIndex,
                        createdAt = selectedBox?.createdAt ?: now,
                        updatedAt = now
                    )
                    viewModel.saveBox(box)
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.confirm),
                fontFamily = vazirFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ColorPalettePicker(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val labels = listOf(
        stringResource(R.string.color_purple),
        stringResource(R.string.color_blue),
        stringResource(R.string.color_teal),
        stringResource(R.string.color_pink),
        stringResource(R.string.color_orange),
        stringResource(R.string.color_green)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NoteBoxPalettes.forEachIndexed { index, palette ->
            val isSelected = index == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(index) }
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 56.dp else 44.dp)
                        .clip(CircleShape)
                        .background(palette.primary)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    fontFamily = vazirFontFamily
                )
            }
        }
    }
}
