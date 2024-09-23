import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun FaceOverlayView(
    faces: List<Rect>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        for (face in faces) {
            val left = face.left.toFloat()
            val top = face.top.toFloat()
            val width = (face.right - face.left).toFloat()
            val height = (face.bottom - face.top).toFloat()

            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = ComposeSize(width, height),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

