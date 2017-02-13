package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Action52Mapper extends BaseMapper {

	int[] ram = new int[4];

	int prgchip = 0;
	int prgpage = 0;
	int chrpage = 0;
	boolean prgmode = false;

	public Action52Mapper(NES nes) {
		super(nes);
	}

	@Override
	public int cartRead(int addr) {
		// by default has wram at 0x6000 and cartridge at 0x8000-0xfff
		// but some mappers have different so override for those
		if (addr >= 0x8000) {
			return prg[prg_map[((addr & 0x7fff)) >> 10] + (addr & 1023)];
		} else if (addr < 0x6000) {
			return ram[addr & 3] & 0xf;
		}
		return addr >> 8; // open bus
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr <= 0x5fff) {
			ram[addr & 3] = data & 0xf;
		} else if (addr >= 0x8000) {
			chrpage = ((addr & 0xf) << 2) + (data & 3);
			prgmode = ((addr & (Utils.BIT5)) != 0);
			prgpage = (addr >> 6) & 0x1f;
			prgchip = (addr >> 11) & 3;
			setmirroring((((addr & (Utils.BIT13)) != 0)) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);
			for (int i = 0; i < 8; ++i) {
				chr_map[i] = (1024 * (chrpage * 8 + i)) % chrsize;
			}
			int off = 0;
			switch (prgchip) {
			case 0:
				off = 0;
				break;
			case 1:
				off = 0x080000;
				break;
			case 3:
				off = 0x100000;
				break;
			default:
				System.err.println("Who knows.");
			}
			if (prgmode) {
				for (int i = 0; i < 16; ++i) {
					prg_map[i] = ((1024 * ((16 * prgpage) + i)) + off) % prgsize;
					prg_map[i + 16] = ((1024 * ((16 * prgpage) + i)) + off) % prgsize;
				}
			} else {
				for (int i = 0; i < 32; ++i) {
					prg_map[i] = ((1024 * ((32 * (prgpage >> 1)) + i)) + off) % prgsize;
				}
			}
		}
	}

	@Override
	public void loadrom(ROMLoader romLoader) throws BadMapperException {
		super.loadrom(romLoader);
		cartWrite(0x8000, 0);
	}

	@Override
	public void reset() {
		cartWrite(0x8000, 0);
	}
}
