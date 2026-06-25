package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig

@Composable
fun HomeScreen(setToolbar: (ToolbarConfig) -> Unit, navController: NavHostController) {

    val settingsDesc = stringResource(R.string.settings)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = "", actions = listOf(
                ToolbarAction(
                    icon = Icons.Outlined.Settings,
                    contentDescription = settingsDesc,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            ))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.financial_title),
                icon = Icons.Outlined.Money,
                onClick = {
                    navController.navigate(Screen.Financial.route)
                }
            )
            DashboardItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.shared_trip),
                icon = Icons.Outlined.MonetizationOn,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.note),
                icon = Icons.AutoMirrored.Filled.Notes,
                onClick = {}
            )
            DashboardItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.assets),
                icon = Icons.Default.Inventory2,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.bank_account_home),
                icon = Icons.Default.CreditCard,
                onClick = { navController.navigate(Screen.BankCards.route) }
            )
            DashboardItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.report),
                icon = Icons.Default.Report,
                onClick = {}
            )
        }
    }

}

@Composable
private fun DashboardItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
