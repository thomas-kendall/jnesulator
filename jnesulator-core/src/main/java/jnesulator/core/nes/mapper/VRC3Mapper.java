package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class VRC3Mapper extends BaseMapper {

	private int irqctr, irqreload = 0;

	private boolean irqmode, irqenable, irqackenable, interrupted = false;

	public VRC3Mapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		switch (addr >> 12) {
		case 0x8: // Bits 0-3 of IRQ reload value
			irqreload = (irqreload & 0xFFF0) | (data & 0xF);
			break;
		case 0x9: // Bits 4-7 of IRQ reload value
			irqreload = (irqreload & 0xFF0F) | (data & 0xF) << 4;
			break;
		case 0xA: // Bits 8-11 of IRQ reload value
			irqreload = (irqreload & 0xF0FF) | (data & 0xF) << 8;
			break;
		case 0xB: // Bits 12-15 of IRQ reload value
			irqreload = (irqreload & 0x0FFF) | (data & 0xF) << 12;
			break;
		case 0xC: // IRQ Control
			irqmode = ((data & (Utils.BIT2)) != 0);
			irqackenable = ((data & (Utils.BIT0)) != 0);

			irqenable = ((data & (Utils.BIT1)) != 0);
			if (irqenable) {
				if (irqmode) {
					irqctr &= 0xFF00;
					irqctr |= (irqreload & 0xFF);
				} else {
					irqctr = irqreload;
				}

				if (interrupted) {
					--getNES().getCPU().interrupt;
					interrupted = false;
				}
			}
			break;
		case 0xD: // IRQ Acknowledge
			irqenable = irqackenable;
			if (interrupted) {
				--getNES().getCPU().interrupt;
				interrupted = false;
			}
			break;
		case 0xF: // PRG Select
			for (int i = 0; i < 16; ++i) {
				prg_map[i] = (1024 * (i + 16 * (data & 0xF))) & (prgsize - 1);
			}
			break;
		}
	}

	@Override
	public void cpucycle(int cycles) {
		if (irqenable) {
			if (irqmode) { // 8-bit mode
				int temp = irqctr;
				irqctr &= 0xFF00;
				if (temp >= 0xFF) {
					irqctr = irqreload;
					irqctr |= (irqreload & 0xFF);
					if (!interrupted) {
						++getNES().getCPU().interrupt;
						interrupted = true;
					}
				} else {
					temp += cycles;
					irqctr |= temp;
				}
			} else { // 16-bit mode
				if (irqctr >= 0xFFFF) {
					irqctr = irqreload;
					if (!interrupted) {
						++getNES().getCPU().interrupt;
						interrupted = true;
					}
				} else {
					irqctr += cycles;
				}
			}
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		// swappable bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		// fixed bank
		for (int i = 1; i <= 16; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
