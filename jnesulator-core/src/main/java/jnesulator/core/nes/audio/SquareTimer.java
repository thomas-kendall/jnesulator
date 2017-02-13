package jnesulator.core.nes.audio;

public class SquareTimer extends Timer {

	protected int[] values;
	private int periodadd;
	private int divider = 0;

	public SquareTimer(int ctrlen) {
		this.periodadd = 0;
		values = new int[ctrlen];
		period = 0;
		position = 0;
		setduty(ctrlen / 2);
	}

	public SquareTimer(int ctrlen, int periodadd) {
		this.periodadd = periodadd;
		values = new int[ctrlen];
		period = 0;
		position = 0;
		setduty(ctrlen / 2);
	}

	@Override
	public void clock() {
		if (period + periodadd <= 0) {
			return;
		}
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
		if (period < 8) {
			return;
		}
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
		return values[position];
	}

	@Override
	public void reset() {
		position = 0;
	}

	@Override
	public void setduty(int duty) {
		for (int i = 0; i < values.length; ++i) {
			values[i] = (i < duty) ? 1 : 0;
		}
	}

	@Override
	public void setduty(int[] dutyarray) {
		values = dutyarray;
	}

	@Override
	public void setperiod(int newperiod) {
		period = newperiod;
	}
}