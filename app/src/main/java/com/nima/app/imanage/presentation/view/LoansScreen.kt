package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nima.app.imanage.Screen
import com.nima.app.imanage.data.db.entity.LoanEntity
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.LoanViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoansScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavController,
    viewModel: LoanViewModel = koinViewModel()
) {

    val loans by viewModel.loans.collectAsState()

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(title = "بدهی بستانکاری", showBack = true, actions = listOf(
                ToolbarAction(
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
                    onClick = {
                        navController.navigate(Screen.CreateLoan.route)
                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.FilterAlt,
                    contentDescription = "Filter",
                    onClick = {

                    }
                ),
                ToolbarAction(
                    icon = Icons.Default.Search,
                    contentDescription = "Search",
                    onClick = {

                    }
                ),
            ))
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(loans, key = { it.id }) { loan ->
                LoanItem(loan)
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
        colors = CardDefaults.cardColors(containerColor = if (loan.type == 0) Color.Red else Color.Blue),
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
                Text(modifier = Modifier.weight(1f), text = loan.price.toString())
                Text(loan.targetPersonName)
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text("توضیحات : " + loan.description)
            Text(loan.date.toString())
        }
    }

}