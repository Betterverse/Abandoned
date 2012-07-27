package net.betterverse.questioner.questionmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class QuestionManager {
	Map<String, LinkedList<AbstractQuestion>> activeQuestions = new HashMap<String, LinkedList<AbstractQuestion>>();
	private static int nextQuestionId = 0;

	public static int getNextQuestionId() {
		return nextQuestionId++;
	}

	public void newQuestion(Question question) {
	}

	public void appendQuestion(Question question) throws Exception {
		if (question.options.size() == 0) {
			throw new Exception("Question has no options.");
		}
		LinkedList<AbstractQuestion> playersActiveQuestions = (LinkedList<AbstractQuestion>) this.activeQuestions.get(question.target.toLowerCase());
		if (playersActiveQuestions == null) {
			playersActiveQuestions = new LinkedList<AbstractQuestion>();
			this.activeQuestions.put(question.target.toLowerCase(), playersActiveQuestions);
		}
		playersActiveQuestions.add(question);
		this.activeQuestions.put(question.target.toLowerCase(), playersActiveQuestions);
	}

	public void appendLinkedQuestion(LinkedQuestion question) throws Exception {
		if (question.options.size() == 0) {
			throw new Exception("Question has no options.");
		}
		for (String target : question.targets) {
			LinkedList<AbstractQuestion> playersActiveQuestions = (LinkedList<AbstractQuestion>) this.activeQuestions.get(target.toLowerCase());
			if (playersActiveQuestions == null) {
				playersActiveQuestions = new LinkedList<AbstractQuestion>();
				this.activeQuestions.put(target.toLowerCase(), playersActiveQuestions);
			}
			playersActiveQuestions.add(question);
			this.activeQuestions.put(target.toLowerCase(), playersActiveQuestions);
		}
	}

	public LinkedList<AbstractQuestion> getQuestions(String target) throws Exception {
		LinkedList<AbstractQuestion> playersActiveQuestions = (LinkedList<AbstractQuestion>) this.activeQuestions.get(target.toLowerCase());
		if (playersActiveQuestions == null) {
			throw new Exception("There are no pending questions");
		}
		return playersActiveQuestions;
	}

	public AbstractQuestion peekAtFirstQuestion(String target) throws Exception {
		LinkedList<?> playersActiveQuestions = getQuestions(target.toLowerCase());
		if (playersActiveQuestions.size() == 0) {
			removeAllQuestions(target.toLowerCase());
			throw new Exception("There are no pending questions");
		}
		return (AbstractQuestion) playersActiveQuestions.peek();
	}

	public void removeAllQuestions(String target) {
		this.activeQuestions.remove(target.toLowerCase());
	}

	public Runnable answerFirstQuestion(String target, String command) throws InvalidOptionException, Exception {
		return peekAtFirstQuestion(target.toLowerCase()).getOption(command).reaction;
	}

	public void removeFirstQuestion(String target) throws Exception {
		LinkedList<?> playersActiveQuestions = getQuestions(target);
		if (playersActiveQuestions.size() == 0) {
			removeAllQuestions(target.toLowerCase());
			throw new Exception("There are no pending questions");
		}
		if ((playersActiveQuestions.peek() instanceof LinkedQuestion)) {
			LinkedQuestion question = (LinkedQuestion) playersActiveQuestions.peek();
			int id = question.id;
			for (String qTarget : new ArrayList<String>(question.targets)) {
				removeQuestionId(qTarget, id);
			}
		} else {
			playersActiveQuestions.removeFirst();
		}
	}

	public void removeQuestionInQueue(String target, int queueNumber) throws Exception {
		LinkedList<?> playersActiveQuestions = getQuestions(target.toLowerCase());
		if (playersActiveQuestions.size() == 0) {
			removeAllQuestions(target);
			throw new Exception("There are no pending questions");
		}
		try {
			if ((playersActiveQuestions.get(queueNumber) instanceof LinkedQuestion)) {
				LinkedQuestion question = (LinkedQuestion) playersActiveQuestions.get(queueNumber);
				int id = question.id;
				for (String qTarget : new ArrayList<String>(question.targets)) {
					removeQuestionId(qTarget, id);
				}
			} else {
				playersActiveQuestions.removeFirst();
			}
		} catch (IndexOutOfBoundsException e) {
			throw new Exception("Invalid question id.");
		}
	}

	public void removeQuestionId(String target, int id) throws Exception {
		LinkedList<AbstractQuestion> playersActiveQuestions = getQuestions(target.toLowerCase());
		for (AbstractQuestion question : new LinkedList<AbstractQuestion>(playersActiveQuestions)) {
			if (question.id == id) {
				playersActiveQuestions.remove(question);
			}
		}
	}

	public boolean hasQuestion(String target) {
		try {
			LinkedList<?> playersActiveQuestions = getQuestions(target.toLowerCase());
			if (playersActiveQuestions.size() == 0) {
				removeAllQuestions(target.toLowerCase());
				return false;
			}
			return true;
		} catch (Exception e) {
		}
		return false;
	}
}
