package de.bananaco.protect.util;

public class Meta {
	/*
	 * These are the fixed values (to help prevent typos)
	 */
	public final static String WORLD = "world";
	public final static String NAME = "name";
	public final static String FLAGS = "flags";
	public final static String HEIGHT = "height";
	public final static String START_Y = "y";
	public final static String OWNERS = "owners";
	/*
	 * These are the changable things that commands will be provided for
	 */
	public final static String[] INT = {HEIGHT, START_Y};
	public final static String[] LIST = {FLAGS, OWNERS};
}
