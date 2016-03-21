package arf;

import java.util.Arrays;

public class SimpleColdStore implements IColdStore {
	private BitArray[] storage;
	private double lagInMilliseconds;
	
	public SimpleColdStore(double lagInMilliseconds) {
		this.storage = new BitArray[0];
		this.lagInMilliseconds = lagInMilliseconds;
	}
	
	@Override
	public boolean hasAnything(BitArray left, BitArray right) {
		long startTime = System.nanoTime();
		int position = Arrays.binarySearch(storage, left);
		if (position < 0)
			position = -position - 1;
		boolean result = position < storage.length && storage[position].compareTo(right) < 0;
		while (System.nanoTime() - startTime < lagInMilliseconds * 1e6) {}
		return result; 
	}
	
	@Override
	public void fillWith(BitArray[] elements) {
		storage = elements;
		Arrays.sort(storage);
	}
}
