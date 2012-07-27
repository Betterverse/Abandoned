package net.betterverse.questioner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class KeyValueTable<K, V> {
	private List<KeyValue<K, V>> keyValues = new ArrayList<KeyValue<K, V>>();

	public List<KeyValue<K, V>> getKeyValues() {
		return this.keyValues;
	}

	public void setKeyValues(List<KeyValue<K, V>> keyValues) {
		this.keyValues = keyValues;
	}

	public KeyValueTable() {
	}

	public KeyValueTable(Hashtable<K, V> table) {
		this(new ArrayList<K>((Collection<K>) table.keySet()), new ArrayList<V>(table.values()));
	}

	public KeyValueTable(List<K> keys, List<V> values) {
		for (int i = 0; i < keys.size(); i++) {
			this.keyValues.add(new KeyValue<K, V>(keys.get(i), values.get(i)));
		}
	}

	public void put(K key, V value) {
		this.keyValues.add(new KeyValue<K, V>(key, value));
	}

	public void add(KeyValue<K, V> keyValue) {
		this.keyValues.add(keyValue);
	}

	public void sortByKey() {
		Collections.sort(this.keyValues, new Sorting.KeySort());
	}

	public void sortByValue() {
		Collections.sort(this.keyValues, new Sorting.ValueSort());
	}

	public void revese() {
		Collections.reverse(this.keyValues);
	}
}
