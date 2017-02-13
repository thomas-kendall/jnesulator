package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper87 extends BaseMapper {

	public Mapper87(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr,  int data) {
		if (addr >= 0x6000 && addr < 0x8000) {
			// remap CHR bank
			int bit0 = (data >> 1) & 1;
			int bit1 = data & 1;
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * ((bit1 << 1) + bit0))) & (chrsize - 1);
			}
		} else {
			super.cartWrite(addr, data);
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
