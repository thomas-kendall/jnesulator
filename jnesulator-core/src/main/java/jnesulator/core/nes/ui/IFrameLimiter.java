package jnesulator.core.nes.ui;

public interface IFrameLimiter {

	void setInterval(long ns);

	void sleep();

	void sleepFixed();
}
