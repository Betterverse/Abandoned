package net.betterverse.questioner.questionmanager;

public class PollTask extends QuestionTask {
	protected Poll poll;

	public void run() {
		this.poll.voteFor(((Question) getQuestion()).getTarget(), getOption());
		this.poll.checkEnd();
	}

	public Poll getPoll() {
		return this.poll;
	}

	public void setPoll(Poll poll) {
		this.poll = poll;
	}
}
