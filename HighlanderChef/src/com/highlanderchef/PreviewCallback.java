package com.highlanderchef;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;

final public class PreviewCallback implements Camera.PreviewCallback
{

	private final CameraConfigurationManager configManager;
	private Handler previewHandler;
	private int previewMessage;

	int height;
	int width;

	boolean halfassmutex;


	PreviewCallback(CameraConfigurationManager configManager, int height, int width) {
		this.configManager = configManager;
		this.height = height;
		this.width = width;
		halfassmutex = false;
	}

	void setHandler(Handler previewHandler, int previewMessage) {
		this.previewHandler = previewHandler;
		this.previewMessage = previewMessage;

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Point cameraResolution = configManager.getCameraResolution();
		Handler thePreviewHandler = previewHandler;
		previewHandler = null;
		if(!halfassmutex)
		{
			halfassmutex = true;
			/*int nrOfPixels = data.length / 3; // Three bytes per pixel.
			int pixels[] = new int[nrOfPixels];
			for(int i = 0; i < nrOfPixels; i++)
			{
				int r = data[3*i];
				int g = data[3*i + 1];
				int b = data[3*i + 2];
				pixels[i] = Color.rgb(r,g,b);
			}*/


			Bitmap image_captured = BitmapFactory.decodeByteArray(data, 0, data.length);
			//Bitmap image_captured = Bitmap.createBitmap(pixels,  cameraResolution.x, cameraResolution.y, Bitmap.Config.ARGB_8888);
			new CompareAnImage().execute(image_captured);
		}

	}

	class CompareAnImage extends AsyncTask<Bitmap, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Bitmap... params) {

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				onSuccess();
			} else {
				onFailure();
			}
		}
	}

	public void onSuccess()
	{
		halfassmutex = false;
	}

	public void onFailure()
	{
		halfassmutex = false;
	}
}



