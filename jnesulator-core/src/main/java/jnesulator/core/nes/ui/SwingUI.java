package jnesulator.core.nes.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import jnesulator.core.nes.FileUtils;
import jnesulator.core.nes.NES;
import jnesulator.core.nes.PrefsSingleton;
import jnesulator.core.nes.audio.IAudioConsumer;
import jnesulator.core.nes.audio.SwingAudioImpl;
import jnesulator.core.nes.cheats.ActionReplay;
import jnesulator.core.nes.cheats.ActionReplayGui;

public class SwingUI extends JFrame implements IGUI, Runnable {

	public class AL implements ActionListener, WindowListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// placeholder for more robust handler
			if (arg0.getActionCommand().equals("Quit")) {
				close();
			} else if (arg0.getActionCommand().equals("Reset")) {
				nes.reset();
			} else if (arg0.getActionCommand().equals("Hard Reset")) {
				nes.reloadROM();
			} else if (arg0.getActionCommand().equals("Pause")) {
				nes.pause();
			} else if (arg0.getActionCommand().equals("Resume")) {
				nes.resume();
			} else if (arg0.getActionCommand().equals("Preferences")) {
				showOptions();
			} else if (arg0.getActionCommand().equals("Fast Forward")) {
				nes.toggleFrameLimiter();
			} else if (arg0.getActionCommand().equals("About")) {
				messageBox("jnesulator\n"
						+ "Get the latest version and report any bugs at https://github.com/thomas-kendall/jnesulator \n"
						+ "\n" + "This program is free software licensed under the GPL version 3, and comes with \n"
						+ "NO WARRANTY of any kind. (but if something's broken, please report it). \n"
						+ "See the license.txt file for details.");
			} else if (arg0.getActionCommand().equals("ROM Info")) {
				String info = nes.getrominfo();
				if (info != null) {
					messageBox(info);
				}
			} else if (arg0.getActionCommand().equals("Open ROM")) {
				loadROM();
			} else if (arg0.getActionCommand().equals("Toggle Fullscreen")) {
				toggleFullScreen();
			} else if (arg0.getActionCommand().equals("Frame Advance")) {
				nes.frameAdvance();
			} else if (arg0.getActionCommand().equals("Escape")) {
				if (inFullScreen) {
					toggleFullScreen();
				} else {
					close();
				}
			} else if (arg0.getActionCommand().equals("Controller Settings")) {
				showControlsDialog();
			} else if (arg0.getActionCommand().equals("Cheat Codes")) {
				showActionReplayDialog();
			}
		}

		private void close() {
			dispose();
			savewindowposition();
			padController1.stopEventQueue();
			padController2.stopEventQueue();
			nes.quit();
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// we don't care about these events
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close();
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			// so we can use the interface.
		}

		@Override
		public void windowIconified(WindowEvent e) {
			// but java wants us to implement something for all of them
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}
	}

	private static final long serialVersionUID = 6411494245530679723L;
	private Canvas canvas;
	private BufferStrategy buffer;
	private NES nes;
	private AL listener = new AL();
	private int screenScaleFactor;
	private boolean smoothScale, inFullScreen = false;
	private GraphicsDevice gd;

	private ControllerImpl padController1, padController2;

	int bgcolor;

	public SwingUI(String[] args) {
		IAudioConsumer audioConsumer = new SwingAudioImpl();
		nes = new NES(this, audioConsumer);
		screenScaleFactor = PrefsSingleton.get().getInt("screenScaling", 2);
		padController1 = new ControllerImpl(this, 0);
		padController2 = new ControllerImpl(this, 1);
		nes.setControllers(padController1, padController2);
		padController1.startEventQueue();
		padController2.startEventQueue();
		this.run();
		if (args == null || args.length < 1 || args[0] == null) {
			nes.run();
		} else {
			nes.run(args[0]);
		}

	}

	public void buildMenus() {
		JMenuBar menus = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem item;
		file.add(item = new JMenuItem("Open ROM"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		file.addSeparator();

		file.add(item = new JMenuItem("Preferences"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		file.addSeparator();

		file.add(item = new JMenuItem("Toggle Fullscreen"));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		item.addActionListener(listener);
		menus.add(file);

		file.add(item = new JMenuItem("Quit"));
		item.addActionListener(listener);
		menus.add(file);

		JMenu nesmenu = new JMenu("NES");
		nesmenu.add(item = new JMenuItem("Reset"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		nesmenu.add(item = new JMenuItem("Hard Reset"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		nesmenu.add(item = new JMenuItem("Pause"));
		item.addActionListener(listener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

		nesmenu.add(item = new JMenuItem("Resume"));
		item.addActionListener(listener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		nesmenu.add(item = new JMenuItem("Fast Forward"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		nesmenu.add(item = new JMenuItem("Frame Advance"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		nesmenu.addSeparator();

		nesmenu.add(item = new JMenuItem("Controller Settings"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		nesmenu.add(item = new JMenuItem("Cheat Codes"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_F10, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		nesmenu.addSeparator();

		nesmenu.add(item = new JMenuItem("ROM Info"));
		item.addActionListener(listener);
		item.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		menus.add(nesmenu);

		JMenu help = new JMenu("Help");
		help.add(item = new JMenuItem("About"));
		item.addActionListener(listener);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menus.add(help);
		this.setJMenuBar(menus);
	}

	private File extractRomFromZip(String zipName, String romName) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipName));
		ZipEntry entry;
		do {
			entry = zipStream.getNextEntry();
		} while ((entry != null) && (!entry.getName().equals(romName)));
		if (entry == null) {
			zipStream.close();
			throw new IOException("Cannot find file " + romName + " inside archive " + zipName);
		}
		// name temp. extracted file after parent zip and file inside

		// note: here's the bug, when it saves the temp file if it's in a folder
		// in the zip it's trying to put it in the same folder outside the zip
		File outputFile = new File(new File(zipName).getParent() + File.separator
				+ FileUtils.stripExtension(new File(zipName).getName()) + " - " + romName);
		if (outputFile.exists()) {
			this.messageBox("Cannot extract file. File " + outputFile.getCanonicalPath() + " already exists.");
			zipStream.close();
			return null;
		}
		byte[] buf = new byte[4096];
		FileOutputStream fos = new FileOutputStream(outputFile);
		int numBytes;
		while ((numBytes = zipStream.read(buf, 0, buf.length)) != -1) {
			fos.write(buf, 0, numBytes);
		}
		zipStream.close();
		fos.close();
		return outputFile;
	}

	private double getmaxscale(int width, int height) {
		return Math.min(height / (double) nes.getFrameManager().getHeight(),
				width / (double) nes.getFrameManager().getWidth());
	}

	private List<String> listRomsInZip(String zipName) throws IOException {
		ZipFile zipFile = new ZipFile(zipName);
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		List<String> romNames = new ArrayList<>();
		while (zipEntries.hasMoreElements()) {
			ZipEntry entry = zipEntries.nextElement();
			if (!entry.isDirectory() && (entry.getName().endsWith(".nes") || entry.getName().endsWith(".fds")
					|| entry.getName().endsWith(".nsf"))) {
				romNames.add(entry.getName());
			}
		}
		zipFile.close();
		if (romNames.isEmpty()) {
			throw new IOException("No NES games found in ZIP file.");
		}
		return romNames;
	}

	public void loadROM() {
		FileDialog fileDialog = new FileDialog(this);
		fileDialog.setMode(FileDialog.LOAD);
		fileDialog.setTitle("Select a ROM to load");
		// should open last folder used, and if that doesn't exist, the folder
		// it's running in
		String path = PrefsSingleton.get().get("filePath", System.getProperty("user.dir", ""));
		File startDirectory = new File(path);
		if (startDirectory.isDirectory()) {
			fileDialog.setDirectory(path);
		}
		// and if the last path used doesn't exist don't set the directory at
		// all
		// and hopefully the jFileChooser will open somewhere usable
		// on Windows it does - on Mac probably not.
		fileDialog.setFilenameFilter(new NESFileFilter());
		boolean wasInFullScreen = false;
		if (inFullScreen) {
			wasInFullScreen = true;
			// load dialog won't show if we are in full screen, so this fixes
			// for now.
			toggleFullScreen();
		}
		fileDialog.setVisible(true);
		if (fileDialog.getFile() != null) {
			PrefsSingleton.get().put("filePath", fileDialog.getDirectory());
			loadROM(fileDialog.getDirectory() + fileDialog.getFile());
		}
		if (wasInFullScreen) {
			toggleFullScreen();
		}
	}

	private void loadROM(String path) {
		if (path.endsWith(".zip") || path.endsWith(".ZIP")) {
			try {
				loadRomFromZip(path);
			} catch (IOException ex) {
				this.messageBox(
						"Could not load file:\nFile does not exist or is not a valid NES game.\n" + ex.getMessage());
			}
		} else {
			nes.loadROM(path);
		}
	}

	private void loadRomFromZip(String zipName) throws IOException {
		String romName = selectRomInZip(listRomsInZip(zipName));
		if (romName != null) {
			File extractedFile = extractRomFromZip(zipName, romName);
			if (extractedFile != null) {
				extractedFile.deleteOnExit();
				nes.loadROM(extractedFile.getCanonicalPath());
			}
		}
	}

	@Override
	public void messageBox(String message) {
		JOptionPane.showMessageDialog(this, message);
	}

	@Override
	public synchronized void render(BufferedImage frame) {
		Graphics graphics = buffer.getDrawGraphics();
		if (smoothScale) {
			((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
		if (inFullScreen) {
			graphics.setColor(Color.BLACK);
			DisplayMode dm = gd.getDisplayMode();
			int scrnheight = dm.getHeight();
			int scrnwidth = dm.getWidth();
			// don't ask why this needs to be done every frame,
			// but it does b/c the canvas keeps resizing itself
			canvas.setSize(scrnwidth, scrnheight);
			graphics.fillRect(0, 0, scrnwidth, scrnheight);
			if (PrefsSingleton.get().getBoolean("maintainAspect", true)) {
				double scalefactor = getmaxscale(scrnwidth, scrnheight);
				int height = (int) (nes.getFrameManager().getHeight() * scalefactor);
				int width = (int) (256 * scalefactor * 1.1666667);
				graphics.drawImage(frame, ((scrnwidth / 2) - (width / 2)), ((scrnheight / 2) - (height / 2)), width,
						height, null);
			} else {
				graphics.drawImage(frame, 0, 0, scrnwidth, scrnheight, null);
			}
			graphics.setColor(Color.DARK_GRAY);
			graphics.drawString(this.getTitle(), 16, 16);

		} else {
			graphics.drawImage(frame, 0, 0, nes.getFrameManager().getWidth() * screenScaleFactor,
					nes.getFrameManager().getHeight() * screenScaleFactor, null);
		}

		graphics.dispose();
		buffer.show();

	}

	@Override
	public synchronized void run() {
		// construct window
		this.setTitle("jnesulator");
		this.setResizable(false);
		buildMenus();
		setRenderOptions();
		this.getRootPane().registerKeyboardAction(listener, "Escape", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		this.getRootPane().registerKeyboardAction(listener, "Toggle Fullscreen",
				KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		this.getRootPane().registerKeyboardAction(listener, "Quit",
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		this.setLocation(PrefsSingleton.get().getInt("windowX", 0), PrefsSingleton.get().getInt("windowY", 0));
		this.addWindowListener(listener);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.setVisible(true);
		// Create BackBuffer

		// now add the drag and drop handler.
		TransferHandler handler = new TransferHandler() {
			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					return false;
				}

				return true;
			}

			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support)) {
					return false;
				}
				Transferable t = support.getTransferable();
				try {
					// holy typecasting batman (this interface predates
					// generics)
					File toload = (File) ((java.util.List) t.getTransferData(DataFlavor.javaFileListFlavor)).get(0);
					loadROM(toload.getCanonicalPath());
				} catch (UnsupportedFlavorException e) {
					return false;
				} catch (IOException e) {
					return false;
				}
				return true;
			}
		};
		this.setTransferHandler(handler);
	}

	public void savewindowposition() {
		PrefsSingleton.get().putInt("windowX", this.getX());
		PrefsSingleton.get().putInt("windowY", this.getY());
	}

	private String selectRomInZip(List<String> romNames) {
		if (romNames.size() > 1) {
			return (String) JOptionPane.showInputDialog(this, "Select ROM to load", "Select ROM to load",
					JOptionPane.PLAIN_MESSAGE, null, romNames.toArray(), romNames.get(0));
		} else if (romNames.size() == 1) {
			return romNames.get(0);
		}
		return null;
	}

	public synchronized void setRenderOptions() {
		if (canvas != null) {
			this.remove(canvas);
		}
		screenScaleFactor = PrefsSingleton.get().getInt("screenScaling", 2);
		smoothScale = PrefsSingleton.get().getBoolean("smoothScaling", false);

		// Create canvas for painting
		canvas = new Canvas();
		canvas.setSize(nes.getFrameManager().getWidth() * screenScaleFactor,
				nes.getFrameManager().getHeight() * screenScaleFactor);
		canvas.setEnabled(false); // otherwise it steals input events.
		// Add canvas to game window
		this.add(canvas);
		this.pack();
		canvas.createBufferStrategy(2);
		buffer = canvas.getBufferStrategy();
	}

	private void showActionReplayDialog() {
		nes.pause();
		ActionReplay actionReplay = nes.getActionReplay();
		if (actionReplay != null) {
			ActionReplayGui dialog = new ActionReplayGui(this, false, actionReplay);
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "You have to load a game first.", "No ROM", JOptionPane.ERROR_MESSAGE);
		}
		nes.resume();
	}

	private void showControlsDialog() {
		ControlsDialog dialog = new ControlsDialog(this);
		dialog.setVisible(true);
		if (dialog.okClicked()) {
			padController1.setButtons();
			padController2.setButtons();
		}
	}

	private void showOptions() {
		PreferencesDialog dialog = new PreferencesDialog(this);
		dialog.setVisible(true);
		if (dialog.okClicked()) {
			setRenderOptions();
			nes.setParameters();
		}
	}

	public synchronized void toggleFullScreen() {
		if (inFullScreen) {
			this.dispose();
			gd.setFullScreenWindow(null);
			canvas.setSize(nes.getFrameManager().getHeight() * screenScaleFactor,
					nes.getFrameManager().getWidth() * screenScaleFactor);
			this.setUndecorated(false);
			this.setVisible(true);
			inFullScreen = false;
			buildMenus();
			// nes.resume();
		} else {
			setJMenuBar(null);
			gd = getGraphicsConfiguration().getDevice();
			if (!gd.isFullScreenSupported()) {
				// then fullscreen will give a window the size of the screen
				// instead
				messageBox("Fullscreen is not supported by your OS or version of Java.");
			}
			this.dispose();
			this.setUndecorated(true);

			gd.setFullScreenWindow(this);
			this.setVisible(true);

			inFullScreen = true;
		}
	}
}
