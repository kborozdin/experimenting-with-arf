package arf;

import java.util.BitSet;

public final class BitSetUtils {
	private BitSetUtils() {}
	
	public static BitSet shiftLeft(BitSet bs, int left, int size, int shift) {
		final int BUCKET = Long.SIZE;
		if (shift >= BUCKET)
			throw new IllegalArgumentException("Shift must be less than bucket size");
		
		bs.set(size, size + shift);
		long[] buckets = bs.toLongArray();
		
		long shiftMask = ((long)1 << shift) - 1;
		int lastBucket = (size - 1) / BUCKET;
		int leftBucket = (left - shift) / BUCKET;
		long carry = 0;
		for (int i = lastBucket; i > leftBucket; i--) {
			long newCarry = (buckets[i] & shiftMask) << (BUCKET - shift);
			buckets[i] >>>= shift;
			buckets[i] |= carry;
			carry = newCarry;
		}
		
		if (left / BUCKET == leftBucket) {
			long prefix = buckets[leftBucket] & (((long)1 << (left - shift)) - 1);
			buckets[leftBucket] &= ~(((long)1 << left) - 1);
			buckets[leftBucket] >>>= shift;
			buckets[leftBucket] |= prefix | carry;
		}
		else {
			long leftMask = -1 ^ (((long)1 << ((left - shift) % BUCKET)) - 1);
			buckets[leftBucket] &= ~leftMask;
			buckets[leftBucket] |= carry & leftMask;
		}
		
		return BitSet.valueOf(buckets);
	}
	
	public static BitSet shiftRight(BitSet bs, int left, int size, int shift) {
		final int BUCKET = Long.SIZE;
		if (shift >= BUCKET)
			throw new IllegalArgumentException("Shift must be less than bucket size");
		
		bs.set(size, size + shift);
		long[] buckets = bs.toLongArray();
		
		long shiftMask = -1 ^ (((long)1 << (BUCKET - shift)) - 1);
		int leftBucket = left / BUCKET;
		int remainedAtLeft = BUCKET - left % BUCKET;
		long carry = (buckets[leftBucket] & shiftMask) >>> (BUCKET - shift);
		if (remainedAtLeft > shift) {
			long leftMask = -1 ^ (((long)1 << (BUCKET - remainedAtLeft)) - 1);
			long suffix = buckets[leftBucket] & leftMask;
			buckets[leftBucket] &= ~leftMask;
			buckets[leftBucket] |= suffix << shift;
		}
		
		int lastBucket = (size + shift - 1) / BUCKET;
		for (int i = leftBucket + 1; i <= lastBucket; i++) {
			long newCarry = (buckets[i] & shiftMask) >>> (BUCKET - shift);
			buckets[i] <<= shift;
			buckets[i] |= carry;
			carry = newCarry;
		}
		
		return BitSet.valueOf(buckets);
	}
}
