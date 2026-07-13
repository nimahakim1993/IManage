package com.nima.app.imanage.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nima.app.imanage.R
import com.nima.app.imanage.ui.theme.LocalIsDarkTheme
import java.util.Locale

@Composable
fun ActionDialog(
    onDismiss: () -> Unit,
    onPositiveClicked: () -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    val isRtlLocale = Locale.getDefault().language == "fa"

    val deleteIconColor = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828)
    val deleteButtonColor = if (isDark) Color(0xFFEF5350) else Color(0xFFC62828)

    val dialogShape = RoundedCornerShape(20.dp)
    val buttonShape = RoundedCornerShape(12.dp)

    val textColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtlLocale) LayoutDirection.Rtl else LayoutDirection.Ltr
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = dialogShape,
                color = surfaceColor,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = deleteIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.delete_title),
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.delete_confirmation),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        Button(
                            onClick = { onPositiveClicked() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = deleteButtonColor,
                                contentColor = Color.White,
                            ),
                            shape = buttonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(text = stringResource(R.string.yes), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor,
                            ),
                            shape = buttonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            border = BorderStroke(
                                1.5.dp,
                                if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(
                                    alpha = 0.2f
                                )
                            )
                        ) {
                            Text(text = stringResource(R.string.no), fontWeight = FontWeight.SemiBold)
                        }
                    }

                }
            }
        }
    }
}