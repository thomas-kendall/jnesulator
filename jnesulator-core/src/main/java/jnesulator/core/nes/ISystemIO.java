package jnesulator.core.nes;

import java.awt.image.BufferedImage;

public interface ISystemIO {

	void onAudioFrame(byte[] buffer, int bufferSize);

	// A message from the system, maybe to show in a message box
	void onMessage(String message);

	void onRomLoaded(int audioSampleRate, double audioFramesPerSecond);

	void onVideoFrame(BufferedImage frame);

	// TODO: Move controller functions here
}
