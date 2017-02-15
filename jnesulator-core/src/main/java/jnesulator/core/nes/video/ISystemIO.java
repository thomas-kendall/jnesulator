package jnesulator.core.nes.video;

import java.awt.image.BufferedImage;

public interface ISystemIO {
	void consumeAudioFrame(int sample);

	void consumeVideoFrame(BufferedImage frame);

	// TODO: Move controller functions here
}
