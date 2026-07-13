package com.nima.app.imanage.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.SecurityManager

@Composable
fun HelpScreen(
    setToolbar: (ToolbarConfig) -> Unit
) {
    val helpTitle = stringResource(R.string.help_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = helpTitle, showBack = true)
        )
    }

    val modules = listOf(
        HelpModule(
            titleRes = R.string.financial_title,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF4CAF50),
            shortDescRes = R.string.help_financial_short,
            longDescRes = R.string.help_financial_long
        ),
        HelpModule(
            titleRes = R.string.bank_account_home,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF2196F3),
            shortDescRes = R.string.help_bank_cards_short,
            longDescRes = R.string.help_bank_cards_long
        ),
        HelpModule(
            titleRes = R.string.shared_trip,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF9C27B0),
            shortDescRes = R.string.help_trips_short,
            longDescRes = R.string.help_trips_long
        ),
        HelpModule(
            titleRes = R.string.note,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFFFF9800),
            shortDescRes = R.string.help_notes_short,
            longDescRes = R.string.help_notes_long
        ),
        HelpModule(
            titleRes = R.string.assets,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF009688),
            shortDescRes = R.string.help_assets_short,
            longDescRes = R.string.help_assets_long
        ),
        HelpModule(
            titleRes = R.string.passwords_home,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFFE91E63),
            shortDescRes = R.string.help_passwords_short,
            longDescRes = R.string.help_passwords_long
        ),
        HelpModule(
            titleRes = R.string.car_services,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF795548),
            shortDescRes = R.string.help_car_short,
            longDescRes = R.string.help_car_long
        ),
        HelpModule(
            titleRes = R.string.report,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF3F51B5),
            shortDescRes = R.string.help_report_short,
            longDescRes = R.string.help_report_long
        ),
        HelpModule(
            titleRes = R.string.office_title,
            icon = Icons.Default.KeyboardArrowDown,
            iconColor = Color(0xFF607D8B),
            shortDescRes = R.string.help_office_short,
            longDescRes = R.string.help_office_long
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        modules.forEach { module ->
            HelpCard(module = module)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class HelpModule(
    val titleRes: Int,
    val icon: ImageVector,
    val iconColor: Color,
    val shortDescRes: Int,
    val longDescRes: Int
)

@Composable
private fun HelpCard(module: HelpModule) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            module.iconColor.copy(alpha = 0.08f),
            module.iconColor.copy(alpha = 0.02f)
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(module.iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = SecurityManager.modules.find {
                        it.titleRes == module.titleRes
                    }?.icon ?: module.icon
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = module.iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(module.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(module.shortDescRes),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = arrowRotation }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(module.iconColor.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(module.longDescRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
