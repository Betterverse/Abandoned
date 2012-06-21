package me.znickq.alias;

import java.io.Serializable;


public class AliasPlayer implements Serializable{
	private String displayName, prefix;
	
	public AliasPlayer(String displayName, String prefix) {
		this.displayName = displayName;
		this.prefix = prefix;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	

}
