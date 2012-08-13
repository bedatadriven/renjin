package org.renjin.gcc;

public class GccException extends RuntimeException {

	public GccException() {
		super();
	}

	public GccException(String message, Throwable cause) {
		super(message, cause);
	}

	public GccException(String message) {
		super(message);
	}

	public GccException(Throwable cause) {
		super(cause);
	}

}
