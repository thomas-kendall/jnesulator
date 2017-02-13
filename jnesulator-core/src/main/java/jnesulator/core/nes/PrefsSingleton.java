package jnesulator.core.nes;

import java.util.prefs.Preferences;

// TODO: Move this out of the core application. Settings should be passed in to the classes.
public class PrefsSingleton {

	private static Preferences instance = null;

	public synchronized static Preferences get() {
		if (instance == null) {
			instance = Preferences.userNodeForPackage(jnesulator.core.nes.NES.class);
		}
		return instance;
	}

	protected PrefsSingleton() {
		// Exists only to defeat instantiation.
	}
}
