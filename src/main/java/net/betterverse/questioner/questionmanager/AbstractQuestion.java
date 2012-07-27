package net.betterverse.questioner.questionmanager;

import java.util.List;

public abstract class AbstractQuestion {
	protected int id;
	protected String question;
	protected List<Option> options;
	protected boolean persistance = false;

	public Option getOption(String command) throws InvalidOptionException {
		for (Option option : this.options) {
			if (option.command.toLowerCase().equals(command.toLowerCase())) {
				return option;
			}
		}
		throw new InvalidOptionException();
	}

	public String getQuestion() {
		return this.question;
	}

	public boolean hasCommand(String command) {
		for (Option option : this.options) {
			if (option.isCommand(command)) {
				return true;
			}
		}
		return false;
	}

	public List<Option> getOptions() {
		return this.options;
	}
}
