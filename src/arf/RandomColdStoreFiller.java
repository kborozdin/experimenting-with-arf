package arf;

import java.util.Random;

public class RandomColdStoreFiller implements IColdStoreFiller {
	Random random;
	int maximalLength;
	
	public RandomColdStoreFiller(Random random, int maximalLength) {
		this.random = random;
		this.maximalLength = maximalLength;
	}
	
	@Override
	public BitArray[] getElements(int count) {
		BitArray[] result = new BitArray[count];
		for (int i = 0; i < count; i++) {
			int length = random.nextInt(maximalLength + 1);
			result[i] = BitArray.generateRandom(random, length);
		}
		return result;
	}
}
