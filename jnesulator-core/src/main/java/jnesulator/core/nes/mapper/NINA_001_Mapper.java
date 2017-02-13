package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class NINA_001_Mapper extends BaseMapper {

	public NINA_001_Mapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr,  int data) {
		if (addr < 0x7ffd || addr > 0x7fff) {
			super.cartWrite(addr, data);
			return;
		}

		switch (addr) {
		case 0x7FFD:
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * data)) & (prgsize - 1);
			}
			break;
		case 0x7FFE:
			for (int i = 0; i < 4; ++i) {
				chr_map[i] = (1024 * (i + 4 * data)) & (chrsize - 1);
			}
			break;
		case 0x7FFF:
			for (int i = 0; i < 4; ++i) {
				chr_map[4 + i] = (1024 * (i + 4 * data)) & (chrsize - 1);
			}
			break;
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
