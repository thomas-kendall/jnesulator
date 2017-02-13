package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class Mapper244 extends BaseMapper {

	public Mapper244(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8065 || addr > 0x80E4) {
			super.cartWrite(addr, data);
			return;
		}

		if (addr < 0x80A5) {
			// remap PRG bank
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * ((addr - 0x8065) & 3))) & (prgsize - 1);
			}
		} else {
			// remap CHR bank
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (i + 8 * ((addr - 0x80A5) & 7))) & (chrsize - 1);
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
}