package arf;

public class Runner {
	IArf arf;
	IColdStore coldStore;
	IQueryMaker queryMaker;
	
	public Runner(IArf arf, IColdStore coldStore, IQueryMaker queryMaker, int coldStoreSize) {
		this.arf = arf;
		this.coldStore = coldStore;
		this.queryMaker = queryMaker;
		for (int i = 0; i < coldStoreSize; i++)
			this.coldStore.addElement(queryMaker.generateElement());
	}
	
	int runAndGetFalsePositivesCount(int iterations) {
		int falsePositivesCount = 0;
		for (int i = 0; i < iterations; i++) {
			Segment query = queryMaker.generateSegment();
			if (!arf.hasAnythingProbably(query.left, query.right)) {
				if (coldStore.hasAnything(query.left, query.right))
					throw new IllegalStateException("ARF false negative");
				continue;
			}
			if (!coldStore.hasAnything(query.left, query.right)) {
				arf.learnFalsePositive(query.left, query.right);
				falsePositivesCount++;
			}
		}
		return falsePositivesCount;
	}
}
