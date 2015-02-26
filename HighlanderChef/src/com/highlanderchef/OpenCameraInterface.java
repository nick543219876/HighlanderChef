package com.highlanderchef;

import android.hardware.Camera;

public final class OpenCameraInterface {
	public static Camera open() {

		int numCameras = Camera.getNumberOfCameras();
		if (numCameras == 0) {

			return null;
		}

		int index = 0;
		while (index < numCameras) {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(index, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				break;
			}
			index++;
		}

		Camera camera;
		if (index < numCameras) {
			camera = Camera.open(index);
		} else {
			camera = Camera.open(0);
		}

		return camera;
	}

}
