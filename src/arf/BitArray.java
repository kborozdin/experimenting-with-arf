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
	
	public int getLongestCommonPrefixWith(BitArray other) {
		int length = Math.min(size, other.getSize());
		long[] bytes1 = bitSet.toLongArray();
		long[] bytes2 = other.bitSet.toLongArray();
		
		for (int i = 0; i * Long.SIZE < length; i++) {
			long xored = 0;
			if (i < bytes1.length)
				xored ^= bytes1[i];
			if (i < bytes2.length)
				xored ^= bytes2[i];
			if (xored == 0)
				continue;
			int index = Long.numberOfTrailingZeros(xored);
			return Math.min(length, i * Long.SIZE + index);
		}
		
		return length;
	}

	public void shiftSuffixLeft(int from, int shiftSize) {
		bitSet = BitSetUtils.shiftLeft(bitSet, from, size, shiftSize);
		size -= shiftSize;
	}

	public void shiftSuffixRight(int from, int shiftSize) {
		bitSet = BitSetUtils.shiftRight(bitSet, from, size, shiftSize);
		size += shiftSize;
	}
	
	// TODO : a constant factor can be reduced by direct work with the array of longs
	public int countOnes(int left, int rightEx) {
		return bitSet.get(left, rightEx).cardinality();
	}
	
	@Override
	public int compareTo(BitArray other) {
		int length = Math.min(size, other.getSize());
		int prefix = getLongestCommonPrefixWith(other);
		if (prefix < length) {
			if (getAsInt(prefix) < other.getAsInt(prefix))
				return -1;
			return 1;
		}
		if (size < other.getSize())
			return -1;
		if (size > other.getSize())
			return 1;
		return 0;
	}
	
	public boolean equals(BitArray other) {
		if (size != other.getSize())
			return false;
		return getLongestCommonPrefixWith(other) == size;
	}
	
	@Override
	public Object clone() {
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
	
	// TODO : does not work with Unicode
	public static BitArray fromString(String str) {
		return BitArray.fromByteArray(str.getBytes());
	}
}
