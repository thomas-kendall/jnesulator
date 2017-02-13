package jnesulator.core.nes.audio;

import jnesulator.core.nes.Utils;

public class NoiseTimer extends Timer {

	private static int periodadd = 0;

	public static int[] genvalues(int whichbit, int seed) {
		int[] tehsuck = new int[(whichbit == 1) ? 32767 : 93];
		for (int i = 0; i < tehsuck.length; ++i) {
			seed = (seed >> 1) | ((((seed & (1 << whichbit)) != 0) ^ ((seed & (Utils.BIT0)) != 0)) ? 16384 : 0);
			tehsuck[i] = seed;
		}
		return tehsuck;

	}

	private int divider = 0;
	private int[] values = genvalues(1, 1);

	private int prevduty = 1;

	public NoiseTimer() {
		period = 0;
	}

	@Override
	public void clock() {
		++divider;
		// note: stay away from negative division to avoid rounding problems
		int periods = (divider + period + periodadd) / (period + periodadd);
		if (periods < 0) {
			periods = 0; // can happen if period or periodadd were made smaller
		}
		position = (position + periods) % values.length;
		divider -= (period + periodadd) * periods;
	}

	@Override
	public void clock(int cycles) {
		divider += cycles;
		// note: stay away from negative division to avoid rounding problems
		int periods = (divider + period + periodadd) / (period + periodadd);
		if (periods < 0) {
			periods = 0; // can happen if period or periodadd were made smaller
		}
		position = (position + periods) % values.length;
		divider -= (period + periodadd) * periods;
	}

	@Override
	public int getval() {
		return (values[position] & 1);
	}

	@Override
	public void reset() {
		position = 0;
	}

	@Override
	public void setduty(int duty) {
		if (duty != prevduty) {
			values = genvalues(duty, values[position]);
			position = 0;
		}
		prevduty = duty;
	}

	@Override
	public void setduty(int[] duty) {
		throw new UnsupportedOperationException("Not supported on noise channel.");
	}

	@Override
	public void setperiod(int newperiod) {
		period = newperiod;
	}
}