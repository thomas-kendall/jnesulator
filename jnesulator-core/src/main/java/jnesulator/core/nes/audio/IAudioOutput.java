package jnesulator.core.nes.audio;

public interface IAudioOutput {

	boolean bufferHasLessThan(int samples);

	void destroy();

	void flushFrame(boolean waitIfBufferFull);

	void outputSample(int sample);

	void pause();

	void resume();
}
