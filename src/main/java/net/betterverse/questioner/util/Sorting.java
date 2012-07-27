package net.betterverse.questioner.util;

import java.util.Comparator;
import java.util.Hashtable;

public class Sorting {
	public static void main(String[] args) {
		Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer>();
		table.put(Integer.valueOf(1), Integer.valueOf(4));
		table.put(Integer.valueOf(0), Integer.valueOf(3));
		table.put(Integer.valueOf(3), Integer.valueOf(0));
		table.put(Integer.valueOf(2), Integer.valueOf(2));
		table.put(Integer.valueOf(4), Integer.valueOf(1));

		KeyValueTable<Integer, Integer> kvTable = new KeyValueTable<Integer, Integer>(table);
		print(kvTable);
		kvTable.sortByKey();
		print(kvTable);
		kvTable.sortByValue();
		print(kvTable);
	}

	public static void print(KeyValueTable<?, ?> table) {
		for (KeyValue<?, ?> index : table.getKeyValues()) {
			System.out.print("[" + index.key + " : " + index.value + "]\n");
		}
		System.out.print("\n");
	}

	static class KeySort implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			if ((! (o1 instanceof KeyValue)) || (! (o2 instanceof KeyValue))) {
				return o1.hashCode() - o2.hashCode();
			}
			KeyValue<?, ?> k1 = (KeyValue<?, ?>) o1;
			KeyValue<?, ?> k2 = (KeyValue<?, ?>) o2;
			return k1.key.hashCode() - k2.key.hashCode();
		}
	}

	static class ValueSort implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			if ((! (o1 instanceof KeyValue)) || (! (o2 instanceof KeyValue))) {
				return o1.hashCode() - o2.hashCode();
			}
			KeyValue<?, ?> k1 = (KeyValue<?, ?>) o1;
			KeyValue<?, ?> k2 = (KeyValue<?, ?>) o2;
			return k1.value.hashCode() - k2.value.hashCode();
		}
	}
}
