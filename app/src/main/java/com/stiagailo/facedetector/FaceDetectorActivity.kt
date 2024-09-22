package com.stiagailo.facedetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.stiagailo.facedetector.ui.theme.FaceDetectorTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceDetectorActivity : ComponentActivity() {
    private var cameraExecutor: ExecutorService? = null
    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setContent {
                FaceDetectorTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        CameraView()
                    }
                }
            }
        } else {
            // Handle permission denial (show message or close app)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        } else {
            setContent {
                FaceDetectorTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        CameraView()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}

@Composable
fun CameraView() {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Select back camera as default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind any existing use cases before binding again
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        ctx as ComponentActivity, cameraSelector, preview
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
