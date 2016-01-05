package arf;

import java.util.Random;

public class RandomQueryMaker implements IQueryMaker {
	private Random random;
	private int elementLimit;
	
	public RandomQueryMaker(Random random) {
		this.random = random;
		this.elementLimit = -1;
	}
	
	public RandomQueryMaker(Random random, int elementLimit) {
		this.random = random;
		this.elementLimit = elementLimit;
	}
	
	private int getElement() {
		if (elementLimit == -1)
			return random.nextInt();
		return random.nextInt(elementLimit);
	}

	@Override
	public Segment generateSegment() {
		return new Segment(getElement(), getElement());
	}
}
