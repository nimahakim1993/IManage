package com.nima.app.imanage.presentation.view

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.PasswordIconType
import com.nima.app.imanage.data.db.entity.PasswordItemEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.PasswordItemInput
import com.nima.app.imanage.presentation.viewmodel.PasswordItemViewModel
import com.nima.app.imanage.ui.component.ActionDialog
import com.nima.app.imanage.ui.component.EmptyState
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.BiometricHelper
import com.nima.app.imanage.util.BiometricHelper.AuthType
import org.koin.androidx.compose.koinViewModel

private const val MASK = "••••••••"

@Composable
fun PasswordItemsScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    viewModel: PasswordItemViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsState()
    val context = LocalContext.current

    val authType = remember { BiometricHelper.availableAuthType(context) }
    var authenticated by remember { mutableStateOf(authType == AuthType.NONE) }
    var authRequested by remember { mutableStateOf(false) }

    var toggleEditMode by rememberSaveable { mutableStateOf(false) }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PasswordItemEntity?>(null) }
    var removing by remember { mutableStateOf<PasswordItemEntity?>(null) }

    // id -> decrypted password, kept in memory only while revealed
    val revealedPasswords = remember { mutableStateMapOf<Int, String>() }

    val passwordsTitle = stringResource(R.string.passwords_title)
    val addDesc = stringResource(R.string.add)
    val editDesc = stringResource(R.string.edit)

    LaunchedEffect(items.isEmpty()) {
        if (authRequested) return@LaunchedEffect
        authRequested = true
        if (items.isEmpty()) {
            authenticated = true; return@LaunchedEffect
        }
        if (authType == AuthType.NONE) { authenticated = true; return@LaunchedEffect }
        val activity = context.findFragmentActivity()
        if (activity == null) { authenticated = true; return@LaunchedEffect }
        BiometricHelper.authenticate(
            activity = activity,
            title = context.getString(R.string.biometric_reveal_title),
            subtitle = context.getString(R.string.biometric_reveal_subtitle),
            authType = authType,
            onSuccess = { authenticated = true },
            onError = { /* stay locked; user can retry by tapping */ }
        )
    }

    LaunchedEffect(items.isEmpty()) {
        if (items.isEmpty()) toggleEditMode = false
    }

    LaunchedEffect(authenticated, toggleEditMode, items.isEmpty()) {
        val actions = mutableListOf(
            ToolbarAction(
                icon = Icons.Default.Add,
                contentDescription = addDesc,
                onClick = {
                    editing = null
                    showCreateSheet = true
                }
            )
        )
        if (items.isNotEmpty()) {
            actions.add(
                ToolbarAction(
                    icon = if (toggleEditMode) Icons.Default.EditOff else Icons.Default.Edit,
                    contentDescription = editDesc,
                    onClick = { toggleEditMode = !toggleEditMode }
                )
            )
        }
        setToolbar(
            ToolbarConfig(
                title = passwordsTitle,
                showBack = true,
                actions = if (authenticated) actions else emptyList()
            )
        )
    }

    if (!authenticated) {
        LockedState(
            authType = authType,
            onRetry = {
                val activity = context.findFragmentActivity() ?: return@LockedState
                BiometricHelper.authenticate(
                    activity = activity,
                    title = context.getString(R.string.biometric_reveal_title),
                    subtitle = context.getString(R.string.biometric_reveal_subtitle),
                    authType = authType,
                    onSuccess = { authenticated = true },
                    onError = { msg ->
                        val text = if (msg.isBlank()) context.getString(R.string.biometric_auth_failed) else msg
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
        return
    }

    if (items.isEmpty()) {
        EmptyState(
            modifier = Modifier.padding(top = 16.dp),
            icon = Icons.Default.Lock,
            title = stringResource(R.string.empty_passwords),
            hint = stringResource(R.string.empty_passwords_hint),
            actionLabel = stringResource(R.string.add),
            onAction = {
                editing = null
                showCreateSheet = true
            }
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                PasswordItemRectangle(
                    item = item,
                    revealedPassword = revealedPasswords[item.id],
                    editMode = toggleEditMode,
                    onUsernameCopy = { copyToClipboard(context, item.username, context.getString(R.string.username_copied)) },
                    onToggleReveal = { reveal ->
                        if (reveal) requestReveal(context, authType, viewModel, item, revealedPasswords)
                        else revealedPasswords.remove(item.id)
                    },
                    onPasswordCopy = {
                        val revealed = revealedPasswords[item.id]
                        if (revealed != null) {
                            copyToClipboard(context, revealed, context.getString(R.string.password_copied))
                        } else {
                            requestReveal(context, authType, viewModel, item, revealedPasswords) { decrypted ->
                                copyToClipboard(context, decrypted, context.getString(R.string.password_copied))
                            }
                        }
                    },
                    onEdit = {
                        editing = item
                        showCreateSheet = true
                    },
                    onDelete = { removing = item }
                )
            }
        }
    }

    if (showCreateSheet) {
        CreatePasswordItemSheet(
            editing = editing,
            onDismiss = {
                showCreateSheet = false
                editing = null
            },
            onSave = { input ->
                viewModel.save(input)
                showCreateSheet = false
                editing = null
            }
        )
    }

    removing?.let { item ->
        ActionDialog(
            onDismiss = { removing = null },
            onPositiveClicked = {
                viewModel.remove(item)
                revealedPasswords.remove(item.id)
                removing = null
            }
        )
    }
}

private fun requestReveal(
    context: android.content.Context,
    authType: AuthType,
    viewModel: PasswordItemViewModel,
    item: PasswordItemEntity,
    revealedPasswords: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, String>,
    onDecrypted: ((String) -> Unit)? = null
) {
    val plain = viewModel.decryptPassword(item.encryptedPassword)
    if (authType == AuthType.NONE) {
        // No lock screen set on this device — reveal directly without authentication
        revealedPasswords[item.id] = plain
        onDecrypted?.invoke(plain)
        return
    }
    val activity = context.findFragmentActivity() ?: run {
        revealedPasswords[item.id] = plain
        onDecrypted?.invoke(plain)
        return
    }
    BiometricHelper.authenticate(
        activity = activity,
        title = context.getString(R.string.biometric_reveal_title),
        subtitle = context.getString(R.string.biometric_reveal_subtitle),
        authType = authType,
        onSuccess = {
            revealedPasswords[item.id] = plain
            onDecrypted?.invoke(plain)
        },
        onError = { msg ->
            val text = if (msg.isBlank()) context.getString(R.string.biometric_auth_failed) else msg
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    )
}

private fun android.content.Context.findFragmentActivity(): FragmentActivity? =
    when (this) {
        is FragmentActivity -> this
        is android.content.ContextWrapper -> this.baseContext.findFragmentActivity()
        else -> null
    }

private fun copyToClipboard(context: android.content.Context, value: String, toast: String) {
    if (value.isBlank()) return
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("text", value))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

@Composable
private fun LockedState(authType: AuthType, onRetry: () -> Unit) {
    val isBiometric = authType == AuthType.BIOMETRIC
    val icon = if (isBiometric) Icons.Default.Fingerprint else Icons.Default.Lock
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = stringResource(R.string.biometric_reveal_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = vazirFontFamily
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = stringResource(
                if (isBiometric) R.string.biometric_reveal_subtitle
                else R.string.device_credential_reveal_subtitle
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            fontFamily = vazirFontFamily
        )
        Spacer(modifier = Modifier.size(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.authenticate),
                fontFamily = vazirFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PasswordItemRectangle(
    item: PasswordItemEntity,
    revealedPassword: String?,
    editMode: Boolean = false,
    onUsernameCopy: () -> Unit,
    onToggleReveal: (reveal: Boolean) -> Unit,
    onPasswordCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val iconType = PasswordIconType.fromValue(item.iconType)
    val secondary = MaterialTheme.colorScheme.secondary

    val glassBorder = if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.95f)

    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    val textMuted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val isRevealed = revealedPassword != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        if (editMode) 0.dp else 1.5.dp,
                        glassBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(
                                    alpha = 0.25f
                                )
                            )
                            .border(1.dp, glassBorder.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconType.icon,
                            contentDescription = null,
                            tint = secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = vazirFontFamily,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.size(6.dp))

                        // Username row with copy icon
                        FieldRow(
                            label = stringResource(R.string.password_username_label),
                            value = item.username,
                            copyDesc = stringResource(R.string.copy),
                            onCopy = onUsernameCopy,
                            trailing = null,
                            textMuted = textMuted,
                            textSecondary = textSecondary
                        )

                        Spacer(modifier = Modifier.size(6.dp))

                        // Password row with visibility toggle + copy icon
                        FieldRow(
                            label = stringResource(R.string.password_field_label),
                            value = if (isRevealed) revealedPassword ?: "" else MASK,
                            copyDesc = stringResource(R.string.copy),
                            onCopy = onPasswordCopy,
                            trailing = {
                                IconButton(onClick = { onToggleReveal(!isRevealed) }) {
                                    Icon(
                                        imageVector = if (isRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.password_toggle_visibility),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            textMuted = textMuted,
                            textSecondary = textSecondary
                        )
                    }
                }
            }

            if (editMode) {
                val editDesc = stringResource(R.string.edit)
                val deleteDesc = stringResource(R.string.delete)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = editDesc,
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = editDesc,
                                color = textSecondary,
                                fontSize = 13.sp,
                                fontFamily = vazirFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = deleteDesc,
                                tint = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = deleteDesc,
                                color = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                                fontSize = 13.sp,
                                fontFamily = vazirFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    copyDesc: String,
    onCopy: () -> Unit,
    trailing: @Composable (() -> Unit)?,
    textMuted: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textMuted,
            fontSize = 11.sp,
            fontFamily = vazirFontFamily
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = value,
            color = textSecondary,
            fontSize = 13.sp,
            fontFamily = vazirFontFamily,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = copyDesc,
                tint = textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePasswordItemSheet(
    editing: PasswordItemEntity?,
    onDismiss: () -> Unit,
    onSave: (PasswordItemInput) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editing != null

    val sheetTitle = stringResource(if (isEdit) R.string.edit_password_title else R.string.create_password_title)

    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var iconType by remember { mutableStateOf(PasswordIconType.DEFAULT.value) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(editing?.id) {
        val current = editing
        if (current == null) {
            title = ""
            username = ""
            password = ""
            iconType = PasswordIconType.DEFAULT.value
            passwordVisible = false
        } else {
            title = current.title
            username = current.username
            iconType = current.iconType
            // do not pre-load plaintext password for security; user retypes or toggles show
            password = ""
            passwordVisible = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = sheetTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
            Spacer(modifier = Modifier.size(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.password_title_label)) },
                placeholder = { Text(stringResource(R.string.password_title_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.password_username_label)) },
                placeholder = { Text(stringResource(R.string.password_username_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_field_label)) },
                placeholder = { Text(stringResource(R.string.password_field_hint)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(R.string.password_toggle_visibility),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = stringResource(R.string.password_icon_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                fontFamily = vazirFontFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val iconTypes = PasswordIconType.entries
            val rows = iconTypes.chunked(5)
            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { type ->
                        val isSelected = iconType == type.value
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { iconType = type.value }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    repeat(5 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.size(4.dp))
            }

            Spacer(modifier = Modifier.size(20.dp))

            Button(
                onClick = {
                    val finalTitle = title.trim()
                    if (finalTitle.isBlank()) return@Button
                    val current = editing
                    val keepExisting = current != null && password.isBlank()
                    val finalPassword = if (keepExisting) current?.encryptedPassword.orEmpty() else password
                    if (finalPassword.isBlank()) return@Button
                    val now = System.currentTimeMillis()
                    onSave(
                        PasswordItemInput(
                            id = current?.id ?: 0,
                            title = finalTitle,
                            username = username.trim(),
                            password = finalPassword,
                            iconType = iconType,
                            createdAt = current?.createdAt ?: now,
                            updatedAt = now,
                            alreadyEncrypted = keepExisting
                        )
                    )
                },
                enabled = title.isNotBlank() && (password.isNotBlank() || editing != null),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.confirm),
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )
            }

            Spacer(modifier = Modifier.size(12.dp))
        }
    }
}