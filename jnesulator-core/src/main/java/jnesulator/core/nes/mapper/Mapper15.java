package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;

public class Mapper15 extends BaseMapper {

	public Mapper15(NES nes) {
		super(nes);
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}

		int prgbank = (data << 1) & 0xFE;
		int prgflip = data >> 7;
		setmirroring(((data & (Utils.BIT6)) != 0) ? MirrorType.H_MIRROR : MirrorType.V_MIRROR);

		switch (addr & 0xFFF) {
		case 0x000:
			for (int i = 0; i < 8; ++i) {
				prg_map[i] = (1024 * (i + 8 * (prgbank | 0 ^ prgflip))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[8 + i] = (1024 * (i + 8 * (prgbank | 1 ^ prgflip))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[16 + i] = (1024 * (i + 8 * (prgbank | 2 ^ prgflip))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[24 + i] = (1024 * (i + 8 * (prgbank | 3 ^ prgflip))) & (prgsize - 1);
			}
			break;
		case 0x001:
			for (int i = 0; i < 8; ++i) {
				prg_map[i] = (1024 * (i + 8 * (prgbank | (0 ^ prgflip)))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[8 + i] = (1024 * (i + 8 * (prgbank | (1 ^ prgflip)))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[16 + i] = (1024 * (i + 8 * (0x7E | (0 ^ prgflip)))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[24 + i] = (1024 * (i + 8 * (0x7F | (1 ^ prgflip)))) & (prgsize - 1);
			}
			break;
		case 0x002:
			prgbank |= prgflip;

			for (int i = 0; i < 8; ++i) {
				prg_map[i] = (1024 * (i + 8 * prgbank)) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[8 + i] = (1024 * (i + 8 * prgbank)) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[16 + i] = (1024 * (i + 8 * prgbank)) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[24 + i] = (1024 * (i + 8 * prgbank)) & (prgsize - 1);
			}
			break;
		case 0x003:
			prgbank |= prgflip;

			for (int i = 0; i < 8; ++i) {
				prg_map[i] = (1024 * (i + 8 * prgbank)) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[8 + i] = (1024 * (i + 8 * (prgbank + 1))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[16 + i] = (1024 * (i + 8 * (prgbank + (~addr >> 1 & 1)))) & (prgsize - 1);
			}
			for (int i = 0; i < 8; ++i) {
				prg_map[24 + i] = (1024 * (i + 8 * (prgbank + 1))) & (prgsize - 1);
			}
			break;
		default:
			break;
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
