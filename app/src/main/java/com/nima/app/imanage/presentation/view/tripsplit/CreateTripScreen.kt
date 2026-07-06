package com.nima.app.imanage.presentation.view.tripsplit

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarAction
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.presentation.viewmodel.TripListViewModel
import com.nima.app.imanage.ui.component.ShamsiDatePicker
import com.nima.app.imanage.ui.component.TextInputDropDown
import com.nima.app.imanage.ui.theme.vazirFontFamily
import com.nima.app.imanage.util.ShamsiDate
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController,
    editTripId: Int?,
    viewModel: TripListViewModel = koinViewModel()
) {
    val isEdit = editTripId != null
    val editingTrip by viewModel.editingTrip.collectAsState()
    val existingParticipants by viewModel.tripParticipants.collectAsState()
    val existingHostId by viewModel.hostId.collectAsState()

    var tripName by remember { mutableStateOf("") }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var participantNames by remember { mutableStateOf(listOf("")) }
    var hostIndex by remember { mutableStateOf<Int?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(editTripId) {
        if (editTripId != null) {
            viewModel.loadTripForEdit(editTripId)
        } else {
            viewModel.resetEditState()
        }
    }

    LaunchedEffect(editingTrip) {
        editingTrip?.let { trip ->
            tripName = trip.name
            startDate = trip.startDate
            endDate = trip.endDate
        }
    }

    LaunchedEffect(existingParticipants) {
        if (isEdit && existingParticipants.isNotEmpty() && participantNames.size <= 1 && participantNames.first()
                .isEmpty()
        ) {
            participantNames = existingParticipants.map { it.name }
            hostIndex = existingParticipants.indexOfFirst { it.id == existingHostId }
                .let { if (it >= 0) it else null }
        }
    }

    val toolbarTitle =
        stringResource(if (isEdit) R.string.trip_edit_title else R.string.trip_create_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = toolbarTitle,
                showBack = true,
                actions = listOf(
                    ToolbarAction(
                        icon = Icons.Default.Check,
                        contentDescription = "Save",
                        onClick = {
                            val validNames = participantNames.filter { it.isNotBlank() }
                            if (tripName.isBlank() || validNames.isEmpty()) return@ToolbarAction
                            if (isEdit && editTripId != null) {
                                viewModel.updateTrip(
                                    tripId = editTripId,
                                    name = tripName,
                                    startDate = startDate,
                                    endDate = endDate,
                                    participantNames = validNames,
                                    hostIndex = hostIndex
                                )
                            } else {
                                viewModel.createTrip(
                                    name = tripName,
                                    startDate = startDate,
                                    endDate = endDate,
                                    participantNames = validNames,
                                    hostIndex = hostIndex
                                )
                            }
                            navController.popBackStack()
                        }
                    )
                )
            )
        )
    }

    if (showStartDatePicker) {
        ShamsiDatePicker(
            initialDate = startDate,
            onConfirm = { newDate ->
                startDate = newDate
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text(stringResource(R.string.trip_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartDatePicker = true }) {
                OutlinedTextField(
                    value = ShamsiDate.format(startDate),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.trip_start_date_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text(
                text = stringResource(R.string.trip_participants_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )

            participantNames.forEachIndexed { index, name ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newName ->
                            participantNames =
                                participantNames.toMutableList().also { it[index] = newName }
                        },
                        label = {
                            Text(
                                stringResource(
                                    R.string.trip_person_name_label,
                                    index + 1
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (participantNames.size > 1) {
                        IconButton(onClick = {
                            participantNames =
                                participantNames.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { participantNames = participantNames + "" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.trip_add_participant))
            }

            val nonEmptyNames = participantNames.filter { it.isNotBlank() }
            if (nonEmptyNames.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.trip_host_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = vazirFontFamily
                )

                TextInputDropDown(
                    label = stringResource(R.string.trip_select_host),
                    items = nonEmptyNames,
                    selectedItem = if (hostIndex != null && hostIndex!! < nonEmptyNames.size) nonEmptyNames[hostIndex!!] else "",
                    onItemSelected = { index, _ -> hostIndex = index }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
