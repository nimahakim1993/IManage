package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.NoteEntity
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.NoteBoxViewModel
import com.nima.app.imanage.presentation.viewmodel.NoteViewModel
import com.nima.app.imanage.ui.theme.vazirFontFamily
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateNoteScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    boxId: Int,
    noteId: Int = -1,
    noteViewModel: NoteViewModel = koinViewModel(),
    boxViewModel: NoteBoxViewModel = koinViewModel()
) {
    val isEdit = noteId != -1
    val createTitle = stringResource(R.string.create_note_title)
    val editTitle = stringResource(R.string.edit_note_title)

    LaunchedEffect(noteId, boxId) {
        if (isEdit) noteViewModel.loadNote(noteId)
        noteViewModel.setBoxId(boxId)
        boxViewModel.loadBox(boxId)
    }
    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = if (isEdit) editTitle else createTitle,
                showBack = true
            )
        )
    }

    val selectedNote by noteViewModel.selectedNote.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedNote, isEdit) {
        if (isEdit && selectedNote != null && !loaded) {
            title = selectedNote!!.title
            content = selectedNote!!.content
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
            label = { Text(stringResource(R.string.note_title_label)) },
            placeholder = { Text(stringResource(R.string.note_title_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text(stringResource(R.string.note_content_label)) },
            placeholder = { Text(stringResource(R.string.note_content_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            maxLines = 20
        )

        Spacer(modifier = Modifier.height(8.dp))

        val scope = rememberCoroutineScope()

        Button(
            onClick = {
                scope.launch {
                    val now = System.currentTimeMillis()
                    val note = NoteEntity(
                        id = if (isEdit) noteId else 0,
                        boxId = boxId,
                        title = title.trim(),
                        content = content.trim(),
                        createdAt = selectedNote?.createdAt ?: now,
                        updatedAt = now
                    )
                    noteViewModel.saveNote(note)
                    boxViewModel.selectedBox.value?.let { b ->
                        boxViewModel.saveBox(b.copy(updatedAt = now))
                    }
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
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
}
