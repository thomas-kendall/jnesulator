package jnesulator.core.nes.ui;

import jnesulator.core.nes.NES;

// TODO: Remove this and use something more appropriate, like ISystemIO. Also decouple it from Runnable.
public interface IGUI extends Runnable {

	NES getNes();

	void messageBox(String message);

	void render();

	@Override
	void run();

	void setFrame(int[] frame, int[] bgcolor, boolean dotcrawl);
	// Frame is now a 256x240 array with NES color numbers from 0-3F
	// plus the state of the 3 color emphasis bits in bits 7,8,9

	void setNES(NES nes);
}
