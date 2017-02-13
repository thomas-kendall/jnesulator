package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper200 extends BaseMapper {

	public Mapper200(NES nes) {
		super(nes);
	}

	@Override
	public int cartRead(int addr) {
		// by default has wram at 0x6000 and cartridge at 0x8000-0xfff
		// but some mappers have different so override for those
		if (addr < 0x4000) {
			return prg[prg_map[((addr & 0x3fff)) >> 10] + (addr & 1023)];
		} else {
			return prg[prg_map[((addr & 0x3fff)) >> 10] + ((addr - 0x4000) & 1023)];
		}
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		int reg = addr & 7;

		setmirroring(((data & (Utils.BIT3)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * reg)) & (chrsize - 1);
		}
		// remap PRG bank
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * (i + 16 * reg)) & (prgsize - 1);
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
