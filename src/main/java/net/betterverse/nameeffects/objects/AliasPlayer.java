package net.betterverse.nameeffects.objects;

import net.betterverse.nameeffects.NameEffects;

import java.io.Serializable;

public class AliasPlayer implements Serializable {

	private final String name;
	private String alias = null, prefix = null;

	public AliasPlayer(String name, String alias, String prefix) {
		this.name = name;
		this.alias = alias;
		this.prefix = prefix;
	}

	public AliasPlayer(String name) {
		this.name = name;
		prefix = "";
		alias = name;
	}

	public String getName() {
		return this.name;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
		update();
	}

	public void resetAlias() {
		this.alias = null;
		update();
	}

	public String getPrefix() {
		return this.prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
		update();
	}

	public void resetPrefix() {
		this.prefix = null;
		update();
	}

	protected void update() {
		NameEffects.getInstance().players.put(this.name, this);
	}
}
