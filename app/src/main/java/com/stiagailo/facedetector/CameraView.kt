import android.content.Context
import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.stiagailo.facedetector.CameraType

@Composable
internal fun CameraView(
    cameraType: CameraType,
    onCameraSwitch: () -> Unit,
) {
    val context = LocalContext.current
    val faceBoundingBoxes = remember { mutableStateListOf<Rect>() }
    var cameraController by remember { mutableStateOf<LifecycleCameraController?>(null) }

    LaunchedEffect(cameraType) {
        cameraController?.unbind()

        cameraController = getCameraController(context) { faces ->
            val newFaceBoundingBoxes = faces?.map { it.boundingBox }
            if (newFaceBoundingBoxes != faceBoundingBoxes) {
                faceBoundingBoxes.clear()
                newFaceBoundingBoxes?.let { faceBoundingBoxes += it }
            }
        }.apply {
            cameraSelector = when (cameraType) {
                CameraType.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraType.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            }
        }

        cameraController?.bindToLifecycle(context as ComponentActivity)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                }
            },
            update = { previewView ->
                previewView.controller = cameraController
            }
        )

        FaceOverlayView(faces = faceBoundingBoxes, modifier = Modifier.fillMaxSize())

        FloatingActionButton(
            onClick = onCameraSwitch,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    PaddingValues(
                        vertical = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                        horizontal = 16.dp
                    )
                )
        ) {
            Text("Switch")
        }
    }
}

private fun getCameraController(
    context: Context,
    onAnalysisResult: (List<Face>?) -> Unit
): LifecycleCameraController {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    val faceDetector = FaceDetection.getClient(faceDetectorOptions)
    val analyzer = MlKitAnalyzer(
        listOf(faceDetector),
        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
        mainExecutor,
    ) { result ->
        val faces = result.getValue(faceDetector)
        onAnalysisResult(faces)
    }

    return LifecycleCameraController(context).apply {
        setImageAnalysisAnalyzer(
            mainExecutor,
            analyzer
        )
    }
}
