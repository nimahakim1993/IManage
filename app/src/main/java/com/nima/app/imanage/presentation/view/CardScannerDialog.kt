package com.nima.app.imanage.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nima.app.imanage.R
import com.nima.app.imanage.util.normalizeDigits
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class CardScanResult(
    val cardNumber: String
)

@Composable
fun CardScannerDialog(
    onDismiss: () -> Unit,
    onResult: (CardScanResult) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            CardScannerContent(onDismiss = onDismiss, onResult = onResult)
        }
    }
}

@Composable
private fun CardScannerContent(
    onDismiss: () -> Unit,
    onResult: (CardScanResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPermission = it }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                onResult = onResult
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.scan_card_permission),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text(stringResource(R.string.scan_card))
                }
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.cancel),
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 210.dp, width = 320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
            )
            Text(
                text = stringResource(R.string.scan_card_hint),
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.scan_card_detecting),
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CameraPreview(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onResult: (CardScanResult) -> Unit
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val scanCompleted = remember { AtomicBoolean(false) }
    val currentOnResult by rememberUpdatedState(onResult)

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        var analysis: ImageAnalysis? = null

        val listener = Runnable {
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { imageAnalysis ->
                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        analyzeImage(imageProxy, recognizer, scanCompleted, currentOnResult)
                    }
                }
            analysis = imageAnalysis
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }

        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            analysis?.clearAnalyzer()
            cameraProvider?.unbindAll()
            executor.shutdown()
            recognizer.close()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun analyzeImage(
    imageProxy: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    scanCompleted: AtomicBoolean,
    onResult: (CardScanResult) -> Unit
) {
    if (scanCompleted.get()) {
        imageProxy.close()
        return
    }
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    recognizer.process(image)
        .addOnSuccessListener { result ->
            parseCardText(result.text)?.let { scanResult ->
                if (scanCompleted.compareAndSet(false, true)) onResult(scanResult)
            }
        }
        .addOnCompleteListener { imageProxy.close() }
}

private fun parseCardText(text: String): CardScanResult? {
    val normalized = text.normalizeDigits()
    val cardNumber = Regex("(?<!\\d)(?:\\d[\\s-]?){15,18}\\d(?!\\d)")
        .find(normalized)
        ?.value
        ?.filter(Char::isDigit)
        ?.takeIf { it.length in 16..19 }
    return cardNumber?.let { CardScanResult(cardNumber = it) }
}
