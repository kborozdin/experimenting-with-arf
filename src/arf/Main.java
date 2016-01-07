package arf;

import java.util.Random;

import arf.Runner.ArfMode;

public class Main {
	public static void main(String[] args) {
		//double elapsedTime = Runner.runWithDefaults(ArfMode.DEBUG, (int)1e3,
		//		new RandomColdStoreFiller(random, (int)1e6), (int)1e6, new RandomQueryMaker(random, (int)1e7), (int)1e4);
		//double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, (int)1e5,
		//		new RandomColdStoreFiller(random), (int)1e6, new SimilarQueryMaker(random, 100, (int)1e3, 100), (int)1e3);
		//System.out.println("Elapsed time: " + elapsedTime);
		
		testBothRandom();
		testRandomAndDensePrefix();
		testRandomAndSimilar();
	}
	
	public static void testBothRandom() {
		Random random = new Random(12345);
		for (int arfSize = 100; arfSize <= (int)1e6; arfSize *= 10)
			for (int coldSize = (int)1e6; coldSize <= (int)1e7; coldSize *= 10) {
				double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, arfSize,
						new RandomColdStoreFiller(random), coldSize, new RandomQueryMaker(random), (int)1e3);
				System.out.println("Both random: arfSize = " + arfSize + ", coldSize = " + coldSize + ": " + elapsedTime + "ms");
			}
	}

	public static void testRandomAndDensePrefix() {
		Random random = new Random(12346);
		for (int arfSize = 100; arfSize <= (int)1e6; arfSize *= 10)
			for (int coldSize = (int)1e6; coldSize <= (int)1e7; coldSize *= 10)
				for (int fullSize = coldSize * 2; fullSize <= coldSize * 16; fullSize *= 2) {
					double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, arfSize,
							new RandomColdStoreFiller(random, coldSize), coldSize, new RandomQueryMaker(random, fullSize), (int)1e3);
					System.out.println("Dense prefix and random: arfSize = " + arfSize + ", coldSize (prefixSize) = " + coldSize +
							", fullSize = " + fullSize + ": " + elapsedTime + "ms");
				}
	}

	public static void testRandomAndSimilar() {
		Random random = new Random(12347);
		for (int arfSize = 100; arfSize <= (int)1e6; arfSize *= 10)
			for (int coldSize = (int)1e6; coldSize <= (int)1e7; coldSize *= 10) {
				double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, arfSize,
						new RandomColdStoreFiller(random), coldSize, new SimilarQueryMaker(random, 100, Integer.MAX_VALUE / coldSize, 100), (int)1e3);
				System.out.println("Random and similar: arfSize = " + arfSize + ", coldSize = " + coldSize + ": " + elapsedTime + "ms");
			}
	}
}
