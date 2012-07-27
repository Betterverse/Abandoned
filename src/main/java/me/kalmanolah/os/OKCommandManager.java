package me.kalmanolah.os;

import java.util.Hashtable;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OKCommandManager
{
  private OKmain plugin;
  private Map<String, CommandExecutor> commands = new Hashtable();

  public OKCommandManager(OKmain instance) {
    this.plugin = instance;
  }

  public void addCommand(String label, CommandExecutor executor) {
    this.commands.put(label, executor);
  }

  public boolean dispatch(CommandSender sender, Command command, String label, String[] args) {
    if (!this.commands.containsKey(label)) {
      return false;
    }
    boolean handled = true;
    CommandExecutor ce = (CommandExecutor)this.commands.get(label);
    handled = ce.onCommand(sender, command, label, args);
    return handled;
  }
}