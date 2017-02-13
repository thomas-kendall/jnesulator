package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class CrazyClimberMapper extends BaseMapper {

	public CrazyClimberMapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		int bank = (data & 7);
		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[16 + i] = (1024 * (i + 16 * bank)) & (prgsize - 1);
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		// needs to be in every mapper. Fill with initial cfg
		super.loadrom(loader);
		// movable (second) bank; first one is fixed
		for (int i = 0; i < 16; ++i) {
			prg_map[16 + i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
