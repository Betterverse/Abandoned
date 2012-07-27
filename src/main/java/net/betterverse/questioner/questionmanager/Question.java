package net.betterverse.questioner.questionmanager;

import java.util.ArrayList;
import java.util.List;

public class Question extends AbstractQuestion {
	protected String target;

	public Question(String target, String question, List<Option> options, boolean persistance) {
		this(target, question, options);
		this.persistance = persistance;
	}

	public Question(String target, String question, List<Option> options) {
		this.id = QuestionManager.getNextQuestionId();
		this.target = target.toLowerCase();
		this.question = question;
		this.options = new ArrayList<Option>(options);
		for (Option option : options) {
			if ((option.reaction instanceof QuestionTask)) {
				((QuestionTask) option.reaction).setQuestion(this);
			}
		}
	}

	public Question newInstance(String target) {
		return new Question(target.toLowerCase(), this.question, this.options, this.persistance);
	}

	public String getTarget() {
		return this.target.toLowerCase();
	}
}
