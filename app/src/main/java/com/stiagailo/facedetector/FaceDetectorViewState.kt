package com.stiagailo.facedetector

data class FaceDetectorViewState(
    val cameraType: CameraType,
)

enum class CameraType {
    FRONT,
    BACK;
}
