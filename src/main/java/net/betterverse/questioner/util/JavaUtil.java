package net.betterverse.questioner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JavaUtil {
	public static boolean isSubInterface(Class<?> sup, Class<?> sub) {
		if ((sup.isInterface()) && (sub.isInterface())) {
			if (sup.equals(sub)) {
				return true;
			}
			for (Class<?> c : sub.getInterfaces()) {
				if (isSubInterface(sup, c)) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<String> readTextFromJar(String path) throws IOException {
		BufferedReader fin = new BufferedReader(new InputStreamReader(JavaUtil.class.getResourceAsStream(path)));

		List<String> out = new ArrayList<String>();
		String line;
		try {
			while ((line = fin.readLine()) != null) {
				out.add(line);
			}
		} catch (IOException e) {
			throw new IOException(e.getCause());
		} finally {
			fin.close();
		}
		return out;
	}
}
