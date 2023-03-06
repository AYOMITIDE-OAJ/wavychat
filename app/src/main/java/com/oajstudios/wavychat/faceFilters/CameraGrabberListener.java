package com.oajstudios.wavychat.faceFilters;

@SuppressWarnings("ALL")
public interface CameraGrabberListener {
    void onCameraInitialized();
    void onCameraError(String errorMsg);
}
