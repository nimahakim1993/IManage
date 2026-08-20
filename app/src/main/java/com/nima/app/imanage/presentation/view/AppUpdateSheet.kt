package com.nima.app.imanage.presentation.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.AppUpdateConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateSheet(config: AppUpdateConfig, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val stores = config.store_urls.filter { it.enable && it.url.isNotBlank() }
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                stringResource(R.string.app_update_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(config.message, style = MaterialTheme.typography.bodyLarge)
            stores.forEach { store ->
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(store.url)))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    if (store.logo.isNotBlank()) {
                        AsyncImage(
                            model = store.logo,
                            imageLoader = imageLoader,
                            contentDescription = store.title,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.app_update_download_from,
                            store.title.ifBlank { stringResource(R.string.app_update_store) }
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            if (!config.force_update) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.app_update_later)) }
            }
        }
    }
}
