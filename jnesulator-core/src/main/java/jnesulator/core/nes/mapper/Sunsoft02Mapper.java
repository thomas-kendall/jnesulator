package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Sunsoft02Mapper extends BaseMapper {

	boolean m93;

	public Sunsoft02Mapper(NES nes, int mappernum) {
		super(nes);
		m93 = (mappernum == 93);
	}

	@Override
	public void cartWrite(int addr,  int data) {
		int prgselect;

		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		if (m93) {
			prgselect = (data >> 4) & 15;
		} else {
			prgselect = (data >> 4) & 7;
			setmirroring(((data & (Utils.BIT3)) != 0) ? MirrorType.SS_MIRROR1 : MirrorType.SS_MIRROR0);

			int chrselect = ((data & 7) | (data >> 7) * 8);

			// remap CHR bank
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * chrselect)) & (chrsize - 1);
			}
		}

		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * prgselect)) & (prgsize - 1);
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		// swappable bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		// fixed bank
		for (int i = 1; i <= 16; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
