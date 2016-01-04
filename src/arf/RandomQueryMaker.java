package arf;

import java.util.Random;

public class RandomQueryMaker implements IQueryMaker {
	Random random;
	
	RandomQueryMaker(Random random) {
		this.random = random;
	}
	
	@Override
	public int generateElement() {
		return random.nextInt(100000);
	}
	
	@Override
	public Segment generateSegment() {
		return new Segment(generateElement(), generateElement());
	}
}
