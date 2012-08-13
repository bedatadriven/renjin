package org.renjin.gcc.gimple;

public class GimpleParseException extends RuntimeException {

	public GimpleParseException() {
		super();
	}

	public GimpleParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public GimpleParseException(String message) {
		super(message);
	}

	public GimpleParseException(Throwable cause) {
		super(cause);
	}

}
