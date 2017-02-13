package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper226 extends BaseMapper {

	int[] reg = { 0, 0 };

	public Mapper226(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xFFFF) {
			super.cartWrite(addr, data);
			return;
		}

		reg[addr & 1] = data;

		int bank = ((reg[0] >> 1 & 0x0F) | (reg[0] >> 3 & 0x10) | (reg[1] << 5 & 0x20));

		setmirroring(((reg[0] & (Utils.BIT6)) != 0) ? MirrorType.V_MIRROR : MirrorType.H_MIRROR);

		if ((reg[0] & 0x20) != 0) {
			bank = (bank << 1) | (reg[0] & 1);
			for (int i = 0; i < 16; ++i) {
				prg_map[i] = (1024 * (i + 16 * bank)) & (prgsize - 1);
			}
			for (int i = 0; i < 16; ++i) {
				prg_map[i + 16] = (1024 * (i + 16 * bank)) & (prgsize - 1);
			}
		} else {
			for (int i = 0; i < 32; ++i) {
				prg_map[i] = (1024 * (i + 32 * bank)) & (prgsize - 1);
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
