package arf;

import java.util.Arrays;

public class SimpleColdStore implements IColdStore {
	private int[] storage;
	private double lagInMilliseconds;
	
	public SimpleColdStore(double lagInMilliseconds) {
		this.storage = new int[0];
		this.lagInMilliseconds = lagInMilliseconds;
	}
	
	@Override
	public boolean hasAnything(int left, int right) {
		long startTime = System.nanoTime();
		int position = Arrays.binarySearch(storage, left);
		if (position < 0)
			position = -position - 1;
		boolean result = position < storage.length && storage[position] <= right;
		while (System.nanoTime() - startTime < lagInMilliseconds * 1e6) {}
		return result; 
	}
	
	@Override
	public void fillWith(IColdStoreFiller coldStoreFiller, int count) {
		storage = coldStoreFiller.getElements(count);
		Arrays.sort(storage);
	}
}
