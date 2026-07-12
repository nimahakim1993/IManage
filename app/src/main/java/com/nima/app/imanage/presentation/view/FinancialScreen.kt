package com.nima.app.imanage.presentation.view

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.ui.theme.vazirFontFamily


@Composable
fun FinancialScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController
) {

    val ledgerTitle = stringResource(R.string.financial_ledger)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = ledgerTitle, showBack = true)
        )
    }

    val items = listOf(
        FinancialEntry(
            title = stringResource(R.string.debt) + " " + stringResource(R.string.receivable),
            icon = Icons.Default.Handshake,
            color = Color(0xFFFF9800),
            onClick = { navController.navigate(Screen.Loans.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.expense),
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFFF44336),
            onClick = { navController.navigate(Screen.Expenses.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.income),
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50),
            onClick = { navController.navigate(Screen.Incomes.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.installment),
            icon = Icons.Default.CalendarMonth,
            color = Color(0xFF009688),
            onClick = { navController.navigate(Screen.Installments.route) }
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(4.dp))

        items.forEach { entry ->
            FinancialItem(
                title = entry.title,
                icon = entry.icon,
                color = entry.color,
                onClick = entry.onClick
            )
        }
    }
}

private data class FinancialEntry(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun FinancialItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = vazirFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
