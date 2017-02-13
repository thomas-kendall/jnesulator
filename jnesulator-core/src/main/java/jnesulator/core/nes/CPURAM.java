package jnesulator.core.nes;

import java.util.Arrays;
import java.util.HashMap;

import jnesulator.core.nes.cheats.Patch;

public class CPURAM {
	private NES nes;
	private int[] wram = new int[2048];
	private HashMap<Integer, Patch> patches = new HashMap<>();

	public CPURAM(NES nes) {
		this.nes = nes;
	}

	public int _read(int addr) {
		if (addr > 0x4018) {
			return nes.getMapper().cartRead(addr);
		} else if (addr <= 0x1fff) {
			return wram[addr & 0x7FF];
		} else if (addr <= 0x3fff) {
			// 8 byte ppu regs; mirrored lots
			return nes.getPPU().read(addr & 7);
		} else if (0x4000 <= addr && addr <= 0x4018) {
			return nes.getAPU().read(addr - 0x4000);
		} else {
			return addr >> 8; // open bus
		}
	}

	public int read(int addr) {
		if (!patches.isEmpty()) {
			int retval = _read(addr);
			Patch p = patches.get(addr);
			if (p != null && p.getAddress() == addr && p.matchesData(retval)) {
				return p.getData();
			}
			return retval;
		} else {
			return _read(addr);
		}
	}

	public void reset() {
		// init memory
		Arrays.fill(wram, 0xff);
	}

	public void setPatches(HashMap<Integer, Patch> p) {
		this.patches = p;
	}

	public void write(int addr, int data) {
		// if((data & 0xff) != data){
		// System.err.println("DANGER WILL ROBINSON");
		// }
		if (addr > 0x4018) {
			nes.getMapper().cartWrite(addr, data);
		} else if (addr <= 0x1fff) {
			wram[addr & 0x7FF] = data;
		} else if (addr <= 0x3fff) {
			// 8 byte ppu regs; mirrored lots
			nes.getPPU().write(addr & 7, data);
		} else if (0x4000 <= addr && addr <= 0x4018) {
			nes.getAPU().write(addr - 0x4000, data);
		}
	}
}
