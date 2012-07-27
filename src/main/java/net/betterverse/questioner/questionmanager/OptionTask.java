package net.betterverse.questioner.questionmanager;

public abstract class OptionTask implements Runnable {
	protected Option option;

	public Option getOption() {
		return this.option;
	}

	void setOption(Option option) {
		this.option = option;
	}

	public abstract void run();
}
