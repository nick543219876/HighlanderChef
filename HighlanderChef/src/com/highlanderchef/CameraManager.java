package com.highlanderchef;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;

public class CameraManager
{
	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private AutoFocusManager autoFocusManager;
	private boolean initialized;
	private boolean previewing;
	Camera theCamera = camera;
	private final PreviewCallback previewCallback;

	public CameraManager(Context context, int height, int width)
	{
		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
		previewCallback = new PreviewCallback(configManager, height, width);
	}

	public synchronized void openDriver(SurfaceHolder holder) throws IOException
	{

		if (theCamera == null)
		{
			theCamera = OpenCameraInterface.open();
			if (theCamera == null)
			{
				throw new IOException();
			}
			camera = theCamera;
		}
		theCamera.setPreviewDisplay(holder);
		theCamera.setPreviewCallback(previewCallback);

		if (!initialized)
		{
			initialized = true;
			configManager.initFromCameraParameters(theCamera);
		}

		Camera.Parameters parameters = theCamera.getParameters();
		String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily
		try
		{
			configManager.setDesiredCameraParameters(theCamera, false);
		}
		catch (RuntimeException re)
		{
			if (parametersFlattened != null)
			{
				parameters = theCamera.getParameters();
				parameters.unflatten(parametersFlattened);
				try
				{
					theCamera.setParameters(parameters);
					configManager.setDesiredCameraParameters(theCamera, true);
				}
				catch (RuntimeException re2)
				{
					// Well, darn. Give up
				}
			}
		}
	}


	public synchronized boolean isOpen()
	{
		return camera != null;
	}

	public synchronized void closeDriver()
	{
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

	public synchronized void startPreview() {
		Camera theCamera = camera;
		if (theCamera != null && !previewing) {
			theCamera.startPreview();
			previewing = true;
			autoFocusManager = new AutoFocusManager(context, camera);
		}
	}

	public synchronized void stopPreview()
	{
		if (autoFocusManager != null) {
			autoFocusManager.stop();
			autoFocusManager = null;
		}
		if (camera != null && previewing) {
			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	public synchronized void setTorch(boolean newSetting) {
		if (newSetting != configManager.getTorchState(camera)) {
			if (camera != null) {
				if (autoFocusManager != null) {
					autoFocusManager.stop();
				}
				configManager.setTorch(camera, newSetting);
				if (autoFocusManager != null) {
					autoFocusManager.start();
				}
			}
		}
	}

	public synchronized void requestPreviewFrame(Handler handler, int message) {
		Camera theCamera = camera;
		if (theCamera != null && previewing) {
			previewCallback.setHandler(handler, message);
			theCamera.setOneShotPreviewCallback(previewCallback);
		}
	}

}
