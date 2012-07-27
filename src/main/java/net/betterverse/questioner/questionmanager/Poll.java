package net.betterverse.questioner.questionmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Poll {
	protected Question question;
	protected HashMap<String, Option> voters = new HashMap<String, Option>();

	protected Map<Option, Integer> votes = new HashMap<Option, Integer>();

	protected long endDate = - 1L;

	protected boolean persistance = false;

	public Poll(List<String> voters, Question question, long endDate, boolean persistance) {
		this(voters, question, endDate);
		this.persistance = persistance;
	}

	public Poll(List<String> voters, Question question, long endDate) {
		this(voters, question);
		this.endDate = endDate;
	}

	public Poll(List<String> voters, Question question) {
		for (String voter : voters) {
			addVoter(voter);
		}
	}

	public void addVoter(String voter) {
		this.voters.put(voter, null);
	}

	public void voteFor(String voter, Option vote) {
		this.voters.put(voter, vote);
		if (this.votes.containsKey(vote)) {
			this.votes.put(vote, Integer.valueOf(((Integer) this.votes.get(vote)).intValue() + 1));
		} else {
			this.votes.put(vote, Integer.valueOf(1));
		}
	}

	public void checkEnd() {
		for (String voter : this.voters.keySet()) {
			if (this.voters.get(voter) == null) {
				return;
			}
		}
		end();
	}

	public HashMap<String, Option> getVoters() {
		return this.voters;
	}

	public Set<String> getVoterNames() {
		return this.voters.keySet();
	}

	public boolean isPersistant() {
		return this.persistance;
	}

	public Map<Option, Integer> getVotes() {
		return this.votes;
	}

	public abstract void end();
}
