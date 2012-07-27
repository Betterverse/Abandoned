package net.betterverse.questioner;

import net.betterverse.questioner.questionmanager.QuestionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class QuestionerPlayerListener implements Listener {
	Questioner plugin;
	private QuestionManager questionManager;

	public QuestionerPlayerListener(Questioner plugin, QuestionManager questionManager) {
		this.plugin = plugin;
		this.questionManager = questionManager;
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		String command = event.getMessage().substring(1);
		Player player = event.getPlayer();
		try {
			Runnable reaction = this.questionManager.answerFirstQuestion(player.getName(), command);
			int id = this.plugin.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin.plugin, reaction);
			if (id == - 1) {
				this.plugin.sendErrorMsg("Could not schedule reaction to " + player.getName() + "'s question.");
			}
			this.questionManager.removeFirstQuestion(player.getName());
			event.setCancelled(true);
		} catch (Exception localException) {
		}
	}
}
