package jnesulator.core.nes.audio;

public class TriangleTimer extends Timer {

	private static int periodadd = 1;
	private static int[] triangle = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 14, 13, 12, 11, 10, 9,
			8, 7, 6, 5, 4, 3, 2, 1, 0 };
	private int divider = 0;

	public TriangleTimer() {
		period = 0;
		position = 0;
	}

	@Override
	public void clock() {
		if (period == 0) {
			return;
		}
		++divider;
		// note: stay away from negative division to avoid rounding problems
		int periods = (divider + period + periodadd) / (period + periodadd);
		if (periods < 0) {
			periods = 0; // can happen if period or periodadd were made smaller
		}
		position = (position + periods) & 0x1F;
		divider -= (period + periodadd) * periods;
	}

	@Override
	public void clock(int cycles) {
		if (period == 0) {
			return;
		}
		divider += cycles;
		// note: stay away from negative division to avoid rounding problems
		int periods = (divider + period + periodadd) / (period + periodadd);
		if (periods < 0) {
			periods = 0; // can happen if period or periodadd were made smaller
		}
		position = (position + periods) & 0x1F;
		divider -= (period + periodadd) * periods;
	}

	@Override
	public int getval() {
		return (period == 0) ? 7 : triangle[position];
		// needed to avoid screech when period is zero
	}

	@Override
	public void reset() {
		// no way to reset the triangle
	}

	@Override
	public void setduty(int duty) {
		throw new UnsupportedOperationException("Triangle counter has no duty setting.");
	}

	@Override
	public void setduty(int[] duty) {
		throw new UnsupportedOperationException("Triangle counter has no duty setting.");
	}

	@Override
	public void setperiod(int newperiod) {
		period = newperiod;
		if (period == 0) {
			position = 7;
		}
	}
}