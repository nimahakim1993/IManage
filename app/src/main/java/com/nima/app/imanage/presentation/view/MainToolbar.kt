package com.nima.app.imanage.presentation.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.ui.theme.getVazirTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    config: ToolbarConfig,
    onBackClick: () -> Unit,
) {

        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    config.actions.forEach { action ->
                        IconButton(onClick = action.onClick) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = config.title,
                        modifier = Modifier.padding(end = 8.dp),
                        style = getVazirTypography(1f).headlineLarge,
                        textAlign = TextAlign.End
                    )
                }
            },
            navigationIcon = {
                if (config.showBack) {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_icon)
                        )
                    }
                }
            },
            actions = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary

            )
        )


}

@Composable
@Preview(showBackground = true)
fun MainToolbarPreview() {
    MainToolbar(ToolbarConfig("", true, arrayListOf()), onBackClick = {})
}