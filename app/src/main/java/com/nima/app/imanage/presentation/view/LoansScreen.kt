package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nima.app.imanage.R
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import com.nima.app.imanage.util.NumberFormatUtils
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoansScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    viewModel: LoanViewModel = koinViewModel()
) {

    val loans by viewModel.loans.collectAsState()

    val loansTitle = stringResource(R.string.loans_title)
    val addDesc = stringResource(R.string.add)
    val filterDesc = stringResource(R.string.filter)
    val searchDesc = stringResource(R.string.search)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = loansTitle, showBack = true, actions = listOf(
                ToolbarAction(
                    icon = Icons.Default.Add,
                    contentDescription = addDesc,
                    onClick = {
                        navController.navigate(Screen.CreateLoan.route)
                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.FilterAlt,
                    contentDescription = filterDesc,
                    onClick = {

                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.Search,
                    contentDescription = searchDesc,
                    onClick = {

                    }
                ),
            ))
        )
    }

    val (totalDebt, totalReceivable) = remember(loans) {
        val debt = loans.filter { it.type == LoanEntity.TYPE_DEBT }.sumOf { it.price }
        val receivable = loans.filter { it.type == LoanEntity.TYPE_RECEIVABLE }.sumOf { it.price }
        debt to receivable
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TotalsCard(totalDebt = totalDebt, totalReceivable = totalReceivable)
            }
            items(loans, key = { it.id }) { loan ->
                LoanItem(loan)
            }
        }
    }
}

@Composable
private fun TotalsCard(totalDebt: Long, totalReceivable: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.total_debt),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = NumberFormatUtils.format(totalDebt),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.total_receivable),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = NumberFormatUtils.format(totalReceivable),
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoanItem(loan: LoanEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (loan.type == LoanEntity.TYPE_DEBT) Color.Red else Color.Blue),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(modifier = Modifier.weight(1f), text = NumberFormatUtils.format(loan.price))
                Text(loan.targetPersonName)
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(stringResource(R.string.description_prefix, loan.description))
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            Text(stringResource(R.string.payment_date_prefix, dateFormat.format(Date(loan.dateLoan))))
            Text(stringResource(R.string.receive_date_prefix, dateFormat.format(Date(loan.dateReceiveBack))))
        }
    }

}
