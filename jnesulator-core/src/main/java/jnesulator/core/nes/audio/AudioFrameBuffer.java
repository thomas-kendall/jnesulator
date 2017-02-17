package jnesulator.core.nes.audio;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.PrefsSingleton;
import jnesulator.core.nes.mapper.TVType;

public class AudioFrameBuffer {
	private boolean soundEnable;
	private byte[] audiobuf;
	private int bufptr = 0;
	private float outputvol;
	private NES nes;
	private double fps;
	private int sampleRate;

	public AudioFrameBuffer(NES nes) {
		this.nes = nes;
	}

	public boolean bufferHasLessThan(int samples) {
		// returns true if the audio buffer has less than the specified amt of
		// samples remaining in it
		return audiobuf.length - bufptr <= samples;
	}

	public void flushFrame() {
		if (soundEnable) {
			nes.getSystemIO().onAudioFrame(audiobuf, bufptr);
		}
		bufptr = 0;

	}

	public double getFps() {
		return fps;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void initialize(int sampleRate, TVType tvtype) {
		this.sampleRate = sampleRate;
		soundEnable = PrefsSingleton.get().getBoolean("soundEnable", true);
		outputvol = (float) (PrefsSingleton.get().getInt("outputvol", 13107) / 16384.);
		switch (tvtype) {
		case NTSC:
		default:
			fps = 60.;
			break;
		case PAL:
		case DENDY:
			fps = 50.;
			break;
		}
		if (soundEnable) {
			int samplesperframe = (int) Math.ceil((sampleRate * 2) / fps);
			audiobuf = new byte[samplesperframe * 2];
		}
	}

	public void outputSample(int sample) {
		if (soundEnable) {
			sample *= outputvol;
			if (sample < -32768) {
				sample = -32768;
				// System.err.println("clip");
			}
			if (sample > 32767) {
				sample = 32767;
				// System.err.println("clop");
			}
			// left ch
			int lch = sample;
			audiobuf[bufptr] = (byte) (lch & 0xff);
			audiobuf[bufptr + 1] = (byte) ((lch >> 8) & 0xff);
			// right ch
			int rch = sample;
			audiobuf[bufptr + 2] = (byte) (rch & 0xff);
			audiobuf[bufptr + 3] = (byte) ((rch >> 8) & 0xff);
			bufptr += 4;
		}
	}

}
