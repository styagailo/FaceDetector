package com.stiagailo.facedetector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FaceDetectorViewModel : ViewModel() {

    private val _state = MutableSharedFlow<FaceDetectorViewState>(replay = 1)

    val state: StateFlow<FaceDetectorViewState> = _state
        .debounce(DEBOUNCE_TIMEOUT)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT),
            FaceDetectorViewState(cameraType = CameraType.FRONT)
        )

    fun switchCamera() {
        viewModelScope.launch {
            val cameraType = when(state.value.cameraType) {
                CameraType.FRONT -> CameraType.BACK
                CameraType.BACK -> CameraType.FRONT
            }
            _state.emit(state.value.copy(cameraType = cameraType))
        }
    }

    companion object {
        private const val STOP_TIMEOUT = 5000L
        private const val DEBOUNCE_TIMEOUT = 300L
    }
}