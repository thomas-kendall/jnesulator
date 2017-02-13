package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class CodemastersMapper extends BaseMapper {

	private int bank = 0x0;

	public CodemastersMapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		if (addr < 0xc000) {
			if (crc == 0x1BC686A8L) {
				// fire hawk is only game with mapper controlled mirroring
				// micro machines glitches hard if this is on
				setmirroring((((data & (Utils.BIT4)) != 0) ? MirrorType.SS_MIRROR1 : MirrorType.SS_MIRROR0));
			}
		} else {
			bank = data & 0xf;
			// remap PRG bank (1st bank switchable, 2nd bank mapped to LAST
			// bank)
			for (int i = 0; i < 16; ++i) {
				prg_map[i] = (1024 * (i + 16 * bank)) & (prgsize - 1);
			}
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		// needs to be in every mapper. Fill with initial cfg
		super.loadrom(loader);
		// movable bank, should really be random. eh, effort
		for (int i = 0; i < 16; ++i) {
			prg_map[i] = (1024 * i) & (prgsize - 1);
		}
		// fixed bank
		for (int i = 1; i <= 16; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}
}
