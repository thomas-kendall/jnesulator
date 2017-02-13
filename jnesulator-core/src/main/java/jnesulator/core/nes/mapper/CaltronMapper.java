package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class CaltronMapper extends BaseMapper {

	int reg = 0;

	public CaltronMapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr >= 0x6000 && addr <= 0x67FF) {
			reg = addr & 0xFF;

			// remap PRG bank
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * (addr & 7))) & (prgsize - 1);
			}

			setmirroring(((addr & (Utils.BIT5)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
		} else if (addr >= 0x8000 && addr <= 0xFFFF && (reg & 4) != 0) {
			// remap CHR bank
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * ((reg >> 1 & 0xC) | (data & 3)))) & (chrsize - 1);
			}
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		// needs to be in every mapper. Fill with initial cfg
		super.loadrom(loader);
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
