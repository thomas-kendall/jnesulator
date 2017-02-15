package jnesulator.core.nes.audio;

import jnesulator.core.nes.mapper.TVType;

// TODO: Can we break this out into a component of the system and something that interacts with it?
public interface IAudioConsumer {

	boolean bufferHasLessThan(int samples);

	void destroy();

	void flushFrame(boolean waitIfBufferFull);

	void initialize(int samplerate, TVType tvtype);

	void outputSample(int sample);

	void pause();

	void resume();
}
