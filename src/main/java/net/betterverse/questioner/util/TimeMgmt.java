package net.betterverse.questioner.util;

import java.util.ArrayList;
import java.util.List;

public class TimeMgmt {
	public static final long[][] defaultCountdownDelays = {{10L, 1L}, {30L, 5L}, {60L, 10L}, {300L, 60L}, {1800L, 300L}, {3600L, 600L}, {86400L, 3600L}, {2147483647L, 86400L}};

	public static List<Long> getCountdownDelays(int start) {
		return getCountdownDelays(start, defaultCountdownDelays);
	}

	public static List<Long> getCountdownDelays(int start, long[][] delays) {
		List<Long> out = new ArrayList<Long>();
		for (int d = 0; d < delays.length; d++) {
			if (delays[d].length != 2) {
				return null;
			}
		}
		Integer lastDelayIndex = null;
		long nextWarningAt = 2147483647L;
		for (long t = start; t > 0L; t -= 1L) {
			for (int d = 0; d < delays.length; d++) {
				if ((t > delays[d][0]) || ((lastDelayIndex != null) && (t > nextWarningAt) &&
						(d >= lastDelayIndex.intValue()))) {
					continue;
				}
				lastDelayIndex = Integer.valueOf(d);
				nextWarningAt = t - delays[d][1];
				out.add(new Long(t));
				break;
			}
		}

		return out;
	}

	public static String formatCountdownTime(long l) {
		String out = "";
		if (l >= 3600L) {
			int h = (int) Math.floor(l / 3600L);
			out = out + h + " hours";
			l -= h * 3600;
		}
		if (l >= 60L) {
			int m = (int) Math.floor(l / 60L);
			out = out + (out.length() > 0 ? out += ", " : "") + m + " minutes";
			l -= m * 60;
		}
		if ((out.length() == 0) || (l > 0L)) {
			out = out + (out.length() > 0 ? out += ", " : "") + l + " seconds";
		}
		return out;
	}

	public static void main(String[] args) {
		for (Long l : getCountdownDelays(36000000, defaultCountdownDelays)) {
			System.out.println(l + " " + formatCountdownTime(l.longValue()));
		}
	}
}
