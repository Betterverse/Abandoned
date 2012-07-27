package net.betterverse.towns;

public class TownsException extends Exception {
	private static final long serialVersionUID = - 6821768221748544277L;
	public String error;

	public TownsException() {
		super();
		error = "unknown";
	}

	public TownsException(String error) {
		super(error);
		this.error = error;
	}

	public String getError() {
		return error;
	}
}
