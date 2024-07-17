package com.example.qr_scanner

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.LineHeightStyle
import com.example.qr_scanner.ui.theme.QR_ScannerTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WorkingQr() {
    Scaffold {
        var code by remember { mutableStateOf("") }
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        var hasCamPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { granted -> hasCamPermission = granted })
        LaunchedEffect(key1 = true) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp, top = 50.dp)) {
            if (hasCamPermission) {
                Text(text = "Scan the QR code", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp).align(
                    Alignment.CenterHorizontally))
                AndroidView(factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(ResolutionSelector.Builder().build())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), QrCodeAnalyser { result ->
                        code = result
                    })
                    try {
                        cameraProviderFuture.get().bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                }, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f))
            }
            if (code.isNotEmpty()) {
                when {
                    code.startsWith("upi://") -> {
                        // Handle UPI payment QR code
                        Text(
                            text = "UPI Payment: $code",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(code))
                                    context.startActivity(intent)
                                }
                        )
                    }
                    code.startsWith("http://") || code.startsWith("https://") -> {
                        // Handle URL QR code
                        Text(
                            text = "Link : $code",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(code))
                                    context.startActivity(intent)
                                }
                        )
                    }
                    else -> {
                        // Handle plain text QR code
                        Text(
                            text = code,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                        )
                    }
                }
            }
        }
    }
}
