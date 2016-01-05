package arf;

import java.util.Random;

public class RandomColdStoreFiller implements IColdStoreFiller {
	Random random;
	int elementLimit;
	
	public RandomColdStoreFiller(Random random) {
		this.random = random;
		this.elementLimit = -1;
	}
	
	public RandomColdStoreFiller(Random random, int elementLimit) {
		this.random = random;
		this.elementLimit = elementLimit;
	}
	
	@Override
	public int[] getElements(int count) {
		int[] result = new int[count];
		for (int i = 0; i < count; i++) {
			if (elementLimit == -1)
				result[i] = random.nextInt();
			else
				result[i] = random.nextInt(elementLimit);
		}
		return result;
	}
}
