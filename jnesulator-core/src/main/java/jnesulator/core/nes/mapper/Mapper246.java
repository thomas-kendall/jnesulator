package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper246 extends BaseMapper {

	public Mapper246(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x6000 || addr > 0x67ff) {
			super.cartWrite(addr, data);
			return;
		}
		switch (addr & 7) {
		case 0:
			for (int i = 0; i < 8; ++i) {
				prg_map[i] = (1024 * (i + 8 * data)) & (prgsize - 1);
			}
			break;
		case 1:
			for (int i = 0; i < 8; ++i) {
				prg_map[8 + i] = (1024 * (i + 8 * data)) & (prgsize - 1);
			}
			break;
		case 2:
			for (int i = 0; i < 8; ++i) {
				prg_map[16 + i] = (1024 * (i + 8 * data)) & (prgsize - 1);
			}
			break;
		case 3:
			for (int i = 0; i < 8; ++i) {
				prg_map[24 + i] = (1024 * (i + 8 * data)) & (prgsize - 1);
			}
			break;
		case 4:
			for (int i = 0; i < 2; ++i) {
				chr_map[i] = (1024 * (i + 2 * data)) & (chrsize - 1);
			}
			break;
		case 5:
			for (int i = 0; i < 2; ++i) {
				chr_map[2 + i] = (1024 * (i + 2 * data)) & (chrsize - 1);
			}
			break;
		case 6:
			for (int i = 0; i < 2; ++i) {
				chr_map[4 + i] = (1024 * (i + 2 * data)) & (chrsize - 1);
			}
			break;
		case 7:
			for (int i = 0; i < 2; ++i) {
				chr_map[6 + i] = (1024 * (i + 2 * data)) & (chrsize - 1);
			}
			break;
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		// swappable bank
		for (int i = 0; i < 24; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		// fixed bank
		for (int i = 1; i <= 8; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
