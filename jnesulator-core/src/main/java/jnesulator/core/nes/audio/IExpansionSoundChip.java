package jnesulator.core.nes.audio;

public interface IExpansionSoundChip {

	void clock(int cycles);

	int getval();

	void write(int register, int data);
}
