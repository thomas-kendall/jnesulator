package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class NINA_003_006_Mapper extends BaseMapper {

	boolean m113 = true;

	public NINA_003_006_Mapper(NES nes, int mappernum) {
		super(nes);
		switch (mappernum) {
		// mappers 79 and 113 differ mainly on whether they can control
		// mirroring or not
		case 79:
			m113 = false;
			break;
		case 113:
			m113 = true;
			break;
		}
	}

	@Override
	public void cartWrite(int addr,  int data) {
		if (addr < 0x4100 || addr > 0x5fff) {
			super.cartWrite(addr, data);
			return;
		}

		if (m113) {
			setmirroring(((data & (Utils.BIT7)) != 0) ? MirrorType.V_MIRROR : MirrorType.H_MIRROR);

			// remap CHR bank
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * ((data >> 3 & 8) | (data & 7)))) & (chrsize - 1);
			}
			// remap PRG bank
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * (data >> 3 & 7))) & (prgsize - 1);
			}
		} else {
			// remap CHR bank
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * data)) & (chrsize - 1);
			}
			// remap PRG bank
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * (data >> 3))) & (prgsize - 1);
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

	@Override
	public void reset() {
		for (int i = 0x4100; i < 0x6000; i += 0x200) {
			cartWrite(i, i + 0xFF);
		}
	}
}
