package jnesulator.core.nes.audio;

public abstract class Timer {

	protected int period;
	protected int position;

	public abstract void clock();

	public abstract void clock(final int cycles);

	public final int getperiod() {
		return period;
	}

	public abstract int getval();

	public abstract void reset();

	public abstract void setduty(int duty);

	public abstract void setduty(int[] duty);

	public abstract void setperiod(final int newperiod);
}
