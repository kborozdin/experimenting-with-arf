package arf;

import java.util.SortedSet;
import java.util.TreeSet;

public class SimpleColdStore implements IColdStore {
	SortedSet<Integer> storage;
	
	public SimpleColdStore() {
		storage = new TreeSet<>();
	}
	
	@Override
	public boolean hasAnything(int left, int right) {
		return storage.subSet(left, right + 1).size() > 0;
	}
	
	@Override
	public void addElement(int element) {
		storage.add(element);
	}
}
