package jnesulator.core.nes.mapper;

import java.util.zip.CRC32;

public class CyclicRedundancyCheck {
	public static long crc32(int[] array) {
		CRC32 c = new CRC32();
		for (int i : array) {
			c.update(i);
		}
		return c.getValue();
	}
}
