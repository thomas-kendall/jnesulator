package jnesulator.core.nes;

import java.awt.image.BufferedImage;

import jnesulator.core.nes.audio.IAudioConsumer;

public interface ISystemIO {

	// TODO: Convert getAudioConsumer() to individual methods for processing
	// audio
	// void onAudioFrame(int sample);
	IAudioConsumer getAudioConsumer();

	// A message from the system, maybe to show in a message box
	void onMessage(String message);

	void onVideoFrame(BufferedImage frame);

	// TODO: Move controller functions here
}
