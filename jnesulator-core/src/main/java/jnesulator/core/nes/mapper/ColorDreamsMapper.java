package jnesulator.core.nes.mapper;

public class ColorDreamsMapper extends Mapper {

	@Override
	public final void cartWrite(final int addr, final int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		final int prgselect = data & 0xf;
		final int chrselect = data >> 4;

		// remap CHR bank
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * (i + 8 * chrselect)) & (chrsize - 1);
		}
		// remap PRG bank
		for (int i = 0; i < 32; ++i) {
			prg_map[i] = (1024 * (i + 32 * prgselect)) & (prgsize - 1);
		}
	}

	@Override
	public void loadrom() throws BadMapperException {
		// needs to be in every mapper. Fill with initial cfg
		super.loadrom();
		for (int i = 1; i <= 32; ++i) {
			prg_map[32 - i] = prgsize - (1024 * i);
		}
		for (int i = 1; i <= 8; ++i) {
			chr_map[8 - i] = chrsize - (1024 * i);
		}
	}
}
