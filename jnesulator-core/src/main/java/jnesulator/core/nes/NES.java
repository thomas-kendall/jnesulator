package jnesulator.core.nes;

import javafx.application.Platform;
import jnesulator.core.nes.audio.IAudioConsumer;
import jnesulator.core.nes.cheats.ActionReplay;
import jnesulator.core.nes.mapper.BadMapperException;
import jnesulator.core.nes.mapper.IMapper;
import jnesulator.core.nes.mapper.MapperLoader;
import jnesulator.core.nes.ui.FrameLimiterImpl;
import jnesulator.core.nes.ui.IController;
import jnesulator.core.nes.ui.IFrameLimiter;
import jnesulator.core.nes.video.FrameManager;

public class NES {
	private IMapper mapper;
	private APU apu;
	private CPU cpu;
	private CPURAM cpuram;
	private PPU ppu;
	private ISystemIO io;
	private IController controller1, controller2;
	public boolean runEmulation = false;
	private boolean dontSleep = false;
	private boolean shutdown = false;
	public long frameStartTime, framecount, frameDoneTime;
	private boolean frameLimiterOn = true;
	private String curRomPath, curRomName;
	private IFrameLimiter limiter = new FrameLimiterImpl(this, 16639267);
	// Pro Action Replay device
	private ActionReplay actionReplay;
	private FrameManager frameManager;

	public NES(ISystemIO io) {
		cpu = new CPU(this);
		cpuram = new CPURAM(this);
		apu = new APU(this);
		ppu = new PPU(this);
		frameManager = new FrameManager(this);

		this.io = io;
	}

	public synchronized void frameAdvance() {
		runEmulation = false;
		if (cpu != null) {
			runframe();
		}
	}

	/**
	 * Access to the Pro Action Replay device.
	 */
	public synchronized ActionReplay getActionReplay() {
		return actionReplay;
	}

	public APU getAPU() {
		return apu;
	}

	public IAudioConsumer getAudioConsumer() {
		return io.getAudioConsumer();
	}

	public IController getcontroller1() {
		return controller1;
	}

	public IController getcontroller2() {
		return controller2;
	}

	public CPU getCPU() {
		return this.cpu;
	}

	public CPURAM getCPURAM() {
		return this.cpuram;
	}

	public String getCurrentRomName() {
		return curRomName;
	}

	public FrameManager getFrameManager() {
		return frameManager;
	}

	public long getFrameTime() {
		return frameDoneTime;
	}

	public IMapper getMapper() {
		return mapper;
	}

	public PPU getPPU() {
		return ppu;
	}

	public String getrominfo() {
		if (mapper != null) {
			return mapper.getrominfo();
		}
		return null;
	}

	public ISystemIO getSystemIO() {
		return io;
	}

	public boolean isFrameLimiterOn() {
		return frameLimiterOn;
	}

	public synchronized void loadROM(String filename) {
		loadROM(filename, null);
	}

	public synchronized void loadROM(String filename, Integer initialPC) {
		runEmulation = false;
		if (FileUtils.exists(filename) && (FileUtils.getExtension(filename).equalsIgnoreCase(".nes")
				|| FileUtils.getExtension(filename).equalsIgnoreCase(".nsf"))) {
			IMapper newmapper;
			try {
				ROMLoader loader = new ROMLoader(filename);
				loader.parseHeader();
				newmapper = MapperLoader.getCorrectMapper(this, loader);
				newmapper.loadrom(loader);
			} catch (BadMapperException e) {
				io.onMessage(
						"Error Loading File: ROM is" + " corrupted or uses an unsupported mapper.\n" + e.getMessage());
				return;
			} catch (Exception e) {
				io.onMessage("Error Loading File: ROM is" + " corrupted or uses an unsupported mapper.\n" + e.toString()
						+ e.getMessage());
				e.printStackTrace();
				return;
			}
			if (mapper != null) {
				// if rom already running save its sram before closing
				apu.destroy();
				saveSRAM(false);
			}
			mapper = newmapper;
			cpuram.reset();
			cpu.reset();
			apu.reset();
			ppu.reset();
			actionReplay = new ActionReplay(cpuram);
			curRomPath = filename;
			curRomName = FileUtils.getFilenamefromPath(filename);

			framecount = 0;
			// if savestate exists, load it
			if (mapper.hasSRAM()) {
				loadSRAM();
			}
			// and start emulation
			cpu.init(initialPC);
			mapper.init();
			setParameters();
			runEmulation = true;
		} else {
			io.onMessage(
					"Could not load file:\nFile " + filename + "\n" + "does not exist or is not a valid NES game.");
		}
	}

	private void loadSRAM() {
		String name = FileUtils.stripExtension(curRomPath) + ".sav";
		if (FileUtils.exists(name) && mapper.supportsSaves()) {
			mapper.setPRGRAM(FileUtils.readfromfile(name));
		}

	}

	public void messageBox(String string) {
		if (io != null) {
			io.onMessage(string);
		}
	}

	public synchronized void pause() {
		if (apu != null) {
			apu.pause();
		}
		runEmulation = false;
	}

	public void quit() {
		// save SRAM and quit
		// should wait for any save sram workers to be done before here
		if (cpu != null && curRomPath != null) {
			runEmulation = false;
			saveSRAM(false);
		}
		// there might be some subtle threading bug with saving?
		// System.Exit is very dirty and does NOT let the delete on exit handler
		// fire so the natives stick around...
		shutdown = true;
		Platform.exit();
	}

	public synchronized void reloadROM() {
		loadROM(curRomPath);
	}

	public synchronized void reset() {
		if (cpu != null) {
			mapper.reset();
			cpu.reset();
			runEmulation = true;
			apu.pause();
			apu.resume();
		}
		// reset frame counter as well because PPU is reset
		// on Famicom, PPU is not reset when Reset is pressed
		// but some NES games expect it to be and you get garbage.
		framecount = 0;
	}

	public synchronized void resume() {
		if (apu != null) {
			apu.resume();
		}
		if (cpu != null) {
			runEmulation = true;
		}
	}

	public void run() {
		while (!shutdown) {
			if (runEmulation) {
				frameStartTime = System.nanoTime();
				actionReplay.applyPatches();
				runframe();
				if (frameLimiterOn && !dontSleep) {
					limiter.sleep();
				}
				frameDoneTime = System.nanoTime() - frameStartTime;
			} else {
				limiter.sleepFixed();
				if (ppu != null && framecount > 1) {
					// TODO: Not sure why we're trying to render here
					// io.render();
				}
			}
		}
	}

	public void run(String romtoload) {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
		// set thread priority higher than the interface thread
		curRomPath = romtoload;
		run();
	}

	private synchronized void runframe() {
		// run cpu, ppu for a whole frame
		ppu.runFrame();

		// do end of frame stuff
		dontSleep = apu.bufferHasLessThan(1000);
		// if the audio buffer is completely drained, don't sleep for this frame
		// this is to prevent the emulator from getting stuck sleeping too much
		// on a slow system or when the audio buffer runs dry.

		apu.finishframe();
		cpu.modcycles();

		// if (framecount == 13 * 60) {
		// cpu.startLog();
		// System.err.println("log on");
		// }
		// render the frame
		ppu.renderFrame(io);
		if ((framecount & 2047) == 0) {
			// save sram every 30 seconds or so
			saveSRAM(true);
		}
		++framecount;
		// System.err.println(framecount);
	}

	private void saveSRAM(boolean async) {
		if (mapper != null && mapper.hasSRAM() && mapper.supportsSaves()) {
			if (async) {
				FileUtils.asyncwritetofile(mapper.getPRGRam(), FileUtils.stripExtension(curRomPath) + ".sav");
			} else {
				FileUtils.writetofile(mapper.getPRGRam(), FileUtils.stripExtension(curRomPath) + ".sav");
			}
		}
	}

	public void setControllers(IController controller1, IController controller2) {
		this.controller1 = controller1;
		this.controller2 = controller2;
	}

	public synchronized void setParameters() {
		if (apu != null) {
			apu.setParameters();
		}
		if (ppu != null) {
			ppu.setParameters();
		}
		if (limiter != null && mapper != null) {
			switch (mapper.getTVType()) {
			case NTSC:
			default:
				limiter.setInterval(16639267);
				break;
			case PAL:
			case DENDY:
				limiter.setInterval(19997200);
			}
		}
	}

	public void toggleFrameLimiter() {
		frameLimiterOn = !frameLimiterOn;
	}
}
