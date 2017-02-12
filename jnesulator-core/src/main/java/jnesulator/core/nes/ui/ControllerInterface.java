package jnesulator.core.nes.ui;

public interface ControllerInterface {

	int getbyte();

	void output(final boolean state);

	int peekOutput();

	void strobe();
}
