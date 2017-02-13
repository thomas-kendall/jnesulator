package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class VRC2Mapper extends BaseMapper {
	// vrc2a mapper(INES #22); vrc2b is mapped to 23 along with the one form of
	// vrc4

	int prgbank0, prgbank1 = 0;

	int[] chrbank = { 0, 0, 0, 0, 0, 0, 0, 0 };

	public VRC2Mapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		boolean bit0 = ((addr & (Utils.BIT1)) != 0);
		boolean bit1 = ((addr & (Utils.BIT0)) != 0);
		switch (addr >> 12) {
		case 0x8:
			prgbank0 = data & 0xf;
			break;
		case 0x9:
			// mirroring
			switch (data & 1) {
			case 0:
				setmirroring(MirrorType.V_MIRROR);
				break;
			case 1:
				setmirroring(MirrorType.H_MIRROR);
				break;
			}
			// 4-4-2016: seems VRC2 only has 1 mirroring bit
			break;
		case 0xa:
			prgbank1 = data & 0xf;
			break;
		case 0xb:
		case 0xc:
		case 0xd:
		case 0xe:
			// chr bank select. black magic
			data &= 0xf;
			int whichreg = ((addr - 0xb000) >> 11) + ((bit1) ? 1 : 0);
			int oldval = chrbank[whichreg];
			if (!bit0) {
				oldval &= 0xf0;
				oldval |= data;
			} else {
				oldval &= 0xf;
				oldval |= (data << 4);
			}
			chrbank[whichreg] = oldval;
			break;
		}

		if (addr < 0xf000) {
			setbanks();
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		for (int i = 1; i <= 32; ++i) {
			// map last banks in to start off
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * i) & (chrsize - 1);
		}
	}

	private void setbanks() {
		// map prg banks
		// last 2 banks fixed to last two in rom
		for (int i = 1; i <= 16; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		// first bank set to prg0 register
		for (int i = 0; i < 8; ++i) {
			prg_map[i] = (1024 * (i + 8 * prgbank0)) % prgsize;
		}
		// second bank set to prg1 register
		for (int i = 0; i < 8; ++i) {
			prg_map[i + 8] = (1024 * (i + 8 * prgbank1)) % prgsize;
		}

		// map chr banks
		for (int i = 0; i < 8; ++i) {
			setppubank(1, i, chrbank[i] >> 1);
		}

	}

	private void setppubank(int banksize, int bankpos, int banknum) {
		// System.err.println(banksize + ", " + bankpos + ", "+ banknum);
		for (int i = 0; i < banksize; ++i) {
			chr_map[i + bankpos] = (1024 * ((banknum) + i)) % chrsize;
		}
		// utils.printarray(chr_map);
	}
}
