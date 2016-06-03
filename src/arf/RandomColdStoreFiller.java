package arf;

import java.util.Random;

public class RandomColdStoreFiller implements IColdStoreFiller {
	private Random random;
	private int minimalLength, maximalLength;
	private BitArray prefix;
	
	public RandomColdStoreFiller(Random random, int minimalLength, int maximalLength, int commonPrefixLength) {
		this.random = random;
		this.minimalLength = minimalLength;
		this.maximalLength = maximalLength;
		prefix = new BitArray();
		for (int i = 0; i < commonPrefixLength; i++)
			prefix.pushBack(random.nextBoolean());
	}
	
	@Override
	public BitArray[] getElements(int count) {
		BitArray[] result = new BitArray[count];
		for (int i = 0; i < count; i++) {
			int length = random.nextInt(maximalLength - minimalLength + 1) + minimalLength - prefix.getSize();
			result[i] = (BitArray)prefix.clone();
			BitArray suffix = BitArray.generateRandom(random, length);
			for (int j = 0; j < length; j++)
				result[i].pushBack(suffix.get(j));
		}
		return result;
	}
}
