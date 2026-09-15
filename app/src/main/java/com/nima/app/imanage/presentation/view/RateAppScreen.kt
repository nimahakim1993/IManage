package com.nima.app.imanage.presentation.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nima.app.imanage.BuildConfig
import com.nima.app.imanage.R
import com.nima.app.imanage.data.model.ToolbarConfig
import com.nima.app.imanage.ui.theme.vazirFontFamily

@Composable
fun RateAppScreen(
    setToolbar: (ToolbarConfig) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val title = stringResource(R.string.rate_app_title)

    LaunchedEffect(Unit) {
        setToolbar(
            ToolbarConfig(
                title = title,
                showBack = true
            )
        )
    }

    val marketUrl = when (BuildConfig.FLAVOR) {
        "cafebazaar" -> "https://cafebazaar.ir/app/com.nima.app.imanage"
        "myket" -> "https://myket.ir/app/com.nima.app.imanage"
        else -> "https://cafebazaar.ir/app/com.nima.app.imanage"
    }

    val buttonText = when (BuildConfig.FLAVOR) {
        "cafebazaar" -> stringResource(R.string.rate_app_button_cafebazaar)
        "myket" -> stringResource(R.string.rate_app_button_myket)
        else -> stringResource(R.string.rate_app_button)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.imanage_logo),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.rate_app_description),
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = vazirFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                when (BuildConfig.FLAVOR) {
                    "cafebazaar" -> {
                        try {
                            val intent = Intent(
                                Intent.ACTION_EDIT,
                                Uri.parse("bazaar://details?id=${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(marketUrl)
                            )
                            context.startActivity(intent)
                        }
                    }

                    "myket" -> {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("myket://comment?id=${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(marketUrl)
                            )
                            context.startActivity(intent)
                        }
                    }

                    else -> {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(marketUrl)
                        )
                        context.startActivity(intent)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = vazirFontFamily
            )
        }
    }
}
