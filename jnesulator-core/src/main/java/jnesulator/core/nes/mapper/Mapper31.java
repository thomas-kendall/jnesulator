package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper31 extends BaseMapper {

	// mapper for NSF compilations, based on BNROM with NSF type bankswitch
	// written in about 20mins so i could listen to 2a03puritans
	public boolean nsfBanking;

	public int[] nsfBanks = { 00, 00, 00, 00, 00, 00, 00, 0xff };

	public Mapper31(NES nes) {
		super(nes);
	}

	@Override
	public int cartRead(int addr) {
		// by default has wram at 0x6000 and cartridge at 0x8000-0xfff
		// but some mappers have different so override for those
		if (addr >= 0x8000) {

			int fuuu = prg_map[((addr & 0x7fff)) >> 10] + (addr & 1023);
			return prg[fuuu];
		} else if (addr >= 0x6000 && hasprgram) {

			return prgram[addr & 0x1fff];
		} else if ((addr >= 0x5000)) {
			return nsfBanks[addr & 7];
		}
		System.err.println("reading open bus " + Utils.hex(addr));
		return addr >> 8; // open bus
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr >= 0x6000 && addr < 0x8000) {
			// default no-mapper operation just writes if in PRG RAM range
			prgram[addr & 0x1fff] = data;
		} else if ((addr >= 0x5000) && (addr < 0x6000)) {
			nsfBanks[addr & 7] = data;
			// System.err.println(addr - 0x5ff8 + " " + data);
			setBanks();
		} else {
			System.err.println("write to " + Utils.hex(addr) + " goes nowhere");
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		super.loadrom(loader);
		setBanks();
	}

	private void setBanks() {
		for (int i = 0; i < prg_map.length; ++i) {
			prg_map[i] = (4096 * nsfBanks[i / 4]) + (1024 * (i % 4));
			if ((prg_map[i]) >= prg.length) {
				// System.err.println("broken banks");
				prg_map[i] &= ((this.prgsize) - 1);
			}
		}
	}

}
