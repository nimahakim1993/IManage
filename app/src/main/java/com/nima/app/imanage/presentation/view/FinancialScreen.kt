package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.model.ToolbarConfig


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
            icon = Icons.Default.AccountBalance,
            onClick = { navController.navigate(Screen.Loans.route) }
        ),
        FinancialEntry(
            title = stringResource(R.string.expense),
            icon = Icons.Default.Build,
            onClick = {}
        ),
        FinancialEntry(
            title = stringResource(R.string.income),
            icon = Icons.Default.TrendingUp,
            onClick = {}
        ),
        FinancialEntry(
            title = stringResource(R.string.installment),
            icon = Icons.Default.CalendarMonth,
            onClick = {}
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
                onClick = entry.onClick
            )
        }
    }
}

private data class FinancialEntry(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun FinancialItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = title)
        }
    }
}
