package jnesulator.core.nes.io;

import static jnesulator.core.nes.Utils.BIT0;
import static jnesulator.core.nes.Utils.BIT1;
import static jnesulator.core.nes.Utils.BIT2;
import static jnesulator.core.nes.Utils.BIT3;
import static jnesulator.core.nes.Utils.BIT4;
import static jnesulator.core.nes.Utils.BIT5;
import static jnesulator.core.nes.Utils.BIT6;
import static jnesulator.core.nes.Utils.BIT7;

public enum ControllerInput {
	A(BIT0), B(BIT1), Select(BIT2), Start(BIT3), Up(BIT4), Down(BIT5), Left(BIT6), Right(BIT7);

	private final int bit;

	ControllerInput(int bit) {
		this.bit = bit;
	}

	public int getBit() {
		return bit;
	}
}