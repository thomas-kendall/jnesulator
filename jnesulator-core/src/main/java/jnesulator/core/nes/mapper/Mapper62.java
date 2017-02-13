package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper62 extends BaseMapper {

	boolean prg_mode;

	int prgselect, chrselect;

	public Mapper62(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		prg_mode = ((addr & (Utils.BIT5)) != 0);
		prgselect = (addr & 0x40) | ((addr >> 8) & 0x3F);
		chrselect = (addr << 2) | (data & 3);

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * chrselect)) & (chrsize - 1);
		}
		// remap PRG bank
		if (prg_mode) {
			for (int i = 0; i < 16; ++i) {
				prg_map[i] = (1024 * (i + 16 * prgselect)) & (prgsize - 1);
			}
			for (int i = 0; i < 16; ++i) {
				prg_map[16 + i] = (1024 * (i + 16 * prgselect)) & (prgsize - 1);
			}
		} else {
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * (prgselect >> 1))) & (prgsize - 1);
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
}
