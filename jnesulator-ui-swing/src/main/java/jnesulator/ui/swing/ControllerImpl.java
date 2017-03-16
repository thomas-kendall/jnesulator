package jnesulator.ui.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.Preferences;

import javafx.scene.Scene;
import jnesulator.core.nes.PrefsSingleton;
import jnesulator.core.nes.io.BaseController;
import jnesulator.core.nes.io.ControllerInput;
import jnesulator.core.nes.io.IController;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class ControllerImpl extends BaseController implements IController, KeyListener {

	/**
	 * This method detects the available joysticks / gamepads on the computer
	 * and return them in a list.
	 *
	 * @return List of available joysticks / gamepads connected to the computer
	 */
	private static Controller[] getAvailablePadControllers() {
		List<Controller> gameControllers = new ArrayList<>();
		// Get a list of the controllers JInput knows about and can interact
		// with
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		// Check the useable controllers (gamepads or joysticks with at least 2
		// axis and 2 buttons)
		for (Controller controller : controllers) {
			if ((controller.getType() == Controller.Type.GAMEPAD) || (controller.getType() == Controller.Type.STICK)) {
				int nbOfAxis = 0;
				// Get this controllers components (buttons and axis)
				Component[] components = controller.getComponents();
				// Check the availability of X/Y axis and at least 2 buttons
				// (for A and B, because select and start can use the keyboard)
				for (Component component : components) {
					if ((component.getIdentifier() == Component.Identifier.Axis.X)
							|| (component.getIdentifier() == Component.Identifier.Axis.Y)) {
						nbOfAxis++;
					}
				}
				if ((nbOfAxis >= 2) && (getButtons(controller).length >= 2)) {
					// Valid game controller
					gameControllers.add(controller);
				}
			}
		}
		return gameControllers.toArray(new Controller[0]);
	}

	/**
	 * Return the available buttons on this controller (by priority order).
	 */
	private static Component[] getButtons(Controller controller) {
		List<Component> buttons = new ArrayList<>();
		// Get this controllers components (buttons and axis)
		Component[] components = controller.getComponents();
		for (Component component : components) {
			if (component.getIdentifier() instanceof Component.Identifier.Button) {
				buttons.add(component);
			}
		}
		return buttons.toArray(new Component[0]);
	}

	// private java.awt.Component parent;
	private Controller gameController;
	private Component[] buttons;
	private ScheduledExecutorService thread = Executors.newSingleThreadScheduledExecutor();
	private HashMap<Integer, ControllerInput> m = new HashMap<>(10);
	private int controllernum;
	double threshold = 0.25;

	public ControllerImpl(int controllernum) {
		if ((controllernum != 0) && (controllernum != 1)) {
			throw new IllegalArgumentException("controllerNum must be 0 or 1");
		}
		this.controllernum = controllernum;
		setButtons();
	}

	public ControllerImpl(java.awt.Component parent, int controllernum) {
		this(controllernum);
		// this.parent = parent;
		parent.addKeyListener(this);
	}

	public ControllerImpl(Scene scene, int controllernum) {
		this(controllernum);
		scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> pressKey(e.getCode().impl_getCode()));
		scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, e -> releaseKey(e.getCode().impl_getCode()));
	}

	private Runnable eventQueueLoop() {
		return new Runnable() {
			@Override
			public void run() {
				boolean upPressed = false;
				boolean downPressed = false;
				boolean leftPressed = false;
				boolean rightPressed = false;
				if (gameController != null) {
					Event event = new Event();
					while (!Thread.interrupted()) {
						gameController.poll();
						EventQueue queue = gameController.getEventQueue();
						while (queue.getNextEvent(event)) {
							Component component = event.getComponent();
							if (component.getIdentifier() == Component.Identifier.Axis.X) {
								if (event.getValue() > threshold) {
									if (!rightPressed) {
										if (leftPressed) {
											inputRelease(ControllerInput.Left);
											leftPressed = false;
										}
										inputPress(ControllerInput.Right);
										rightPressed = true;
									}
								} else if (event.getValue() < -threshold) {
									if (!leftPressed) {
										if (rightPressed) {
											inputRelease(ControllerInput.Right);
											rightPressed = false;
										}
										inputPress(ControllerInput.Left);
										leftPressed = true;
									}
								} else {
									if (leftPressed) {
										inputRelease(ControllerInput.Left);
										leftPressed = false;
									}
									if (rightPressed) {
										inputRelease(ControllerInput.Right);
										rightPressed = false;
									}
								}
							} else if (component.getIdentifier() == Component.Identifier.Axis.Y) {
								if (event.getValue() > threshold) {
									if (!upPressed) {
										if (downPressed) {
											inputRelease(ControllerInput.Down);
											downPressed = false;
										}
										inputPress(ControllerInput.Up);
										upPressed = true;
									}
								} else if (event.getValue() < -threshold) {
									if (!downPressed) {
										if (upPressed) {
											inputRelease(ControllerInput.Up);
											upPressed = false;
										}
										inputPress(ControllerInput.Down);
										downPressed = true;
									}
								} else {
									if (downPressed) {
										inputRelease(ControllerInput.Down);
										downPressed = false;
									}
									if (upPressed) {
										inputRelease(ControllerInput.Up);
										upPressed = false;
									}
								}
							} else if (component == buttons[0]) {
								if (isPressed(event)) {
									inputPress(ControllerInput.A);
								} else {
									inputRelease(ControllerInput.A);
								}
							} else if (component == buttons[1]) {
								if (isPressed(event)) {
									inputPress(ControllerInput.B);
								} else {
									inputRelease(ControllerInput.B);
								}
							} else if (component == buttons[2]) {
								if (isPressed(event)) {
									inputPress(ControllerInput.Select);
								} else {
									inputRelease(ControllerInput.Select);
								}
							} else if (component == buttons[3]) {
								if (isPressed(event)) {
									inputPress(ControllerInput.Start);
								} else {
									inputRelease(ControllerInput.Start);
								}
							}
						}

						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							// Preserve interrupt status
							Thread.currentThread().interrupt();
						}
					}
				}
			}
		};
	}

	private boolean isPressed(Event event) {
		Component component = event.getComponent();
		if (component.isAnalog()) {
			if (Math.abs(event.getValue()) > 0.2f) {
				return true;
			} else {
				return false;
			}
		} else if (event.getValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		pressKey(keyEvent.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {
		releaseKey(keyEvent.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

	private void pressKey(int keyCode) {
		if (!m.containsKey(keyCode)) {
			return;
		}

		inputPress(m.get(keyCode));
	}

	private void releaseKey(int keyCode) {
		if (!m.containsKey(keyCode)) {
			return;
		}
		inputRelease(m.get(keyCode));
	}

	public void setButtons() {
		Preferences prefs = PrefsSingleton.get();
		// reset the buttons from prefs
		m.clear();
		switch (controllernum) {
		case 0:
			m.put(prefs.getInt("keyUp1", KeyEvent.VK_UP), ControllerInput.Up);
			m.put(prefs.getInt("keyDown1", KeyEvent.VK_DOWN), ControllerInput.Down);
			m.put(prefs.getInt("keyLeft1", KeyEvent.VK_LEFT), ControllerInput.Left);
			m.put(prefs.getInt("keyRight1", KeyEvent.VK_RIGHT), ControllerInput.Right);
			m.put(prefs.getInt("keyA1", KeyEvent.VK_X), ControllerInput.A);
			m.put(prefs.getInt("keyB1", KeyEvent.VK_Z), ControllerInput.B);
			m.put(prefs.getInt("keySelect1", KeyEvent.VK_SHIFT), ControllerInput.Select);
			m.put(prefs.getInt("keyStart1", KeyEvent.VK_ENTER), ControllerInput.Start);
			break;
		case 1:
		default:
			m.put(prefs.getInt("keyUp2", KeyEvent.VK_W), ControllerInput.Up);
			m.put(prefs.getInt("keyDown2", KeyEvent.VK_S), ControllerInput.Down);
			m.put(prefs.getInt("keyLeft2", KeyEvent.VK_A), ControllerInput.Left);
			m.put(prefs.getInt("keyRight2", KeyEvent.VK_D), ControllerInput.Right);
			m.put(prefs.getInt("keyA2", KeyEvent.VK_G), ControllerInput.A);
			m.put(prefs.getInt("keyB2", KeyEvent.VK_F), ControllerInput.B);
			m.put(prefs.getInt("keySelect2", KeyEvent.VK_R), ControllerInput.Select);
			m.put(prefs.getInt("keyStart2", KeyEvent.VK_T), ControllerInput.Start);
			break;

		}
		Controller[] controllers = getAvailablePadControllers();
		if (controllers.length > controllernum) {
			this.gameController = controllers[controllernum];
			PrefsSingleton.get().put("controller" + controllernum, gameController.getName());
			System.err.println(controllernum + 1 + ". " + gameController.getName());
			this.buttons = getButtons(controllers[controllernum]);
		} else {
			PrefsSingleton.get().put("controller" + controllernum, "");
			this.gameController = null;
			this.buttons = null;
		}
	}

	/**
	 * Start in a separate thread the processing of the controller event queue.
	 * Must be called after construction of the class to enable the processing
	 * of the joystick / gamepad events.
	 */
	public void startEventQueue() {
		// if (System.getProperty("java.class.path").contains("jinput")) {
		thread.execute(eventQueueLoop());
		// }
	}

	/**
	 * Stop the controller event queue thread. Must be called before closing the
	 * application.
	 */
	public void stopEventQueue() {
		thread.shutdownNow();
	}
}
