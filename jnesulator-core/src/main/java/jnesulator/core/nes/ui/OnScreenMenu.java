package jnesulator.core.nes.ui;

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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import jnesulator.core.nes.FileUtils;

public class OnScreenMenu extends StackPane {

	class GameAction extends MenuAction {

		GameAction(File game) {
			name = game.getName();
			action = () -> {
				try {
					gui.getNes().loadROM(game.getCanonicalPath());
					Platform.runLater(() -> {
						gameMenu.setVisible(false);
						menu.setVisible(false);
					});
				} catch (IOException e) {
					gui.messageBox(e.getMessage());
				}
			};
		}

		GameAction(final String zipName, final String romName) {
			if (romName.toLowerCase().endsWith(".nes")) {
				name = romName.substring(0, romName.length() - 4);
			} else {
				name = romName;
			}
			action = () -> {
				try {
					final File extractedFile = extractRomFromZip(zipName, romName);
					if (extractedFile != null) {
						extractedFile.deleteOnExit();
					}
					runGame(extractedFile.getCanonicalPath());
				} catch (IOException e) {
					gui.messageBox(e.getMessage());
				}
			};
		}
	}

	class MenuAction {

		String name;
		Runnable action;

		MenuAction() {
		}

		MenuAction(String name, Runnable action) {
			this.name = name;
			this.action = action;
		}

		public void run() {
			action.run();
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private GUIInterface gui;
	private ListView<MenuAction> menu;
	private ListView<MenuAction> gameMenu;

	private final ObservableList<MenuAction> menuItems = FXCollections.<MenuAction> observableArrayList(
			new MenuAction("Resume", this::resume), new MenuAction("Load Game", this::loadGame),
			new MenuAction("Reset", this::reset), new MenuAction("Exit", this::exit),
			new MenuAction("Power Off", this::powerOff));

	private final ObservableList<MenuAction> games = FXCollections
			.<MenuAction> observableArrayList(new MenuAction("Back", () -> gameMenu.setVisible(false)));

	public OnScreenMenu(GUIInterface gui) {
		this.gui = gui;
		menu = new ListView<>(menuItems);
		gameMenu = new ListView(games);
		addMenuListeners(menu);
		addMenuListeners(gameMenu);
		getChildren().addAll(menu, gameMenu);
		gameMenu.setVisible(false);
		setVisible(false);
	}

	private void addMenuListeners(ListView<MenuAction> menu) {
		menu.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, e -> {
			if (e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.SPACE)) {
				menu.getSelectionModel().getSelectedItem().run();
			}
		});
		menu.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
			if (e.getClickCount() == 2) {
				menu.getSelectionModel().getSelectedItem().run();
			}
		});
	}

	private void exit() {
		gui.getNes().quit();
		Platform.exit();
	}

	private File extractRomFromZip(String zipName, String romName) throws IOException {
		final File outputFile;
		final FileOutputStream fos;
		try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipName))) {
			ZipEntry entry;
			do {
				entry = zipStream.getNextEntry();
			} while ((entry != null) && (!entry.getName().equals(romName)));
			if (entry == null) {
				zipStream.close();
				throw new IOException("Cannot find file " + romName + " inside archive " + zipName);
			}
			// name temp. extracted file after parent zip and file inside
			// note: here's the bug, when it saves the temp file if it's in a
			// folder
			// in the zip it's trying to put it in the same folder outside the
			// zip
			outputFile = new File(new File(zipName).getCanonicalFile().getParent() + File.separator
					+ FileUtils.stripExtension(new File(zipName).getName()) + " - " + romName);
			if (outputFile.exists() && !outputFile.delete()) {
				gui.messageBox("Cannot extract file. File " + outputFile.getCanonicalPath() + " already exists.");
				zipStream.close();
				return null;
			}
			final byte[] buf = new byte[4096];
			fos = new FileOutputStream(outputFile);
			int numBytes;
			while ((numBytes = zipStream.read(buf, 0, buf.length)) != -1) {
				fos.write(buf, 0, numBytes);
			}
		}
		fos.close();
		return outputFile;
	}

	private void hide() {
		setVisible(false);
	}

	private List<String> listRomsInZip(String zipName) throws IOException {
		final List<String> romNames;
		try (ZipFile zipFile = new ZipFile(zipName)) {
			final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			romNames = new ArrayList<>();
			while (zipEntries.hasMoreElements()) {
				final ZipEntry entry = zipEntries.nextElement();
				if (!entry.isDirectory() && (entry.getName().endsWith(".nes") || entry.getName().endsWith(".fds")
						|| entry.getName().endsWith(".nsf"))) {
					romNames.add(entry.getName());
				}
			}
		}
		if (romNames.isEmpty()) {
			throw new IOException("No NES games found in ZIP file.");
		}
		return romNames;
	}

	private void loadGame() {
		gameMenu.setVisible(true);
		gameMenu.requestFocus();
	}

	private void loadRomFromZip(String zipName) throws IOException {
		listRomsInZip(zipName).stream().map(romName -> new GameAction(zipName, romName)).forEach(games::add);
		if (games.size() == 2) {
			games.get(1).run();
		} else if (games.size() > 2) {
			Platform.runLater(() -> loadGame());
		}
	}

	public void loadROMs(String path) {
		if (path.toLowerCase().endsWith(".zip")) {
			try {
				loadRomFromZip(path);
			} catch (IOException ex) {
				gui.messageBox(
						"Could not load file:\nFile does not exist or is not a valid NES game.\n" + ex.getMessage());
			}
		} else {
			games.add(new GameAction(new File(path)));
			runGame(path);
		}
	}

	private void powerOff() {
		// try {
		// Runtime.getRuntime().exec("sudo shutdown -h now");
		// } catch (IOException ex) {
		// Logger.getLogger(OnScreenMenu.class.getName()).log(Level.SEVERE,
		// null, ex);
		// }
	}

	private void reset() {
		gui.getNes().reset();
		hide();
	}

	private void resume() {
		gui.getNes().resume();
		hide();
	}

	private void runGame(String path) {
		gui.getNes().loadROM(path);
		Platform.runLater(() -> {
			gameMenu.setVisible(false);
			hide();
		});
	}

	public void show() {
		gui.getNes().pause();
		setVisible(true);
	}
}
