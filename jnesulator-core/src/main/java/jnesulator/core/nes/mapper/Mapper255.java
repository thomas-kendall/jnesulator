package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper255 extends BaseMapper {

	public Mapper255(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		int mode = (~addr >> 12 & 1);
		int bank = (addr >> 8 & 0x40) | (addr >> 6 & 0x3F);

		setmirroring(((addr & 0x2000) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * ((addr >> 8 & 0x40) | (addr & 0x3F)))) & (chrsize - 1);
		}
		// remap PRG banks
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * (bank & ~mode))) & (prgsize - 1);
		}
		for (int i = 0; i < 16; ++i) {
			prg_map[16 + i] = (1024 * (i + 16 * (bank | mode))) & (prgsize - 1);
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
