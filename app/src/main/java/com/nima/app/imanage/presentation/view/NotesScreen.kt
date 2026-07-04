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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.NoteBoxEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.NoteBoxViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.theme.NoteBoxPalettes
import com.nima.app.imanage.ui.theme.vazirFontFamily
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotesScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: NoteBoxViewModel = koinViewModel()
) {
    val boxes by viewModel.boxes.collectAsState()
    val notesTitle = stringResource(R.string.notes_title)
    val addDesc = stringResource(R.string.add)
    val editDesc = stringResource(R.string.edit)

    var removingBox by remember { mutableStateOf<NoteBoxEntity?>(null) }
    var toggleEditMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(toggleEditMode) {
        setToolbar(
            ToolbarConfig(
                title = notesTitle,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.Add,
                        contentDescription = addDesc,
                        onClick = {
                            navController.navigate(Screen.CreateNoteBox.createRoute())
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

    Box(modifier = Modifier.fillMaxSize()) {
        NotesBackdrop()

        if (boxes.isEmpty()) {
            EmptyNotesState(
                onCreate = { navController.navigate(Screen.CreateNoteBox.createRoute()) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(boxes, key = { it.id }) { box ->
                    NoteBoxCard(
                        box = box,
                        editMode = toggleEditMode,
                        onClick = { navController.navigate(Screen.NoteBoxDetail.createRoute(box.id)) },
                        onEdit = { navController.navigate(Screen.CreateNoteBox.createRoute(box.id)) },
                        onDelete = { removingBox = box }
                    )
                }
            }

            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.CreateNoteBox.createRoute()) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = addDesc) },
                text = {
                    Text(
                        text = stringResource(R.string.add),
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    }

    removingBox?.let { box ->
        ActionDialog(
            onDismiss = { removingBox = null },
            onPositiveClicked = {
                viewModel.removeBox(box)
                removingBox = null
            }
        )
    }
}

@Composable
private fun NotesBackdrop() {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val brush = Brush.linearGradient(
        colors = listOf(
            primary.copy(alpha = 0.12f),
            secondary.copy(alpha = 0.18f),
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
private fun EmptyNotesState(onCreate: () -> Unit) {
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
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.empty_note_boxes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.empty_note_boxes_hint),
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

@Composable
private fun NoteBoxCard(
    box: NoteBoxEntity,
    editMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = remember(box.colorIndex) {
        NoteBoxPalettes.getOrElse(box.colorIndex) { NoteBoxPalettes.first() }
    }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val glassBorder = Color.White.copy(alpha = if (isDark) 0.18f else 0.55f)

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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(if (isDark) 8.dp else 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .border(
                    width = 1.dp,
                    color = glassBorder,
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconBadge(palette = palette)
                    Spacer(modifier = Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = box.title,
                            color = Color.White,
                            fontFamily = vazirFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }

                if (box.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = box.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = vazirFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AnimatedVisibility(visible = editMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GlassActionButton(
                            icon = Icons.Default.Edit,
                            label = stringResource(R.string.edit),
                            onClick = onEdit
                        )
                        GlassActionButton(
                            icon = Icons.Default.Delete,
                            label = stringResource(R.string.delete),
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassIconBadge(palette: com.nima.app.imanage.ui.theme.NoteBoxPalette) {
    Box(
        modifier = Modifier
            .size(52.dp)
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
            imageVector = Icons.Default.NoteAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun GlassActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontFamily = vazirFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
