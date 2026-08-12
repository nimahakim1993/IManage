package com.nima.app.imanage.ui.component

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nima.app.imanage.R

@Composable
fun RequiredFieldError(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Text(
            text = stringResource(R.string.required_field_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
        )
    }
}

fun showRequiredFieldsToast(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.required_fields_toast),
        Toast.LENGTH_SHORT
    ).show()
}
