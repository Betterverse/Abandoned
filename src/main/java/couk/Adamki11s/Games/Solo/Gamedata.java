package couk.Adamki11s.Games.Solo;

import couk.Adamki11s.Warzone.Warzone.GameType;
import java.util.ArrayList;
import org.bukkit.entity.Player;

public abstract class Gamedata {
	
	
	public abstract void initiateScheduler();
	
	public abstract void initiateGame(ArrayList<Player> list);
	
	public abstract void addParticipant(Player p);
	
	public abstract void removeParticipants(Player p);
	
	public abstract void shotFired(Player p);
	
	public abstract void shotMissed(Player p);
	
	public abstract void swordStruck(Player p, Player target);
	
	public abstract void shotHit(Player p, Player target);
	
	public abstract void incrementPlayerScore(Player p);
	
	public abstract Player getWinner();
	
	public abstract Player getLooser();
	
	public abstract String getName();
	
	public abstract void tickerTask(int schedulerTask);
	
	public abstract void endGame(int taskid);
	
	public abstract void saveData(ArrayList<Player> partic);
	
	public abstract ArrayList<Player> getParticipants();
	
	public abstract void respawn();
	
	public abstract void passGameType(GameType gt);
	
	public abstract void updateSigns();
	

}
