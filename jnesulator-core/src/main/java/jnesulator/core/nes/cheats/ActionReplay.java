package jnesulator.core.nes.cheats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jnesulator.core.nes.CPURAM;

/**
 * Emulation of the Pro Action Replay device. This device allows to apply "RAM
 * codes" to have extra lives, ammo, time, etc...
 */
public class ActionReplay {

	private static int RAM_SIZE = 0x07FF;
	private CPURAM cpuram;
	// Memory patches for Pro Action Replay codes
	private HashMap<Integer, Patch> patches = new HashMap<>();
	// List of addresses for the "find code" feature
	private List<Integer> foundAddresses = new ArrayList<>();

	/**
	 * Creates a new Pro Action Replay device which will act on the given
	 * memory.
	 *
	 * @param cpuram
	 *            - memory
	 */
	public ActionReplay(CPURAM cpuram) {
		this.cpuram = cpuram;
	}

	/**
	 * Add a memory patch. The patch is permanent (the value is constantly
	 * written into memory until a new game is loaded).
	 */
	public void addMemoryPatch(Patch patch) {
		if (!patches.containsKey(patch.getAddress())) {
			patches.put(patch.getAddress(), patch);
		}
	}

	/**
	 * Patches the memory with Pro Action Replay codes.
	 */
	public void applyPatches() {
		cpuram.setPatches(patches);
	}

	/**
	 * Remove all the patches.
	 */
	public void clear() {
		patches.clear();
	}

	/**
	 * Find where at the previously found addresses can be found the given
	 * value. This method continue a previously started search.
	 *
	 * @param value
	 *            - value to be found.
	 * @return the list of addresses where the value were found.
	 */
	public List<Integer> continueSearch(byte value) {
		List<Integer> addressesToRemove = new ArrayList<>();
		for (int address : foundAddresses) {
			if ((cpuram.read(address) & 0xFF) != (value & 0xFF)) {
				addressesToRemove.add(address);
			}
		}
		foundAddresses.removeAll(addressesToRemove);
		return foundAddresses;
	}

	/**
	 * Gets the list memory addresses of the current search.
	 */
	public List<Integer> getFoundAddresses() {
		return foundAddresses;
	}

	/**
	 * Get the list of patches currently applied.
	 */
	public HashMap<Integer, Patch> getPatches() {
		return patches;
	}

	/**
	 * Find where in RAM can be found the given value. This method begins a new
	 * search.
	 *
	 * @param value
	 *            - value to be found.
	 * @return the list of addresses where the value were found.
	 */
	public List<Integer> newSearchInMemory(byte value) {
		foundAddresses.clear();
		for (int address = 0; address < RAM_SIZE; address++) {
			if ((cpuram.read(address) & 0xFF) == (value & 0xFF)) {
				foundAddresses.add(address);
			}
		}
		return foundAddresses;
	}
}
