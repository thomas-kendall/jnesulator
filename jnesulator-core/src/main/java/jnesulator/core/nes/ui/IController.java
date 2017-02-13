package jnesulator.core.nes.ui;

public interface IController {

	int getbyte();

	void output(boolean state);

	int peekOutput();

	void strobe();
}
