package jnesulator.core.nes.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import jnesulator.core.nes.PrefsSingleton;

public class SwingAudioImpl {

	private boolean soundEnable;
	private SourceDataLine sdl;

	// @Override
	// public boolean bufferHasLessThan(int samples) {
	// // returns true if the audio buffer has less than the specified amt of
	// // samples remaining in it
	// return (sdl == null) ? false : ((sdl.getBufferSize() - sdl.available())
	// <= samples);
	// }

	public void destroy() {
		if (soundEnable) {
			if (sdl.isRunning()) {
				sdl.stop();
			}
			sdl.close();
		}
	}

	// @Override
	// public void flushFrame(boolean waitIfBufferFull) {
	// if (soundEnable) {
	//
	// // if (sdl.available() == sdl.getBufferSize()) {
	// // System.err.println("Audio is underrun");
	// // }
	// if (sdl.available() < bufptr) {
	// // System.err.println("Audio is blocking");
	// if (waitIfBufferFull) {
	// // write to audio buffer and don't worry if it blocks
	// sdlWrite();
	// }
	// // else don't bother to write if the buffer is full
	// } else {
	// sdlWrite();
	// }
	// }
	// bufptr = 0;
	//
	// }

	public void initialize(int samplerate, double fps) {
		soundEnable = PrefsSingleton.get().getBoolean("soundEnable", true);
		// outputvol = (float) (PrefsSingleton.get().getInt("outputvol", 13107)
		// / 16384.);
		if (soundEnable) {
			int samplesperframe = (int) Math.ceil((samplerate * 2) / fps);
			try {
				AudioFormat af = new AudioFormat(samplerate, 16, // bit
						2, // channel
						true, // signed
						false // little endian
				// (works everywhere, afaict, but macs need 44100 sample rate)
				);
				sdl = AudioSystem.getSourceDataLine(af);
				sdl.open(af,
						samplesperframe * 4 * 2 /* ch */ * 2 /* bytes/sample */);
				// create 4 frame audio buffer
				sdl.start();
			} catch (LineUnavailableException a) {
				System.err.println(a);
				soundEnable = false;
			} catch (IllegalArgumentException a) {
				System.err.println(a);
				soundEnable = false;
			}
		}
	}

	public void onAudioFrame(byte[] buffer, int bufferSize) {
		sdl.write(buffer, 0, bufferSize);
	}

	public void pause() {
		if (soundEnable) {
			sdl.flush();
			sdl.stop();
		}
	}

	public void resume() {
		if (soundEnable) {
			sdl.start();
		}
	}
}
