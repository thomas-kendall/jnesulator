package jnesulator.core.nes.io;

public interface IController {

	int getbyte();

	void output(boolean state);

	int peekOutput();

	void strobe();
}
