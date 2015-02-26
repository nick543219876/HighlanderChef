package com.highlanderchef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

public class CameraConfigurationManager {
	private static final String TAG = "CameraConfiguration";

	// This is bigger than the size of a small screen, which is still supported. The routine
	// below will still select the default (presumably 320x240) size for these. This prevents
	// accidental selection of very low resolution on some devices.
	private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
	private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
	private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
	private static final double MAX_ASPECT_DISTORTION = 0.15;

	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point theScreenResolution = new Point();
		//TODO try to find a way to getSize for lower API level
		display.getSize(theScreenResolution);
		screenResolution = theScreenResolution;
		cameraResolution = findBestPreviewSizeValue(parameters, screenResolution);
	}

	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			return;
		}


		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		initializeTorch(parameters, prefs, safeMode);

		String focusMode = null;
		/*if (prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true))
		{
			if (safeMode || prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, false)) {
				focusMode = findSettableValue(parameters.getSupportedFocusModes(),
						Camera.Parameters.FOCUS_MODE_AUTO);
			} else {
				focusMode = findSettableValue(parameters.getSupportedFocusModes(),
						Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
						Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
						Camera.Parameters.FOCUS_MODE_AUTO);
			}
		}*/
		// Maybe selected auto-focus but not available, so fall through here:
		if (!safeMode && focusMode == null) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
					Camera.Parameters.FOCUS_MODE_MACRO,
					Camera.Parameters.FOCUS_MODE_EDOF);
		}
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}

		/*if (prefs.getBoolean(PreferencesActivity.KEY_INVERT_SCAN, false)) {
				String colorMode = findSettableValue(parameters.getSupportedColorEffects(),
						Camera.Parameters.EFFECT_NEGATIVE);
				if (colorMode != null) {
					parameters.setColorEffect(colorMode);
				}
			}*/

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize!= null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}
	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}

	boolean getTorchState(Camera camera) {
		if (camera != null) {
			Camera.Parameters parameters = camera.getParameters();
			if (parameters != null) {
				String flashMode = camera.getParameters().getFlashMode();
				return flashMode != null &&
						(Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
								Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
			}
		}
		return false;
	}

	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();
		doSetTorch(parameters, newSetting, false);
		camera.setParameters(parameters);
	}

	private void initializeTorch(Camera.Parameters parameters, SharedPreferences prefs, boolean safeMode) {
		boolean currentSetting =  true;
		doSetTorch(parameters, currentSetting, safeMode);
	}

	private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
		String flashMode;
		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_TORCH,
					Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}


		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.getBoolean("preferences_disable_exposure", false)) {
			if (!safeMode) {
				int minExposure = parameters.getMinExposureCompensation();
				int maxExposure = parameters.getMaxExposureCompensation();
				if (minExposure != 0 || maxExposure != 0) {
					float step = parameters.getExposureCompensationStep();
					int desiredCompensation;
					if (newSetting) {
						// Light on; set low exposue compensation
						desiredCompensation = Math.max((int) (MIN_EXPOSURE_COMPENSATION / step), minExposure);
					} else {
						// Light off; set high compensation
						desiredCompensation = Math.min((int) (MAX_EXPOSURE_COMPENSATION / step), maxExposure);
					}
					parameters.setExposureCompensation(desiredCompensation);
				} else {
				}
			}
		}

	}

	private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {

		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			Camera.Size defaultSize = parameters.getPreviewSize();
			return new Point(defaultSize.width, defaultSize.height);
		}

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});


		double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;

		// Remove sizes that are unsuitable
		Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
		while (it.hasNext()) {
			Camera.Size supportedPreviewSize = it.next();
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
				it.remove();
				continue;
			}

			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
			double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
			double distortion = Math.abs(aspectRatio - screenAspectRatio);
			if (distortion > MAX_ASPECT_DISTORTION) {
				it.remove();
				continue;
			}

			if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
				Point exactPoint = new Point(realWidth, realHeight);

				return exactPoint;
			}
		}

		// If no exact match, use largest preview size. This was not a great idea on older devices because
		// of the additional computation needed. We're likely to get here on newer Android 4+ devices, where
		// the CPU is much more powerful.
		if (!supportedPreviewSizes.isEmpty()) {
			Camera.Size largestPreview = supportedPreviewSizes.get(0);
			Point largestSize = new Point(largestPreview.width, largestPreview.height);

			return largestSize;
		}

		// If there is nothing at all suitable, return current preview size
		Camera.Size defaultPreview = parameters.getPreviewSize();
		Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);

		return defaultSize;
	}

	private static String findSettableValue(Collection<String> supportedValues,
			String... desiredValues) {

		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}

		return result;
	}
}
