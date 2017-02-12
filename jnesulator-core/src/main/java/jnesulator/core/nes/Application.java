package jnesulator.core.nes;

import java.io.IOException;

import javax.swing.UIManager;

import jnesulator.core.nes.ui.SwingUI;

public class Application {
	public static void main(String[] args) throws IOException {
		JInputHelper.setupJInput();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Could not set system look and feel. Meh.");
		}
		new SwingUI(args);
	}
}
