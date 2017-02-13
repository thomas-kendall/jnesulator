package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper60 extends BaseMapper {

	int reg = 0;

	public Mapper60(NES nes) {
		super(nes);
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 16; ++i) {
			prg_map[i + 16] = (1024 * i) & (prgsize - 1);
		}
	}

	@Override
	public void reset() {
		reg = (reg + 1) & 3;

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * reg)) & (chrsize - 1);
		}
		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * reg)) & (prgsize - 1);
		}
		for (int i = 0; i < 16; ++i) {
			prg_map[i + 16] = (1024 * (i + 16 * reg)) & (prgsize - 1);
		}
	}
}
