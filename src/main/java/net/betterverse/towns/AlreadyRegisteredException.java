package net.betterverse.towns;

public class AlreadyRegisteredException extends TownsException {
	private static final long serialVersionUID = 4191685552690886161L;

	public AlreadyRegisteredException() {
		super();
		error = "Already registered.";
	}

	public AlreadyRegisteredException(String error) {
		super(error);
		this.error = error;
	}
}
