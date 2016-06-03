package arf;

import java.util.Random;

public class RandomQueryMaker implements IQueryMaker {
	private Random random;
	private int minimalLength, maximalLength;
	
	public RandomQueryMaker(Random random, int minimalLength, int maximalLength) {
		this.random = random;
		this.minimalLength = minimalLength;
		this.maximalLength = maximalLength;
	}
	
	private BitArray getElement() {
		int length = random.nextInt(maximalLength - minimalLength + 1) + minimalLength;
		return BitArray.generateRandom(random, length);
	}

	@Override
	public Segment generateSegment() {
		return new Segment(getElement(), getElement());
	}
}
