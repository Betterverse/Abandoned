package de.bananaco.protect.util;

import java.io.File;
import java.io.FilenameFilter;

public class StringFilenameFilter implements FilenameFilter {

	private final String match;
	
	public StringFilenameFilter(String match) {
		this.match = match;
	}
	
	@Override
	public boolean accept(File file, String arg) {
		return arg.endsWith(match);
	}

}
