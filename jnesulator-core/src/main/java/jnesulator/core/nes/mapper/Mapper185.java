package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

//CNROM with copy protection
public class Mapper185 extends BaseMapper {

	boolean chr_enabled = true;

	public Mapper185(NES nes) {
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
			chr_map[i] = (1024 * (i + 8 * (data & 3))) & (chrsize - 1);
			// copy protection
			chr_enabled = ((chr_map[i] & 0xF) > 0 && (chr_map[i] != 0x13));
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

	@Override
	public int ppuRead(int addr) {
		if (!chr_enabled) {
			chr_enabled = true;
			return 0x12;
		}
		if (addr < 0x2000) {
			return chr[chr_map[addr >> 10] + (addr & 1023)];
		} else {
			return super.ppuRead(addr);
		}
	}
}
