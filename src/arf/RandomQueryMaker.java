package arf;

import java.util.Random;

public class RandomQueryMaker implements IQueryMaker {
	private Random random;
	private int maximalLength;
	
	public RandomQueryMaker(Random random, int maximalLength) {
		this.random = random;
		this.maximalLength = maximalLength;
	}
	
	private BitArray getElement() {
		int length = random.nextInt(maximalLength + 1);
		return BitArray.generateRandom(random, length);
	}

	@Override
	public Segment generateSegment() {
		return new Segment(getElement(), getElement());
	}
}
