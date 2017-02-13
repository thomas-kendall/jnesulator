package jnesulator.core.nes.mapper;

import jnesulator.core.nes.ROMLoader;

public interface IMapper {
	int cartRead(int addr);

	void cartWrite(int addr, int data);

	void checkA12(int addr);

	void cpucycle(int cycles);

	int[] getPRGRam();

	String getrominfo();

	TVType getTVType();

	boolean hasSRAM();

	void init();

	void loadrom(ROMLoader loader) throws BadMapperException;

	void notifyscanline(int scanline);

	int ppuRead(int addr);

	void ppuWrite(int addr, int data);

	void reset();

	void setPRGRAM(int[] newprgram);

	boolean supportsSaves();
}
