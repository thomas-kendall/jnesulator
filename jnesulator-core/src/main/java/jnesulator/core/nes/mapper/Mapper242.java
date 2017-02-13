package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper242 extends BaseMapper {

	public Mapper242(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xFFFF) {
			super.cartWrite(addr, data);
			return;
		}

		// remap PRG bank
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * (i + 32 * (addr >> 3))) & (prgsize - 1);
		}

		setmirroring(((addr & (Utils.BIT1)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
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
