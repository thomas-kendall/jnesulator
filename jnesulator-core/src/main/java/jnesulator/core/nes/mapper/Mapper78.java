package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper78 extends BaseMapper {

	public Mapper78(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr,  int data) {
		// System.out.println(data);
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		int prgselect = data & 7;
		int chrselect = (data >> 4) & 0xf;
		if (crc == 0x42392440) // Cosmo Carrier
		{
			setmirroring(((data & (Utils.BIT3)) != 0) ? MirrorType.SS_MIRROR1 : MirrorType.SS_MIRROR0);
		} else {
			setmirroring(((data & (Utils.BIT3)) != 0) ? MirrorType.V_MIRROR : MirrorType.H_MIRROR);
		}

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * chrselect)) & (chrsize - 1);
		}
		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * prgselect)) & (prgsize - 1);
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		for (int i = 1; i <= 32; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 1; i <= 8; ++i) {
			chr_map[8 - i] = chrsize - (1024 * i);
		}
	}
}
