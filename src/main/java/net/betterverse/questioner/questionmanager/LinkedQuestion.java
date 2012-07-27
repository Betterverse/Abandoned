package net.betterverse.questioner.questionmanager;

import java.util.ArrayList;
import java.util.List;

public class LinkedQuestion extends AbstractQuestion {
	protected List<String> targets;

	public LinkedQuestion(int id, List<String> targets, String question, List<Option> options, boolean persistance) {
		this(id, targets, question, options);
		this.persistance = persistance;
	}

	public LinkedQuestion(int id, List<String> targets, String question, List<Option> options) {
		this.id = id;
		this.targets = targets;
		this.question = question;
		this.options = new ArrayList<Option>(options);
		for (Option option : options) {
			if ((option.reaction instanceof QuestionTask)) {
				((QuestionTask) option.reaction).setQuestion(this);
			}
		}
	}

	public LinkedQuestion newInstance(List<String> targets) {
		return new LinkedQuestion(QuestionManager.getNextQuestionId(), targets, this.question, this.options, this.persistance);
	}

	public List<String> getTargets() {
		return this.targets;
	}
}
