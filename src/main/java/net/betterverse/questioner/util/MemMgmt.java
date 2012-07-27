package net.betterverse.questioner.util;

public class MemMgmt {
	public static String getMemoryBar(int size, Runtime run) {
		String line = "";
		double percentUsed = (run.totalMemory() - run.freeMemory()) / run.maxMemory();
		int pivot = (int) Math.floor(size * percentUsed);
		for (int i = 0; i < pivot - 1; i++) {
			line = line + "=";
		}
		if (pivot < size - 1) {
			line = line + "+";
		}
		for (int i = pivot + 1; i < size; i++) {
			line = line + "-";
		}
		return line;
	}

	public static String getMemSize(long num) {
		String[] s = {"By", "Kb", "Mb", "Gb", "Tb"};
		double n = num;
		int w = 0;
		while ((n > 1024.0D) && (w < s.length - 1)) {
			n /= 1024.0D;
			w++;
		}
		return String.format("%.2f %s", new Object[]{Double.valueOf(n), s[w]});
	}
}
