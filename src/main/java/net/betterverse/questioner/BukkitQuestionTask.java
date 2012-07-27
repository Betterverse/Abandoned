package net.betterverse.questioner;

import org.bukkit.Server;

import net.betterverse.questioner.questionmanager.QuestionTask;

public abstract class BukkitQuestionTask extends QuestionTask {
	protected Server server;

	public Server getServer() {
		return this.server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public abstract void run();
}
