package net.betterverse.questioner.questionmanager;

public class Option {
	protected String command;
	protected String optionDescription;
	protected Runnable reaction;

	public Option(String command, Runnable reaction) {
		this.command = command;
		this.reaction = reaction;
		if ((reaction instanceof OptionTask)) {
			((OptionTask) reaction).setOption(this);
		}
	}

	public Option(String command, Runnable reaction, String optionDescription) {
		this(command, reaction);
		this.optionDescription = optionDescription;
	}

	public String getOptionDescription() {
		return this.optionDescription;
	}

	public String getOptionString() {
		if (hasDescription()) {
			return this.optionDescription;
		}
		return this.command;
	}

	public boolean isCommand(String command) {
		return this.command.toLowerCase().equals(command.toLowerCase());
	}

	public Runnable getReaction() {
		return this.reaction;
	}

	public String toString() {
		return this.command;
	}

	public boolean hasDescription() {
		return this.optionDescription != null;
	}
}
