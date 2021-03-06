package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;
import jnesulator.core.nes.Utils;
import jnesulator.core.nes.audio.IExpansionSoundChip;
import jnesulator.core.nes.audio.Sunsoft5BSoundChip;

public class FME7Mapper extends BaseMapper {

	private int commandRegister = 0;

	private int soundCommand = 0;
	private int[] charbanks = new int[8]; // 8 1k char rom banks
	private int[] prgbanks = new int[4]; // 4 8k prg banks - PLUS 1 8k fixed one
	private boolean ramEnable = true;
	private boolean ramSelect = false;
	private int irqcounter = 0xffff; // really needs to be unsigned but we'll
	// cheese it
	private boolean irqenabled;
	private boolean irqclock;
	private boolean hasInitSound = false;
	private IExpansionSoundChip sndchip = new Sunsoft5BSoundChip();
	private boolean interrupted = false;

	public FME7Mapper(NES nes) {
		super(nes);
	}

	@Override
	public int cartRead(int addr) {
		// five possible rom banks.
		if (addr >= 0x6000) {
			if (addr < 0x8000 && ramSelect) {
				if (ramEnable) {
					return prgram[addr - 0x6000];
				} else {
					return addr >> 8; // open bus
				}
			}
			return prg[prg_map[(addr - 0x6000) >> 10] + (addr & 1023)];
		}
		return addr >> 8; // open bus
	}

	@Override
	public void cartWrite(int addr, int data) {
		if (addr < 0x8000 || addr > 0xffff) {
			super.cartWrite(addr, data);
			return;
		}
		if (addr == 0x8000) {
			// command register
			commandRegister = data & 0xf;
		} else if (addr == 0xc000) {
			// sound command register
			soundCommand = data & 0xf;
			if (!hasInitSound) {
				// only initialize the sound chip if anything writes a sound
				// command.
				getNES().getAPU().addExpnSound(sndchip);
				hasInitSound = true;
			}
		} else if (addr == 0xa000) {
			// mapper data register
			switch (commandRegister) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				// char bank switches
				charbanks[commandRegister] = data;
				setbanks();
				break;
			case 8:
				ramEnable = ((data & (Utils.BIT7)) != 0);
				ramSelect = ((data & (Utils.BIT6)) != 0);
				prgbanks[0] = data & 0x3f;
				setbanks();
				break;
			case 9:
			case 0xa:
			case 0xb:
				// prg bank switch
				prgbanks[commandRegister - 8] = data;
				setbanks();
				break;
			case 0xc:
				// mirroring select
				switch (data & 3) {
				case 0:
					setmirroring(MirrorType.V_MIRROR);
					break;
				case 1:
					setmirroring(MirrorType.H_MIRROR);
					break;
				case 2:
					setmirroring(MirrorType.SS_MIRROR0);
					break;
				case 3:
					setmirroring(MirrorType.SS_MIRROR1);
					break;
				}
			case 0xd:
				// irq - let's put this in and hope it works
				irqclock = ((data & (Utils.BIT7)) != 0);
				// 2015-05: test by Teppples says that any value written here
				// will acknowledge a pending interrupt

				irqenabled = ((data & (Utils.BIT0)) != 0);

				if (interrupted && getNES().getCPU().interrupt > 0) {
					--getNES().getCPU().interrupt;
				}
				interrupted = false;
				// System.err.println(cpu.interrupt);
				break;
			case 0xe:
				irqcounter &= 0xff00;
				irqcounter |= data;
				break;
			case 0xf:
				irqcounter &= 0xff;
				irqcounter |= (data << 8);
				break;
			}
		} else if (addr == 0xe000) {
			sndchip.write(soundCommand, data);
		}

	}

	@Override
	public void cpucycle(int cycles) {
		if (irqclock) {
			if (irqcounter == 0) {
				irqcounter = 0xffff;
				if (irqenabled && !interrupted) {
					interrupted = true;
					++getNES().getCPU().interrupt;
					// System.err.println("FME7 Interrupt");
				}
			} else {
				--irqcounter;
			}
		}
	}

	@Override
	public void loadrom(ROMLoader loader) throws BadMapperException {
		// needs to be in every mapper. Fill with initial cfg
		super.loadrom(loader);
		// on startup:
		prg_map = new int[40]; // (trollface)

		// fixed bank maps to last 8k of rom, set everything else to last chunk
		// as well.
		for (int i = 1; i <= 40; ++i) {
			prg_map[40 - i] = prgsize - (1024 * i);
		}

		for (int i = 0; i < 8; ++i) {
			chr_map[i] = 0;
		}
	}

	private void setbanks() {
		for (int i = 0; i < 8; ++i) {
			for (int j = 0; j < 4; ++j) {
				prg_map[i + 8 * j] = (1024 * (i + (prgbanks[j] * 8))) % prgsize;
			}
		}
		for (int i = 0; i < 8; ++i) {
			chr_map[i] = (1024 * charbanks[i]) % chrsize;
		}
	}
}
