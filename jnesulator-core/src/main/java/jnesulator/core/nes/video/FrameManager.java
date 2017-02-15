package jnesulator.core.nes.video;

import java.awt.image.BufferedImage;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.PrefsSingleton;

public class FrameManager {
	private NES nes;
	private Renderer renderer;

	private long[] frametimes = new long[60];
	private int frametimeptr = 0;
	private int frameskip = 0;
	private BufferedImage frame;
	private double fps;

	private int NES_HEIGHT, NES_WIDTH;

	public FrameManager(NES nes) {
		this.nes = nes;
		createRenderer();
	}

	private void createRenderer() {
		if (PrefsSingleton.get().getBoolean("TVEmulation", false)) {
			renderer = new NTSCRenderer();
			NES_WIDTH = 302;
		} else {
			renderer = new RGBRenderer();
			NES_WIDTH = 256;
		}
		if (PrefsSingleton.get().getInt("region", 0) > 1) {
			NES_HEIGHT = 240;
			renderer.setClip(0);
		} else {
			NES_HEIGHT = 224;
			renderer.setClip(8);
		}
	}

	public int getHeight() {
		return NES_HEIGHT;
	}

	public int getWidth() {
		return NES_WIDTH;
	}

	public synchronized void setFrame(int[] nextframe, int[] bgcolors, boolean dotcrawl) {
		// todo: stop running video filters while paused!
		// also move video filters into a worker thread because they
		// don't really depend on emulation state at all. Yes this is going to
		// cause more lag but it will hopefully get back up to playable speed
		// with NTSC filter

		frametimes[frametimeptr] = nes.getFrameTime();
		++frametimeptr;
		frametimeptr %= frametimes.length;

		if (frametimeptr == 0) {
			long averageframes = 0;
			for (long l : frametimes) {
				averageframes += l;
			}
			averageframes /= frametimes.length;
			fps = 1E9 / averageframes;
			// this.setTitle(
			// String.format("jnesulator - %s, %2.2f fps" + ((frameskip > 0) ? "
			// frameskip " + frameskip : ""),
			// nes.getCurrentRomName(), fps));
		}
		if (nes.framecount % (frameskip + 1) == 0) {
			frame = renderer.render(nextframe, bgcolors, dotcrawl);
			nes.getGUI().render(frame);
		}
	}
}
