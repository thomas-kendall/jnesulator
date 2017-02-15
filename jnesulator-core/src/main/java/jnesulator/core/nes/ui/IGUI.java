package jnesulator.core.nes.ui;

import java.awt.image.BufferedImage;

// TODO: Remove this and use something more appropriate, like ISystemIO. Also decouple it from Runnable.
public interface IGUI {

	void messageBox(String message);

	void render(BufferedImage frame);
}
