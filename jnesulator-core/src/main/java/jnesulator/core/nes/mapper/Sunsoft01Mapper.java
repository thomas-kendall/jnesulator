package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Sunsoft01Mapper extends BaseMapper {

	private int lowBank = 0;

	private int highBank = 0;

	public Sunsoft01Mapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr >= 0x6000 && addr < 0x8000) {
			lowBank = data & 7;
			highBank = (data >> 4) & 7;

			// remap CHR bank 0
			for (int i = 0; i < 4; ++i) {
				chr_map[i] = (1024 * (i + lowBank * 4)) % chrsize;
			}
			// remap CHR bank 1
			for (int i = 0; i < 4; ++i) {
				chr_map[4 + i] = (1024 * (i + highBank * 4)) % chrsize;
			}
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
