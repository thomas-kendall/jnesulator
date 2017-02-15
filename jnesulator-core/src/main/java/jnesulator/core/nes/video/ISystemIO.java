package jnesulator.core.nes.video;

import java.awt.image.BufferedImage;

public interface ISystemIO {

	void onAudioFrame(int sample);

	// A message from the system, maybe to show in a message box
	void onMessage(String message);

	void onVideoFrame(BufferedImage frame);

	// TODO: Move controller functions here
}
