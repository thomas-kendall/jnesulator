package jnesulator.core.nes.audio;

import jnesulator.core.nes.Utils;

public class MMC5SoundChip implements IExpansionSoundChip {
	// really quickly hacked together. Need better interfaces for this kind of
	// thing.

	private static int[] DUTYLOOKUP = { 1, 2, 4, 6 };
	private static int[] LENCTRLOAD = { 10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14, 12, 16, 24, 18,
			48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30 };
	private Timer[] timers = { new SquareTimer(8, 2), new SquareTimer(8, 2) };
	private int[] volume = new int[2];
	private boolean[] lenCtrEnable = { true, true, true, true };
	private boolean pcmMode, pcmIRQen;

	private int cycles, pcmOut;

	private int[] lengthctr = { 0, 0, 0, 0 };

	private boolean[] lenctrHalt = { true, true, true, true };

	// instance variables for envelope units
	private int[] envelopeValue = { 15, 15, 15, 15 };
	private int[] envelopeCounter = { 0, 0, 0, 0 };
	private int[] envelopePos = { 0, 0, 0, 0 };
	private boolean[] envConstVolume = { true, true, true, true };

	private boolean[] envelopeStartFlag = { false, false, false, false };
	private int framectr = 0;
	private int ctrmode = 4;

	@Override
	public void clock(int cycle) {
		cycles += cycle;
		if ((cycles % 7445) != cycles) {
			clockframecounter();
			cycles %= 6445;
		}
		timers[0].clock(cycle);
		timers[1].clock(cycle);
	}

	private void clockframecounter() {
		// System.err.println("frame ctr clock " + framectr);
		// should be ~4x a frame, 240 Hz
		// separate timebase from the NES APU though
		if (framectr < 4) {
			setenvelope();
		}
		if ((ctrmode == 4 && (framectr == 1 || framectr == 3)) || (ctrmode == 5 && (framectr == 0 || framectr == 2))) {
			setlength();
		}
		++framectr;
		framectr %= ctrmode;
		setvolumes();
	}

	@Override
	public int getval() {
		int accum = 0;
		for (int i = 0; i < 2; ++i) {
			accum += volume[i] * timers[i].getval() * 750;
		}
		accum += pcmOut << 5;
		return accum;
	}

	private void setenvelope() {
		for (int i = 0; i < 2; ++i) {
			if (envelopeStartFlag[i]) {
				envelopeStartFlag[i] = false;
				envelopePos[i] = envelopeValue[i] + 1;
				envelopeCounter[i] = 15;
			} else {
				--envelopePos[i];
			}
			if (envelopePos[i] <= 0) {
				envelopePos[i] = envelopeValue[i] + 1;
				if (envelopeCounter[i] > 0) {
					--envelopeCounter[i];
				} else if (lenctrHalt[i] && envelopeCounter[i] <= 0) {
					envelopeCounter[i] = 15;
				}
			}
		}
	}

	private void setlength() {
		for (int i = 0; i < 4; ++i) {
			if (!lenctrHalt[i] && lengthctr[i] > 0) {
				--lengthctr[i];
				if (lengthctr[i] == 0) {
					setvolumes();
				}
			}
		}
	}

	private void setvolumes() {
		volume[0] = ((lengthctr[0] <= 0) ? 0 : (((envConstVolume[0]) ? envelopeValue[0] : envelopeCounter[0])));
		volume[1] = ((lengthctr[1] <= 0) ? 0 : (((envConstVolume[1]) ? envelopeValue[1] : envelopeCounter[1])));
		// System.err.println("setvolumes " + volume[1]);
	}

	public int status() {
		return (lengthctr[0] == 0 ? 0 : 1) + (lengthctr[1] == 0 ? 0 : 2);
	}

	@Override
	public void write(int register, int data) {
		switch (register) {
		case 0x0:
			// length counter 1 halt
			lenctrHalt[0] = ((data & (Utils.BIT5)) != 0);
			// pulse 1 duty cycle
			timers[0].setduty(DUTYLOOKUP[data >> 6]);
			// and envelope
			envConstVolume[0] = ((data & (Utils.BIT4)) != 0);
			envelopeValue[0] = data & 15;
			// setvolumes();
			break;
		case 0x1:
			// pulse 1 sweep setup
			// mmc5 lacks a sweep
			break;
		case 0x2:
			// pulse 1 timer low bit
			timers[0].setperiod((timers[0].getperiod() & 0xfe00) + (data << 1));
			break;
		case 0x3:
			// length counter load, timer 1 high bits
			if (lenCtrEnable[0]) {
				lengthctr[0] = LENCTRLOAD[data >> 3];
			}
			timers[0].setperiod((timers[0].getperiod() & 0x1ff) + ((data & 7) << 9));
			// sequencer restarted
			timers[0].reset();
			// envelope also restarted
			envelopeStartFlag[0] = true;
			break;
		case 0x4:
			// length counter 2 halt
			lenctrHalt[1] = ((data & (Utils.BIT5)) != 0);
			// pulse 2 duty cycle
			timers[1].setduty(DUTYLOOKUP[data >> 6]);
			// and envelope
			envConstVolume[1] = ((data & (Utils.BIT4)) != 0);
			envelopeValue[1] = data & 15;
			// setvolumes();
			break;
		case 0x5:
			// pulse 2 sweep setup
			break;
		case 0x6:
			// pulse 2 timer low bit
			timers[1].setperiod((timers[1].getperiod() & 0xfe00) + (data << 1));
			break;
		case 0x7:
			if (lenCtrEnable[1]) {
				lengthctr[1] = LENCTRLOAD[data >> 3];
			}
			timers[1].setperiod((timers[1].getperiod() & 0x1ff) + ((data & 7) << 9));
			// sequencer restarted
			timers[1].reset();
			// envelope also restarted
			envelopeStartFlag[1] = true;
			break;
		case 0x10:
			pcmMode = ((data & (Utils.BIT0)) != 0);
			// true = read mode, false = write mode
			// read mode watches ALL reads in first 8k of PRG ROM
			// and writes to dpcm reg
			// (no way to implement w/o refactors)
			pcmIRQen = ((data & (Utils.BIT7)) != 0);
			if (pcmIRQen || pcmMode) {
				System.err.println("Implement the MMC5 PCM IRQ, something's using it!");
			}
			break;
		case 0x11:
			if (!pcmMode) {
				if (data != 0) {
					pcmOut = data;
				} else {
					// should trip an irq, but no way to in current design
				}
			}
			break;
		default:
			break;
		}
	}
}
