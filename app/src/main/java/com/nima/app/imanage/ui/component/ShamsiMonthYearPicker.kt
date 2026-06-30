package com.nima.app.imanage.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nima.app.imanage.R
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ShamsiDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShamsiMonthYearPicker(
    initialMonth: Int?,
    initialYear: Int?,
    onConfirm: (month: Int, year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val today = ShamsiDate.today()
    val currentYear = today.first

    var selectedMonth by remember { mutableIntStateOf(initialMonth ?: today.second) }
    var selectedYear by remember { mutableIntStateOf(initialYear ?: currentYear) }
    var displayYear by remember { mutableIntStateOf(selectedYear) }

    val monthName = ShamsiDate.getMonthName(selectedMonth)
    val yearText = ShamsiDate.toPersianDigits(selectedYear.toString())
    val selectedLabel = "$monthName $yearText"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.select_month_year),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = vazirFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (displayYear > currentYear - 80) displayYear--
                    }) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = ShamsiDate.toPersianDigits(displayYear.toString()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = vazirFontFamily,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = {
                        if (displayYear < currentYear + 5) displayYear++
                    }) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ShamsiDate.MONTH_NAMES.forEachIndexed { index, name ->
                        val month = index + 1
                        val isSelected = month == selectedMonth && displayYear == selectedYear
                        val isCurrent = month == today.second && displayYear == currentYear
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    }
                                )
                                .border(
                                    width = if (isCurrent && !isSelected) 1.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedMonth = month
                                    selectedYear = displayYear
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = name,
                                fontFamily = vazirFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedMonth, selectedYear)
            }) {
                Text(
                    text = stringResource(R.string.confirm),
                    fontFamily = vazirFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontFamily = vazirFontFamily
                )
            }
        }
    )
}
