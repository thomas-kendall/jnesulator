package jnesulator.core.nes.ui;

import static jnesulator.core.nes.utils.BIT0;
import static jnesulator.core.nes.utils.BIT1;
import static jnesulator.core.nes.utils.BIT2;
import static jnesulator.core.nes.utils.BIT3;
import static jnesulator.core.nes.utils.BIT4;
import static jnesulator.core.nes.utils.BIT5;
import static jnesulator.core.nes.utils.BIT6;
import static jnesulator.core.nes.utils.BIT7;

public class PuppetController implements ControllerInterface {
	public enum Button {
		UP, DOWN, LEFT, RIGHT, A, B, SELECT, START
	}

	private int latchbyte = 0, controllerbyte = 0, outbyte = 0;

	@Override
	public int getbyte() {
		return outbyte;
	}

	@Override
	public void output(boolean state) {
		latchbyte = controllerbyte;
	}

	@Override
	public int peekOutput() {
		return latchbyte;
	}

	public void pressButton(Button button) {
		switch (button) {
		case UP:
			controllerbyte |= BIT4;
			break;
		case DOWN:
			controllerbyte |= BIT5;
			break;
		case LEFT:
			controllerbyte |= BIT6;
			break;
		case RIGHT:
			controllerbyte |= BIT7;
			break;
		case A:
			controllerbyte |= BIT0;
			break;
		case B:
			controllerbyte |= BIT1;
			break;
		case SELECT:
			controllerbyte |= BIT2;
			break;
		case START:
			controllerbyte |= BIT3;
			break;
		}
	}

	public void releaseButton(Button button) {
		switch (button) {
		case UP:
			controllerbyte &= ~BIT4;
			break;
		case DOWN:
			controllerbyte &= ~BIT5;
			break;
		case LEFT:
			controllerbyte &= ~BIT6;
			break;
		case RIGHT:
			controllerbyte &= ~BIT7;
			break;
		case A:
			controllerbyte &= ~BIT0;
			break;
		case B:
			controllerbyte &= ~BIT1;
			break;
		case SELECT:
			controllerbyte &= ~BIT2;
			break;
		case START:
			controllerbyte &= ~BIT3;
			break;
		}
	}

	public void resetButtons() {
		controllerbyte = 0;
	}

	@Override
	public void strobe() {
		// shifts a byte out
		outbyte = latchbyte & 1;
		latchbyte = ((latchbyte >> 1) | 0x100);
	}
}
