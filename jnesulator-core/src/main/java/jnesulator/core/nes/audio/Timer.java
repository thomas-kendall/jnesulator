package jnesulator.core.nes.audio;

// TODO: Seems like this should just be an interface
public abstract class Timer {

	protected int period;
	protected int position;

	public abstract void clock();

	public abstract void clock(int cycles);

	public int getperiod() {
		return period;
	}

	public abstract int getval();

	public abstract void reset();

	public abstract void setduty(int duty);

	public abstract void setduty(int[] duty);

	public abstract void setperiod(int newperiod);
}
