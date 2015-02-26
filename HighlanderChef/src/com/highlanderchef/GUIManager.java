package com.highlanderchef;


public class GUIManager {
	private static GUIManager instance;
	private static volatile String authToken;
	private static volatile String email;

	protected GUIManager() {
		// Exists only to defeat instantiation.
	}

	public static GUIManager getInstance() {
		if (instance == null) {
			return new GUIManager();
		}
		return instance;
	}

	public boolean doLogin(final String email, final String password) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				Comm c = new Comm();
				int ret = c.login(email, password);
				switch (ret) {
				case Comm.SUCCESS:
					GUIManager.getInstance().authToken = c.getAuthToken();
					GUIManager.getInstance().email = c.getEmail();
					break;
				default:
					break;
				}
			}
		}).start();

		// STUB -- need to use a Callable<boolean> instead of Runnable
		return false;
	}
}
