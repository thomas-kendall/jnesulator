package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class IremH3001Mapper extends BaseMapper {

	private int[] chrbank = { 0, 0, 0, 0, 0, 0, 0, 0 };

	private int irqctr, irqreload = 0;
	private boolean irqenable, interrupted = false;

	public IremH3001Mapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xCFFF) {
			super.cartWrite(addr, data);
			return;
		}

		if (addr >= 0x8000 && addr <= 0x8FFF) { // PRG Reg 0
			for (int i = 0; i < 8; ++i) {
				prg_map[i] = (1024 * (i + (data * 8))) & (prgsize - 1);
			}
		} else if (addr == 0x9001) { // Mirroring
			setmirroring(((data & (Utils.BIT7)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
		} else if (addr == 0x9003) { // IRQ Enable
			irqenable = ((data & (Utils.BIT7)) != 0);
			if (interrupted) {
				--getNES().getCPU().interrupt;
				interrupted = false;
			}
		} else if (addr == 0x9004) { // IRQ Reload
			irqctr = irqreload;
			if (interrupted) {
				--getNES().getCPU().interrupt;
				interrupted = false;
			}
		} else if (addr == 0x9005) { // High 8 bits of IRQ Reload
			irqreload = (irqreload & 0x00FF) | (data << 8);
		} else if (addr == 0x9006) { // Low 8 bits of IRQ Reload
			irqreload = (irqreload & 0xFF00) | data;
		} else if (addr >= 0xA000 && addr <= 0xAFFF) { // PRG Reg 1
			for (int i = 0; i < 8; ++i) {
				prg_map[i + 8] = (1024 * (i + data * 8)) & (prgsize - 1);
			}
		} else if (addr >= 0xB000 && addr <= 0xBFFF) { // CHR Regs
			chrbank[addr & 7] = data;
			setppubank(1, (addr & 7), chrbank[addr & 7]);
		} else if (addr >= 0xC000 && addr <= 0xCFFF) { // PRG Reg 2
			for (int i = 0; i < 8; ++i) {
				prg_map[i + 16] = (1024 * (i + data * 8)) & (prgsize - 1);
			}
		}
	}

	@Override
	public void cpucycle(int cycles) {
		if (irqenable) {
			if (irqctr <= 0) {
				if (!interrupted) {
					++getNES().getCPU().interrupt;
					interrupted = true;
				}
				irqenable = false;
			} else {
				irqctr -= cycles;
			}
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		for (int i = 1; i <= 32; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}

	private void setppubank(int banksize, int bankpos, int banknum) {
		for (int i = 0; i < banksize; ++i) {
			chr_map[i + bankpos] = (1024 * (banknum + i)) & (chrsize - 1);
		}
	}
}
