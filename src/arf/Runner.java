package arf;

import arf.IQueryMaker.Segment;

public class Runner {
	IArf arf;
	IColdStore coldStore;
	IQueryMaker queryMaker;
	
	public enum ArfMode {
		DISABLED, ENABLED, DEBUG
	}
	
	public Runner(IArf arf, IColdStore coldStore, IQueryMaker queryMaker) {
		this.arf = arf;
		this.coldStore = coldStore;
		this.queryMaker = queryMaker;
	}
	
	public double runAndMeasureTimeInMilliseconds(int iterations, ArfMode arfMode) {
		long startTime = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			System.out.println(i); // TODO
			Segment query = queryMaker.generateSegment();
			if (arfMode != ArfMode.DISABLED && !arf.hasAnythingProbably(query.left, query.right)) {
				if (arfMode == ArfMode.DEBUG && coldStore.hasAnything(query.left, query.right))
					throw new IllegalStateException("ARF false negative");
				continue;
			}
			if (!coldStore.hasAnything(query.left, query.right) && arfMode != ArfMode.DISABLED)
				arf.learnFalsePositive(query.left, query.right);
		}
		long totalTime = System.nanoTime() - startTime;
		return (double)totalTime / 1e6;
	}
	
	public static double runWithDefaults(ArfMode arfMode, int arfSizeInBits, IColdStoreFiller coldStoreFiller,
			int coldStoreSize, IQueryMaker queryMaker, int queriesCount) {
		IArf arf = new SimpleBitArf(arfSizeInBits);
		IColdStore coldStore = new SimpleColdStore(10);
		coldStore.fillWith(coldStoreFiller.getElements(coldStoreSize));
		return new Runner(arf, coldStore, queryMaker).runAndMeasureTimeInMilliseconds(queriesCount, arfMode);
	}
}
