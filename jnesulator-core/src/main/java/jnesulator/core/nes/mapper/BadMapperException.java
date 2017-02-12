package jnesulator.core.nes.mapper;

public class BadMapperException extends Exception {
	private static final long serialVersionUID = -6872655898869194455L;

	public String e;

	public BadMapperException(String e) {
		this.e = e;
	}

	@Override
	public String getMessage() {
		return e;
	}
}
