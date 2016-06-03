package arf;

import java.util.Random;

public class FixedLengthColdStoreFiller implements IColdStoreFiller {
	private Random random;
	private int length;
	
	public FixedLengthColdStoreFiller(Random random, int length) {
		this.random = random;
		this.length = length;
	}
	
	@Override
	public BitArray[] getElements(int count) {
		BitArray[] result = new BitArray[count];
		for (int i = 0; i < count; i++)
			result[i] = BitArray.generateRandom(random, length);
		return result;
	}
}
