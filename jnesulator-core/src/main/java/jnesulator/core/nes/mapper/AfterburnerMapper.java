package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class AfterburnerMapper extends BaseMapper {
	// the Afterburner mapper is special in that it uses ROM name tables

	private int bank = 0x0;

	boolean useromnt = false;
	int romnt1, romnt2;

	public AfterburnerMapper(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		} else if (addr <= 0x8fff) {
			setppubank(2, 0, data);
		} else if (addr <= 0x9fff) {
			setppubank(2, 2, data);
		} else if (addr <= 0xafff) {
			setppubank(2, 4, data);
		} else if (addr <= 0xbfff) {
			setppubank(2, 6, data);
		} else if (addr <= 0xcfff) {
			romnt1 = data | 0x80;
		} else if (addr <= 0xdfff) {
			romnt2 = data | 0x80;
		} else if (addr <= 0xefff) {
			useromnt = ((data & (Utils.BIT4)) != 0);
			setmirroring(((data & (Utils.BIT0)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
		} else if (addr <= 0xffff) {
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

	@Override
	public int ppuRead(int addr) {
		if (addr < 0x2000) {
			return chr[chr_map[addr >> 10] + (addr & 1023)];
		} else {
			switch (addr & 0xc00) {
			case 0:
				return (useromnt ? chr[(addr & 0x3ff) + (romnt1 * 1024)] : nt0[addr & 0x3ff]);
			case 0x400:
				return (useromnt ? chr[(addr & 0x3ff) + (romnt2 * 1024)] : nt1[addr & 0x3ff]);
			case 0x800:
				return (useromnt ? chr[(addr & 0x3ff) + (romnt2 * 1024)] : nt2[addr & 0x3ff]);
			case 0xc00:
			default:
				if (addr >= 0x3f00) {
					addr &= 0x1f;
					if (addr >= 0x10 && ((addr & 3) == 0)) {
						addr -= 0x10;
					}
					return getNES().getPPU().pal[addr];
				} else {
					return (useromnt ? chr[(addr & 0x3ff) + (romnt1 * 1024)] : nt3[addr & 0x3ff]);
				}
			}
		}
	}

	private void setppubank(int banksize, int bankpos, int banknum) {
		// System.err.println(banksize + ", " + bankpos + ", "+ banknum);
		for (int i = 0; i < banksize; ++i) {
			chr_map[i + bankpos] = (1024 * ((banksize * banknum) + i)) % chrsize;
		}
	}
}
