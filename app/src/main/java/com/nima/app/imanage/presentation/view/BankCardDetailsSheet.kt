package com.nima.app.imanage.presentation.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nima.app.imanage.R
import com.nima.app.imanage.data.db.entity.BankCardEntity
import com.nima.app.imanage.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankCardDetailsSheet(
    card: BankCardEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.copied_to_clipboard)
    val notSetLabel = stringResource(R.string.not_set)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {

            Text(
                text = card.bankName.ifBlank { stringResource(R.string.bank_name_placeholder) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.size(20.dp))

            DetailRow(
                icon = Icons.Default.CreditCard,
                label = stringResource(R.string.card_number),
                value = card.cardNumber
                    .takeIf { it.isNotBlank() }
                    ?.let { NumberFormatUtils.toLocalizedDigits(it) }
                    ?: notSetLabel,
                onCopy = {
                    if (card.cardNumber.isNotBlank()) copyToClipboard(context, "Card Number", card.cardNumber, copiedMsg)
                }
            )

            Spacer(modifier = Modifier.size(12.dp))

            DetailRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.account_number),
                value = card.accountNumber
                    ?.takeIf { it.isNotBlank() }
                    ?.let { NumberFormatUtils.toLocalizedDigits(it) }
                    ?: notSetLabel,
                onCopy = {
                    card.accountNumber?.takeIf { it.isNotBlank() }?.let {
                        copyToClipboard(context, "Account Number", it, copiedMsg)
                    }
                }
            )

            Spacer(modifier = Modifier.size(12.dp))

            DetailRow(
                icon = Icons.Default.AccountBalance,
                label = stringResource(R.string.sheba_number),
                value = card.shebaNumber
                    ?.takeIf { it.isNotBlank() }
                    ?.let { NumberFormatUtils.toLocalizedDigits(it) }
                    ?: notSetLabel,
                onCopy = {
                    card.shebaNumber?.takeIf { it.isNotBlank() }?.let {
                        copyToClipboard(context, "Sheba Number", it, copiedMsg)
                    }
                }
            )

            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.copy),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun copyToClipboard(
    context: Context,
    label: String,
    value: String,
    toastMessage: String
) {
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
