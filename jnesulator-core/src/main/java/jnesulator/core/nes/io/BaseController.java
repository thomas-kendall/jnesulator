package jnesulator.core.nes.io;

public class BaseController implements IController {

	private int latchByte = 0;
	private int controllerByte = 0;
	private int outbyte = 0; // Used for strobing, really just a bit

	@Override
	public int getbyte() {
		return outbyte;
	}

	protected void inputPress(ControllerInput input) {

		// Left and right cannot be pressed simultaneously
		if (input == ControllerInput.Left && (controllerByte & ControllerInput.Right.getBit()) > 0) {
			return;
		}
		if (input == ControllerInput.Right && (controllerByte & ControllerInput.Left.getBit()) > 0) {
			return;
		}

		// Up and down cannot be pressed simultaneously
		if (input == ControllerInput.Up && (controllerByte & ControllerInput.Down.getBit()) > 0) {
			return;
		}
		if (input == ControllerInput.Down && (controllerByte & ControllerInput.Up.getBit()) > 0) {
			return;
		}

		controllerByte |= input.getBit();
	}

	protected void inputRelease(ControllerInput input) {
		controllerByte &= ~input.getBit();
	}

	@Override
	public void output(boolean state) {
		latchByte = controllerByte;
	}

	@Override
	public int peekOutput() {
		return latchByte;
	}

	@Override
	public void strobe() {
		// shifts a byte out
		outbyte = latchByte & 1;
		latchByte = ((latchByte >> 1) | 0x100);
	}
}
