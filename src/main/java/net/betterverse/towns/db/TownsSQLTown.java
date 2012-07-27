package net.betterverse.towns.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

/**
 * @author FuzzeWuzze
 */
@Entity()
@Table(name = "towns_towns")
public class TownsSQLTown {
	@Id
	private int id;
	@NotNull
	private String playerName;
	@Length(max = 30)
	@NotEmpty
	private String name;

	@NotEmpty
	private int id_Mayor;

	@NotEmpty
	private int totalBlocks;

	@NotEmpty
	private int id_Home;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId_Mayor() {
		return id_Mayor;
	}

	public void setId_Mayor(int id_Mayor) {
		this.id_Mayor = id_Mayor;
	}

	public int getTotalBlocks() {
		return totalBlocks;
	}

	public void setTotalBlocks(int totalBlocks) {
		this.totalBlocks = totalBlocks;
	}

	public int getId_Home() {
		return id_Home;
	}

	public void setId_Home(int id_Home) {
		this.id_Home = id_Home;
	}
}
