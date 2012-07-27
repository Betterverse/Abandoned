package net.betterverse.questioner.util;

import java.util.List;

public class StringMgmt {
	public static String join(List<?> arr) {
		return join(arr, " ");
	}

	public static String join(List<?> arr, String separator) {
		if ((arr == null) || (arr.size() == 0)) {
			return "";
		}
		String out = arr.get(0).toString();
		for (int i = 1; i < arr.size(); i++) {
			out = out + separator + arr.get(i);
		}
		return out;
	}

	public static String join(Object[] arr) {
		return join(arr, " ");
	}

	public static String join(Object[] arr, String separator) {
		if (arr.length == 0) {
			return "";
		}
		String out = arr[0].toString();
		for (int i = 1; i < arr.length; i++) {
			out = out + separator + arr[i];
		}
		return out;
	}

	public static String[] remFirstArg(String[] arr) {
		return remArgs(arr, 1);
	}

	public static String[] remArgs(String[] arr, int startFromIndex) {
		if (arr.length == 0) {
			return arr;
		}
		if (arr.length < startFromIndex) {
			return new String[0];
		}
		String[] newSplit = new String[arr.length - startFromIndex];
		System.arraycopy(arr, startFromIndex, newSplit, 0, arr.length - startFromIndex);
		return newSplit;
	}

	public static String maxLength(String str, int length) {
		if (str.length() < length) {
			return str;
		}
		if (length > 3) {
			return str.substring(0, length - 3) + "...";
		}
		throw new UnsupportedOperationException("Minimum length of 3 characters.");
	}

	public static boolean containsIgnoreCase(List<String> arr, String str) {
		for (String s : arr) {
			if (s.equalsIgnoreCase(str)) {
				return true;
			}
		}
		return false;
	}
}
