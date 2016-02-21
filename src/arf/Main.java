package arf;

import java.util.Random;

import arf.Runner.ArfMode;

public class Main {
	public static void main(String[] args) {
		new Main().testConcurrency();
		//testBothRandom();
		//testRandomAndDensePrefix();
		//testRandomAndSimilar();
	}
	
	private class WorkerForConcurrency implements Runnable {
		private Runner runner;
		
		public WorkerForConcurrency(Runner runner) {
			this.runner = runner;
		}
		
		@Override
		public void run() {
			double elapsedTime = runner.runAndMeasureTimeInMilliseconds((int)1e3, ArfMode.ENABLED);
			System.out.println("Finished thread: " + elapsedTime + " ms");
		}
	}
	
	public void testConcurrency() {
		Random random = new Random(777);
		IArf arf = new ConcurrentBitArf((int)1e5, 100);
		IColdStore coldStore = new SimpleColdStore(10);
		coldStore.fillWith(new RandomColdStoreFiller(random, 800, 100).getElements((int)1e6));

		for (int i = 0; i < 3; i++)
			new Thread(new WorkerForConcurrency(new Runner(arf, coldStore, new RandomQueryMaker(random, 800)))).start();
	}

	public static void testBothRandom() {
		Random random = new Random(12345);
		for (int arfSize = 100; arfSize <= (int)1e6; arfSize *= 10)
			for (int coldSize = (int)1e6; coldSize <= (int)2e6; coldSize *= 2) {
				double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, arfSize,
						new RandomColdStoreFiller(random, 800, 0), coldSize, new RandomQueryMaker(random, 800), (int)1e3);
				System.out.println("Both random: arfSize = " + arfSize + ", coldSize = " + coldSize + ": " + elapsedTime + "ms");
			}
	}

	public static void testRandomAndDensePrefix() {
		Random random = new Random(12346);
		for (int arfSize = 100; arfSize <= (int)1e6; arfSize *= 10)
			for (int coldSize = (int)1e6; coldSize <= (int)2e6; coldSize *= 2) {
				double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, arfSize,
						new RandomColdStoreFiller(random, 800, 200), coldSize, new RandomQueryMaker(random, 800), (int)1e3);
				System.out.println("Dense prefix and random: arfSize = " + arfSize + ", coldSize (prefixSize) = " + coldSize +
						": " + elapsedTime + "ms");
			}
	}

	public static void testRandomAndSimilar() {
		Random random = new Random(12347);
		for (int arfSize = 100; arfSize <= (int)1e6; arfSize *= 10)
			for (int coldSize = (int)1e6; coldSize <= (int)2e6; coldSize *= 2) {
				double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, arfSize,
						new RandomColdStoreFiller(random, 800, 0), coldSize, new SimilarQueryMaker(random, 100, 600, 780, 800), (int)1e3);
				System.out.println("Random and similar: arfSize = " + arfSize + ", coldSize = " + coldSize + ": " + elapsedTime + "ms");
			}
	}
}
