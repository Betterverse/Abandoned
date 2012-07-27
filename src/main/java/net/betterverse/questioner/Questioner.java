package net.betterverse.questioner;

import net.betterverse.questioner.questionmanager.*;
import net.betterverse.questioner.util.StringMgmt;
import net.betterverse.towns.Towns;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Questioner {
	protected Towns plugin;
	protected QuestionManager questionManager;
	protected QuestionerPlayerListener playerListener;
	private List<Option> currentOptions = new ArrayList<Option>();
	private List<String> currentTargets = new ArrayList<String>();
	private int questionsPerPage = 5;
	private String questionFormat = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "%s" + ChatColor.DARK_GRAY + "] " + ChatColor.DARK_GREEN + "%s";
	private String optionFormat = ChatColor.GREEN + "          /%s";
	private String optionEntendedFormat = ChatColor.YELLOW + " : %s";
	private String listFooterFormat = ChatColor.DARK_GRAY + " ---- " + ChatColor.GRAY + "Page: %d/%d " + ChatColor.DARK_GRAY + "~" + ChatColor.GRAY + " Total Questions: %d";

	public Questioner(Towns plugin) {
		this.plugin = plugin;
		onEnable();
	}

	public void onDisable() {
		this.playerListener = null;
		this.questionManager = null;
		System.out.println("[Questioner] - Disabled");
	}

	private void onEnable() {
		this.questionManager = new QuestionManager();
		this.playerListener = new QuestionerPlayerListener(this, this.questionManager);

		if (plugin.getServer() != null) {
			plugin.getServer().getPluginManager().registerEvents(playerListener, plugin);
			System.out.println("[Questioner] - Enabled");
		}
	}

	public QuestionManager getQuestionManager() {
		return this.questionManager;
	}

	public void appendQuestion(Question question) throws Exception {
		for (Option option : question.getOptions()) {
			if ((option.getReaction() instanceof BukkitQuestionTask)) {
				((BukkitQuestionTask) option.getReaction()).setServer(plugin.getServer());
			}
		}
		getQuestionManager().appendQuestion(question);

		Player player = plugin.getServer().getPlayer(question.getTarget());
		if (player != null) {
			for (String line : formatQuestion(question, "New Question")) {
				player.sendMessage(line);
			}
		}
	}

	public void sendErrorMsg(String msg) {
		System.out.println("[Questioner] Error: " + msg);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String command = cmd.getName().toLowerCase();
		if (command.equals("question")) {
			if (args.length > 0) {
				if (sender.isOp()) {
					if (args[0].equalsIgnoreCase("target")) {
						for (int i = 1; i < args.length; i++) {
							this.currentTargets.add(args[i]);
						}
						sender.sendMessage("NumTargets: " + this.currentTargets.size());
						return true;
					}
					if (args[0].equalsIgnoreCase("opt")) {
						if (args.length > 1) {
							this.currentOptions.add(new Option(args[1], new QuestionTask() {
								public void run() {
									System.out.println("You chose " + getOption().getOptionString() + "!");
								}
							}));
							sender.sendMessage("NumOptions: " + this.currentOptions.size());
						} else {
							sender.sendMessage("help > question opt [option]");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("ask")) {
						try {
							String q = StringMgmt.join(StringMgmt.remFirstArg(args), " ");
							for (String target : this.currentTargets) {
								Question question = new Question(target.toLowerCase(), q, this.currentOptions);
								appendQuestion(question);
							}
							this.currentOptions.clear();
							this.currentTargets.clear();
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
						return true;
					}
				}
				if ((args[0].equalsIgnoreCase("list")) && ((sender instanceof Player))) {
					Player player = (Player) sender;
					int page = 1;
					if (args.length > 1) {
						try {
							page = Integer.parseInt(args[1]);
						} catch (NumberFormatException localNumberFormatException1) {
						}
					}
					for (String line : formatQuestionList(player.getName(), page)) {
						player.sendMessage(line);
					}
					return true;
				}
			}

			sender.sendMessage("Invalid sub command.");
			return true;
		}

		return false;
	}

	public List<String> formatQuestionList(String user, int page) {
		List<String> out = new ArrayList<String>();
		try {
			if (page < 0) {
				throw new Exception("Invalid page number.");
			}
			LinkedList<?> activePlayerQuestions = getQuestionManager().getQuestions(user);
			int numQuestions = activePlayerQuestions.size();
			int maxPage = (int) Math.ceil(numQuestions / this.questionsPerPage);
			if (page > maxPage) {
				throw new Exception("There are no questions on page " + page);
			}
			int start = (page - 1) * this.questionsPerPage;
			for (int i = start; i < start + this.questionsPerPage; i++) {
				try {
					AbstractQuestion question = (AbstractQuestion) activePlayerQuestions.get(i);
					out.addAll(formatQuestion(question, Integer.toString(i)));
				} catch (IndexOutOfBoundsException localIndexOutOfBoundsException) {
				}
			}
			if (maxPage > 1) {
				out.add(String.format(this.listFooterFormat, new Object[]{Integer.valueOf(page), Integer.valueOf(maxPage), Integer.valueOf(numQuestions)}));
			}
		} catch (Exception e) {
			out.add(ChatColor.RED + e.getMessage());
		}
		return out;
	}

	public List<String> formatQuestion(AbstractQuestion question, String tag) {
		List<String> out = new ArrayList<String>();
		out.add(String.format(this.questionFormat, new Object[]{tag, StringMgmt.maxLength(question.getQuestion(), 54)}));
		for (Option option : question.getOptions()) {
			out.add(String.format(this.optionFormat, new Object[]{option.toString()}) + (option.hasDescription() ? String.format(this.optionEntendedFormat, new Object[]{option.getOptionDescription()}) : ""));
		}
		return out;
	}

	public void loadClasses() {
		String[] classes = {"net.betterverse.questioner.BukkitQuestionTask", "net.betterverse.questioner.questionmanager.AbstractQuestion", "net.betterverse.questioner.questionmanager.InvalidOptionException", "net.betterverse.questioner.questionmanager.LinkedQuestion", "net.betterverse.questioner.questionmanager.LinkedQuestionTask", "net.betterverse.questioner.questionmanager.Option", "net.betterverse.questioner.questionmanager.OptionTask", "net.betterverse.questioner.questionmanager.Poll", "net.betterverse.questioner.questionmanager.PollQuestion", "net.betterverse.questioner.questionmanager.PollTask", "net.betterverse.questioner.questionmanager.Question", "net.betterverse.questioner.questionmanager.QuestionFormatter", "net.betterverse.questioner.questionmanager.QuestionManager", "net.betterverse.questioner.questionmanager.QuestionTask"};

		for (String c : classes) {
			try {
				Questioner.class.getClassLoader().loadClass(c);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
