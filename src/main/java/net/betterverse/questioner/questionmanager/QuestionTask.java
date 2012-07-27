package net.betterverse.questioner.questionmanager;

public abstract class QuestionTask extends OptionTask {
	protected AbstractQuestion question;

	public AbstractQuestion getQuestion() {
		return this.question;
	}

	void setQuestion(AbstractQuestion question) {
		this.question = question;
	}

	public abstract void run();
}
