package com.stiagailo.facedetector

import CameraView
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stiagailo.facedetector.ui.theme.FaceDetectorTheme

internal class FaceDetectorActivity : ComponentActivity() {

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            showToast(PERMISSION_NOT_GRANTED)
            finish()
        }
    }

    private val viewModel by viewModels<FaceDetectorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (permissionsGranted()) {
            startCamera()
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        setContent {
            FaceDetectorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    CameraView(state.cameraType, viewModel::switchCamera)
                }
            }
        }
    }

    private fun permissionsGranted() = ContextCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        private const val PERMISSION_NOT_GRANTED = "Permission is not granted"
    }
}

