package jnesulator.core.nes.ui;

import java.awt.image.BufferedImage;

import jnesulator.core.nes.CPURAM;
import jnesulator.core.nes.NES;
import jnesulator.core.nes.video.RGBRenderer;
import jnesulator.core.nes.video.Renderer;

public class HeadlessUI implements GUIInterface {

	private NES nes;
	private Renderer renderer;
	private boolean renderFrames;
	private BufferedImage lastFrame = null;
	private boolean updateImage;
	private PuppetController controller1, controller2;

	public HeadlessUI(String romToLoad, boolean renderFrames) {
		nes = new NES(this);
		this.loadROM(romToLoad);
		this.renderer = new RGBRenderer();
		this.controller1 = new PuppetController();
		this.controller2 = new PuppetController();
		nes.setControllers(this.controller1, this.controller2);
		this.renderFrames = renderFrames;
	}

	public PuppetController getController1() {
		return controller1;
	}

	public PuppetController getController2() {
		return controller2;
	}

	public BufferedImage getLastFrame() {
		return lastFrame;
	}

	@Override
	public NES getNes() {
		return nes;
	}

	public CPURAM getNESCPURAM() {
		return nes.getCPURAM();
	}

	public void loadROM(String romToLoad) {
		this.nes.loadROM(romToLoad);
	}

	@Override
	public void loadROMs(String path) {
	}

	@Override
	public void messageBox(String message) {
		System.err.println(message); // Shouldn't get any messages except errors
	}

	@Override
	public void render() {
	}

	@Override
	public void run() {
		// Null-op
	}

	public synchronized void runFrame() {
		nes.frameAdvance();
	}

	@Override
	public void setFrame(int[] frame, int[] bgcolor, boolean dotcrawl) {
		if (renderFrames) {
			this.lastFrame = renderer.render(frame, bgcolor, dotcrawl);
		}
	}

	@Override
	public void setNES(NES nes) {
		this.nes = nes;
	}
}
