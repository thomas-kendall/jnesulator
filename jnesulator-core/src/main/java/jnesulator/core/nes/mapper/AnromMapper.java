package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class AnromMapper extends BaseMapper {

	public AnromMapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		// remap all 32k of PRG to 32 x bank #
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * (i + (32 * (data & 15)))) & (prgsize - 1);
		}
		setmirroring(((data & (Utils.BIT4)) != 0) ? MirrorType.SS_MIRROR1 : MirrorType.SS_MIRROR0);

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
