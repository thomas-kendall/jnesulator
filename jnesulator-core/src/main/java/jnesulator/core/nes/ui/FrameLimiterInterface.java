package jnesulator.core.nes.ui;

public interface FrameLimiterInterface {

	void setInterval(long ns);

	void sleep();

	void sleepFixed();
}
