package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper58 extends BaseMapper {

	public Mapper58(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		setmirroring(((addr & (Utils.BIT7)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * (addr >> 3))) & (chrsize - 1);
		}

		int prgselect = addr & ~(~addr >> 6 & 1);
		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * prgselect)) & (prgsize - 1);
		}
		prgselect = addr | (~addr >> 6 & 1);
		for (int i = 0; i < 16; ++i) {
			prg_map[16 + i] = (1024 * (i + 16 * prgselect)) & (prgsize - 1);
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
		cartWrite(0x8000, cartRead(0x8000));
	}
}
