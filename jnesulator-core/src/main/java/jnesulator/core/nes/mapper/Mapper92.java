package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper92 extends BaseMapper {

	public Mapper92(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr,  int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		if ((data & 0x40) != 0) {
			// remap CHR bank
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * (data & 0xF))) & (chrsize - 1);
			}
		}

		if ((data & 0x80) != 0) {
			// remap PRG bank
			for (int i = 0; i < 16; ++i) {
				prg_map[16 + i] = (1024 * (i + 16 * (data & 0xF))) & (prgsize - 1);
			}
		}
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
