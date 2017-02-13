package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper48 extends BaseMapper {

	int prgbank0, prgbank1 = 0;

	int[] chrbank = { 0, 0, 0, 0, 0, 0 };
	private int irqctrreload = 0;
	private int irqctr = 0;
	private boolean irqenable = false;
	private boolean irqreload = false;
	private boolean interrupted = false;

	public Mapper48(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xFFFF) {
			super.cartWrite(addr, data);
			return;
		} else if (addr <= 0x9FFF) {
			switch (addr & 3) {
			case 0:
				prgbank0 = data;
				setbanks();
				break;
			case 1:
				prgbank1 = data;
				setbanks();
				break;
			case 2:
				chrbank[0] = data;
				setbanks();
				break;
			case 3:
				chrbank[1] = data;
				setbanks();
				break;
			}
		} else if (addr <= 0xBFFF) {
			switch (addr & 3) {
			case 0:
				chrbank[2] = data;
				setbanks();
				break;
			case 1:
				chrbank[3] = data;
				setbanks();
				break;
			case 2:
				chrbank[4] = data;
				setbanks();
				break;
			case 3:
				chrbank[5] = data;
				setbanks();
				break;
			}
		} else if (addr <= 0xDFFF) {
			switch (addr & 3) {
			case 0:
				// value written here used to reload irq counter _@ end of
				// scanline_
				irqctrreload = data & 0xFF;
				irqreload = true;
				break;
			case 1:
				// any value here reloads irq counter
				irqctr = data;
				irqreload = true;
				break;
			case 2:
				// any value here enables interrupts
				irqenable = true;
				break;
			case 3:
				// any value here disables IRQ and acknowledges
				if (interrupted) {
					--getNES().getCPU().interrupt;
				}
				interrupted = false;
				irqenable = false;
				irqctr = irqctrreload;
				break;
			}
		} else if (addr <= 0xFFFF) {
			switch (addr & 3) {
			case 0:
				setmirroring(((data & (Utils.BIT6)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
				break;
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

	@Override
	public void notifyscanline(int scanline) {
		// Scanline counter
		if (scanline > 239 && scanline != 261) {
			// clocked on LAST line of vblank and all lines of frame. Not on
			// 240.
			return;
		}
		if (!getNES().getPPU().mmc3CounterClocking()) {
			return;
		}

		if (irqreload) {
			irqreload = false;
			irqctr = irqctrreload;
		}

		if (irqctr-- <= 0) {
			if (irqctrreload == 0) {
				return;
				// irqs stop being generated if reload set to zero
			}
			if (irqenable && !interrupted) {
				++getNES().getCPU().interrupt;
				interrupted = true;
			}
			irqctr = irqctrreload;
		}
	}

	private void setbanks() {
		// map prg banks
		// last two banks fixed to the last two banks in ROM
		for (int i = 1; i <= 16; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		// first bank set to prg0 register
		for (int i = 0; i < 8; ++i) {
			prg_map[i] = (1024 * (i + 8 * prgbank0)) & (prgsize - 1);
		}
		// second bank set to prg1 register
		for (int i = 0; i < 8; ++i) {
			prg_map[i + 8] = (1024 * (i + 8 * prgbank1)) & (prgsize - 1);
		}

		// map chr banks
		setppubank(1, 4, chrbank[2]);
		setppubank(1, 5, chrbank[3]);
		setppubank(1, 6, chrbank[4]);
		setppubank(1, 7, chrbank[5]);

		setppubank(2, 0, chrbank[0]);
		setppubank(2, 2, chrbank[1]);
	}

	private void setppubank(int banksize, int bankpos, int banknum) {
		for (int i = 0; i < banksize; ++i) {
			chr_map[i + bankpos] = (1024 * (i + (banksize * banknum))) & (chrsize - 1);
		}
	}
}
