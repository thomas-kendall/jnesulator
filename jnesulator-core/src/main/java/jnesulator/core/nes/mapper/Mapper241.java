package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper241 extends BaseMapper {

	public Mapper241(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		// remap PRG bank
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * (i + 32 * data)) & (prgsize - 1);
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
