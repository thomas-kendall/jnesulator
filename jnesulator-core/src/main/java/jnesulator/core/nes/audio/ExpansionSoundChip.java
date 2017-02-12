package jnesulator.core.nes.audio;

public interface ExpansionSoundChip {

	void clock(final int cycles);

	int getval();

	void write(int register, int data);
}
