package jnesulator.core.nes.mapper;

import jnesulator.core.nes.utils;
import jnesulator.core.nes.audio.VRC6SoundChip;

public class VRC6Mapper extends Mapper {

	int[][] registerselectbits = { { 0, 1 }, { 1, 0 } };
	int[] registers;
	int prgbank0, prgbank1 = 0;
	int[] chrbank = { 0, 0, 0, 0, 0, 0, 0, 0 };
	boolean irqmode, irqenable, irqack, firedinterrupt = false;
	int irqreload, irqcounter = 22;
	VRC6SoundChip sndchip;
	boolean hasInitSound = false;

	int prescaler = 341;

	public VRC6Mapper(int mappernum) {
		super();
		sndchip = new VRC6SoundChip();

		switch (mappernum) {
		// vrc6 has 2 different mapper numbers, for 2 different ways to assign
		// the registers
		case 24:
			registers = registerselectbits[0];
			break;
		case 26:
		default:
			registers = registerselectbits[1];
			break;

		}
	}

	@Override
	public final void cartWrite(final int addr, final int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		final boolean bit0 = ((addr & (1 << registers[0])) != 0);
		final boolean bit1 = ((addr & (1 << registers[1])) != 0);
		switch (addr >> 12) {
		case 0x8:
			// 8000-8003: prg bank 0 select
			prgbank0 = data;
			setbanks();
			break;
		case 0x9:
		case 0xa:
			// sound registers here
			sndchip.write((addr & 0xf000) + (bit1 ? 2 : 0) + (bit0 ? 1 : 0), data);
			break;
		case 0xc:
			// c000-c003: prg bank 1 select
			prgbank1 = data;
			setbanks();
			break;
		case 0xb:
			if (bit0 && bit1) {
				// mirroring select
				switch ((data >> 2) & 3) {
				case 0:
					setmirroring(Mapper.MirrorType.V_MIRROR);
					break;
				case 1:
					setmirroring(Mapper.MirrorType.H_MIRROR);
					break;
				case 2:
					setmirroring(Mapper.MirrorType.SS_MIRROR0);
					break;
				case 3:
					setmirroring(Mapper.MirrorType.SS_MIRROR1);
					break;
				}
			} else {
				// expansion sound register here as well
				sndchip.write((addr & 0xf000) + (bit1 ? 2 : 0) + (bit0 ? 1 : 0), data);
			}
			break;
		case 0xd:
			// character bank selects
			chrbank[(bit1 ? 2 : 0) + (bit0 ? 1 : 0)] = data;
			setbanks();
			break;
		case 0xe:
			chrbank[(bit1 ? 2 : 0) + (bit0 ? 1 : 0) + 4] = data;
			setbanks();
			break;
		case 0xf:
			// irq control
			if (!bit1) {
				if (!bit0) {
					irqreload = data;
				} else {
					irqack = ((data & (utils.BIT0)) != 0);
					irqenable = ((data & (utils.BIT1)) != 0);
					irqmode = ((data & (utils.BIT2)) != 0);
					if (irqenable) {
						irqcounter = irqreload;
						prescaler = 341;
					}
					if (firedinterrupt) {
						--cpu.interrupt;
					}
					firedinterrupt = false;
				}
			} else {
				if (!bit0) {
					irqenable = irqack;
					if (firedinterrupt) {
						--cpu.interrupt;
					}
					firedinterrupt = false;
				}
			}

		}
	}

	@Override
	public void cpucycle(int cycles) {
		if (irqenable) {
			if (irqmode) {
				scanlinecount();
				// clock regardless of prescaler state
			} else {
				prescaler -= 3;
				if (prescaler <= 0) {
					prescaler += 341;
					scanlinecount();
				}
			}
		}
	}

	@Override
	public void loadrom() throws BadMapperException {
		super.loadrom();
		// needs to be in every mapper. Fill with initial cfg
		for (int i = 1; i <= 32; ++i) {
			// map last banks in to start off
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}

	public void scanlinecount() {
		if (!hasInitSound) {
			// tiny hack, because the APU is not initialized until AFTER this
			// happens
			// TODO: this really should not need to be here.
			cpuram.apu.addExpnSound(sndchip);
			hasInitSound = true;
		}
		if (irqenable) {
			if (irqcounter == 255) {
				irqcounter = irqreload;
				// System.err.println("Interrupt @ Scanline " + scanline + "
				// reload " + irqreload);
				if (!firedinterrupt) {
					++cpu.interrupt;
				}
				firedinterrupt = true;
			} else {
				++irqcounter;
			}
		}
	}

	private void setbanks() {
		// map prg banks
		// last 8k fixed to end of rom
		for (int i = 1; i <= 8; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		// first bank set to prg0 register
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * prgbank0)) % prgsize;
		}
		// second bank set to prg1 register
		for (int i = 0; i < 8; ++i) {
			prg_map[i + 16] = (1024 * (i + 8 * prgbank1)) % prgsize;
		}

		// map chr banks
		for (int i = 0; i < 8; ++i) {
			setppubank(1, i, chrbank[i]);
		}
	}

	private void setppubank(final int banksize, final int bankpos, final int banknum) {
		// System.err.println(banksize + ", " + bankpos + ", "+ banknum);
		for (int i = 0; i < banksize; ++i) {
			chr_map[i + bankpos] = (1024 * ((banknum) + i)) % chrsize;
		}
		// utils.printarray(chr_map);
	}
}
