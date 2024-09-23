package com.stiagailo.facedetector

internal data class FaceDetectorViewState(
    val cameraType: CameraType,
)

internal enum class CameraType {
    FRONT,
    BACK;
}
