package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper229 extends BaseMapper {

	public Mapper229(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * addr)) & (chrsize - 1);
		}
		// remap PRG bank
		int bank = (addr & 0x1E) != 0 ? (addr & 0x1F) : 0;
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * bank)) & (prgsize - 1);
		}
		bank = (addr & 0x1E) != 0 ? (addr & 0x1F) : 1;
		for (int i = 0; i < 16; ++i) {
			prg_map[i + 16] = (1024 * (i + 16 * bank)) & (prgsize - 1);
		}

		setmirroring(((addr & (Utils.BIT5)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
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
