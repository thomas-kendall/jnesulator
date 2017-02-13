package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper212 extends BaseMapper {

	public Mapper212(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		} else if (addr >= 0x8000 && addr <= 0xBFFF) {
			// remap PRG bank
			for (int i = 0; i < 16; ++i) {
				prg_map[i] = (1024 * (i + 16 * addr)) & (prgsize - 1);
			}
			for (int i = 0; i < 16; ++i) {
				prg_map[i + 16] = (1024 * (i + 16 * addr)) & (prgsize - 1);
			}
		} else if (addr >= 0xC000 && addr <= 0xFFFF) {
			// remap PRG bank
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * (addr >> 1))) & (prgsize - 1);
			}
		}
		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * addr)) & (chrsize - 1);
		}
		setmirroring(((addr & (Utils.BIT4)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		for (int i = 0; i < 16; ++i) {
			prg_map[16 + i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
