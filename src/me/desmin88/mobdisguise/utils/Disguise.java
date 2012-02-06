package me.desmin88.mobdisguise.utils;

import me.desmin88.mobdisguise.MobDisguise.MobType;

public class Disguise {
	public MobType mob;
	public String data;
	
	public Disguise(MobType mob, String data) {
		this.mob = mob;
		this.data = data;
	}
	
	public void setData(String newData) {
		data = newData;
	}
}
