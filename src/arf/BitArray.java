package arf;

import java.util.BitSet;
import java.util.Random;

public class BitArray implements Comparable<BitArray>, Cloneable {
	private BitSet bitSet;
	private int size;
	
	public BitArray() {
		this.bitSet = new BitSet();
		this.size = 0;
	}
	
	public BitArray(int size) {
		this.bitSet = new BitSet(size);
		this.size = size;
	}
	
	public BitArray(BitSet bs, int size) {
		this.bitSet = bs;
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean get(int position) {
		return bitSet.get(position);
	}
	
	public int getAsInt(int position) {
		return get(position) ? 1 : 0;
	}
	
	public void set(int position, boolean value) {
		bitSet.set(position, value);
	}
	
	public void flip(int position) {
		bitSet.set(position, !get(position));
	}
	
	public void popBack() {
		size--;
	}
	
	public void pushBack(boolean value) {
		set(size, value);
		size++;
	}
	
	// TODO : optimize
	public int getLongestCommonPrefixWith(BitArray other) {
		int maximalPrefix = Math.min(size, other.getSize());
		for (int i = 0; i < maximalPrefix; i++)
			if (get(i) != other.get(i))
				return i;
		return maximalPrefix;
	}

	// TODO : optimize
	@Override
	public int compareTo(BitArray other) {
		int length = Math.min(size, other.getSize());
		for (int i = 0; i < length; i++) {
			if (getAsInt(i) < other.getAsInt(i))
				return -1;
			if (getAsInt(i) > other.getAsInt(i))
				return 1;
		}
		if (other.getSize() > length)
			return -1;
		if (size > length)
			return 1;
		return 0;
	}
	
	// TODO : optimize
	public boolean equals(BitArray other) {
		if (size != other.getSize())
			return false;
		for (int i = 0; i < size; i++)
			if (get(i) != other.get(i))
				return false;
		return true;
	}
	
	@Override
	protected Object clone() {
		return new BitArray((BitSet)bitSet.clone(), size);
	}
	
	public static BitArray generateRandom(Random random, int length) {
		BitArray result = new BitArray(length);
		for (int i = 0; i < length; i++)
			result.set(i, random.nextBoolean());
		return result;
	}
	
	public static BitArray fromByteArray(byte[] array) {
		BitArray result = new BitArray(Byte.SIZE * array.length);
		int pointer = 0;
		for (int i = 0; i < array.length; i++)
			for (int j = Byte.SIZE - 1; j >= 0; j--) {
				result.set(pointer, (array[i] & (1 << j)) != 0);
				pointer++;
			}
		return result;
	}
	
	// TODO : Unicode troubles
	public static BitArray fromString(String str) {
		return BitArray.fromByteArray(str.getBytes());
	}
}
