package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.db.entity.NoteEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.NoteBoxViewModel
import com.nima.app.imanage.presentation.viewmodel.NoteViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import com.nima.app.imanage.ui.theme.vazirFontFamily
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoteBoxDetailScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    boxId: Int,
    boxViewModel: NoteBoxViewModel = koinViewModel(),
    noteViewModel: NoteViewModel = koinViewModel()
) {
    val addDesc = stringResource(R.string.add)
    val editDesc = stringResource(R.string.edit)
    val detailTitle = stringResource(R.string.note_box_detail_title)

    val selectedBox by boxViewModel.selectedBox.collectAsState()
    val boxes by boxViewModel.boxes.collectAsState()

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(boxId) {
        boxViewModel.loadBox(boxId)
        noteViewModel.setBoxId(boxId)
    }

    val box = selectedBox ?: boxes.firstOrNull { it.id == boxId }

    LaunchedEffect(box?.title, toggleEditMode) {
        setToolbar(
            ToolbarConfig(
                title = box?.title ?: detailTitle,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.Add,
                        contentDescription = addDesc,
                        onClick = {
                            navController.navigate(Screen.CreateNote.createRoute(boxId))
                        }
                    ),
                    ToolbarAction(
                        icon = if (toggleEditMode) Icons.Default.EditOff else Icons.Default.Edit,
                        contentDescription = editDesc,
                        onClick = { toggleEditMode = !toggleEditMode }
                    )
                )
            )
        )
    }

    val notes by noteViewModel.notes.collectAsState()
    var removingNote by remember { mutableStateOf<NoteEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (box != null) {
            DetailBackdrop(palette = NoteBoxPalettes.getOrElse(box.colorIndex) { NoteBoxPalettes.first() })
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (box != null) {
                BoxHeader(box = box, noteCount = notes.size)
            }

            if (notes.isEmpty()) {
                EmptyNotesDetailState(
                    onCreate = { navController.navigate(Screen.CreateNote.createRoute(boxId)) }
                )
            } else {
                val boxPalette = remember(box?.colorIndex) {
                    NoteBoxPalettes.getOrElse(box?.colorIndex ?: 0) { NoteBoxPalettes.first() }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteListItem(
                            note = note,
                            editMode = toggleEditMode,
                            palette = boxPalette,
                            onClick = {
                                navController.navigate(
                                    Screen.CreateNote.createRoute(
                                        boxId,
                                        note.id
                                    )
                                )
                            },
                            onEdit = {
                                navController.navigate(
                                    Screen.CreateNote.createRoute(
                                        boxId,
                                        note.id
                                    )
                                )
                            },
                            onDelete = { removingNote = note }
                        )
                    }
                }
            }
        }
    }

    removingNote?.let { note ->
        ActionDialog(
            onDismiss = { removingNote = null },
            onPositiveClicked = {
                noteViewModel.removeNote(note)
                box?.let { b ->
                    boxViewModel.saveBoxAsync(b.copy(updatedAt = System.currentTimeMillis()))
                }
                removingNote = null
            }
        )
    }
}

@Composable
private fun DetailBackdrop(palette: com.nima.app.imanage.ui.theme.NoteBoxPalette) {
    val brush = Brush.linearGradient(
        colors = listOf(
            palette.primary.copy(alpha = 0.15f),
            palette.secondary.copy(alpha = 0.10f),
            Color.Transparent
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    )
}

@Composable
private fun BoxHeader(box: NoteBoxEntity, noteCount: Int) {
    val palette = NoteBoxPalettes.getOrElse(box.colorIndex) { NoteBoxPalettes.first() }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val cardBrush = Brush.linearGradient(
        colors = listOf(
            palette.primary.copy(alpha = if (isDark) 0.85f else 0.95f),
            palette.secondary.copy(alpha = if (isDark) 0.95f else 1f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.6f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = box.title,
                            color = Color.White,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.notes_count, noteCount),
                            color = Color.White.copy(alpha = 0.85f),
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
                if (box.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = box.description,
                        color = Color.White,
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteListItem(
    note: NoteEntity,
    editMode: Boolean,
    palette: com.nima.app.imanage.ui.theme.NoteBoxPalette,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val glassSurface = if (isDark) Color.Black.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.85f)
    val glassBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.55f)
    val textColor = if (isDark) Color.White else Color.Black
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.65f)
    val contentSurface = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(if (isDark) 4.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = glassSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = glassBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NoteIconBadge(palette = palette)
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = note.title.ifBlank { "\u2026" },
                    color = textColor,
                    fontFamily = vazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                AnimatedVisibility(visible = editMode) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val context = LocalContext.current
                val copiedMsg = stringResource(R.string.copied_to_clipboard)
                val copyDesc = stringResource(R.string.copy)
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(contentSurface)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = note.content,
                            color = subtitleColor,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
                            clipboard?.setPrimaryClip(ClipData.newPlainText("note", note.content))
                            Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = copyDesc,
                            tint = textColor.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteIconBadge(palette: com.nima.app.imanage.ui.theme.NoteBoxPalette) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(palette.accent.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.StickyNote2,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyNotesDetailState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.StickyNote2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.empty_notes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.empty_notes_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(
            onClick = onCreate,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.add),
                fontFamily = vazirFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
