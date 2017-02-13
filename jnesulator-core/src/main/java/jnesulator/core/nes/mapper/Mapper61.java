package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper61 extends BaseMapper {

	public Mapper61(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		switch (addr & 0x30) {
		case 0x00:
		case 0x30:
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * (addr & 0xF))) & (prgsize - 1);
			}
			break;
		case 0x10:
		case 0x20:
			int prgselect = (addr << 1 & 0x1E) | (addr >> 4 & 2);
			for (int i = 0; i < 16; ++i) {
				prg_map[i] = (1024 * (i + 32 * prgselect)) & (prgsize - 1);
			}
			for (int i = 0; i < 16; ++i) {
				prg_map[i + 16] = (1024 * (i + 32 * prgselect)) & (prgsize - 1);
			}
		}

		setmirroring(((addr & (Utils.BIT7)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
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

	@Override
	public void reset() {
		cartWrite(0x8000, cartRead(0x8000));
	}
}