package com.highlanderchef;

import android.os.Handler;
import android.os.Message;

public class CaptureActivityHandler extends Handler
{
	private final ImageComp activity;

	private State state;
	private final CameraManager cameraManager;

	private enum State {
		PREVIEW,
		SUCCESS,
		DONE
	}

	CaptureActivityHandler(ImageComp activity, CameraManager cameraManager)
	{
		this.activity = activity;
		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		this.cameraManager = cameraManager;
		cameraManager.startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		//TODO figure out what this does

	}

	public void quitSynchronously() {
		state = State.DONE;
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			//TODO get this line working
			//cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
		}
	}
}
