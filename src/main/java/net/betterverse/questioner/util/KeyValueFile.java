package net.betterverse.questioner.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class KeyValueFile {
	private static final String newLine = System.getProperty("line.separator");
	private Map<String, String> keys = new HashMap<String, String>();
	private String fileName;

	public KeyValueFile(String fileName) {
		this.fileName = fileName;

		File file = new File(fileName);

		if (file.exists()) {
			load();
		} else {
			save();
		}
	}

	public void load() {
		try {
			BufferedReader fin = new BufferedReader(new FileReader(this.fileName));
			try {
				String line;
				while ((line = fin.readLine()) != null) {
					String[] tokens = line.split("#");
					if (tokens.length > 0) {
						String lineBeforeComment = tokens[0];
						tokens = lineBeforeComment.split("=");
						if (tokens.length >= 2) {
							this.keys.put(tokens[0], tokens[1]);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		SortedMap<String, String> sortedKeys = new TreeMap<String, String>(this.keys);
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(this.fileName));
			try {
				for (String key : keys.keySet()) {
					output.write(key.toLowerCase() + "=" + (String) sortedKeys.get(key) + newLine);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setMap(Map<String, String> keys) {
		this.keys = keys;
		save();
	}

	public void putAll(Map<String, String> keys) {
		this.keys.putAll(keys);
		save();
	}

	public String get(String key) {
		return (String) this.keys.get(key);
	}

	public Map<String, String> returnMap() throws Exception {
		return new HashMap<String, String>(this.keys);
	}

	public boolean containsKey(String var) {
		return this.keys.containsKey(var);
	}

	public String getProperty(String var) {
		return (String) this.keys.get(var);
	}

	public void removeKey(String var) {
		if (this.keys.containsKey(var)) {
			this.keys.remove(var);
			save();
		}
	}

	public boolean keyExists(String key) {
		return containsKey(key);
	}

	public String getString(String key) {
		if (containsKey(key)) {
			return getProperty(key);
		}
		return "";
	}

	public String getString(String key, String value) {
		if (containsKey(key)) {
			return getProperty(key);
		}
		setString(key, value);
		return value;
	}

	public void setString(String key, String value) {
		this.keys.put(key, value);
		save();
	}

	public int getInt(String key) {
		if (containsKey(key)) {
			return Integer.parseInt(getProperty(key));
		}
		return 0;
	}

	public int getInt(String key, int value) {
		if (containsKey(key)) {
			return Integer.parseInt(getProperty(key));
		}
		setInt(key, value);
		return value;
	}

	public void setInt(String key, int value) {
		this.keys.put(key, String.valueOf(value));

		save();
	}

	public double getDouble(String key) {
		if (containsKey(key)) {
			return Double.parseDouble(getProperty(key));
		}
		return 0.0D;
	}

	public double getDouble(String key, double value) {
		if (containsKey(key)) {
			return Double.parseDouble(getProperty(key));
		}
		setDouble(key, value);
		return value;
	}

	public void setDouble(String key, double value) {
		this.keys.put(key, String.valueOf(value));

		save();
	}

	public long getLong(String key) {
		if (containsKey(key)) {
			return Long.parseLong(getProperty(key));
		}
		return 0L;
	}

	public long getLong(String key, long value) {
		if (containsKey(key)) {
			return Long.parseLong(getProperty(key));
		}
		setLong(key, value);
		return value;
	}

	public void setLong(String key, long value) {
		this.keys.put(key, String.valueOf(value));

		save();
	}

	public boolean getBoolean(String key) {
		if (containsKey(key)) {
			return Boolean.parseBoolean(getProperty(key));
		}
		return false;
	}

	public boolean getBoolean(String key, boolean value) {
		if (containsKey(key)) {
			return Boolean.parseBoolean(getProperty(key));
		}
		setBoolean(key, value);
		return value;
	}

	public void setBoolean(String key, boolean value) {
		this.keys.put(key, String.valueOf(value));

		save();
	}
}
