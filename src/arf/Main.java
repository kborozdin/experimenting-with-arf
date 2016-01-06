package arf;

import java.util.Random;

import arf.Runner.ArfMode;

public class Main {
	public static void main(String[] args) {
		Random random = new Random(123);
		//double elapsedTime = Runner.runWithDefaults(ArfMode.ENABLED, (int)1e5,
		//		new RandomColdStoreFiller(random, (int)1e6), (int)1e6, new RandomQueryMaker(random, (int)1e7), (int)1e3);
		double elapsedTime = Runner.runWithDefaults(ArfMode.DEBUG, (int)1e5,
				new RandomColdStoreFiller(random), (int)1e6, new SimilarQueryMaker(random, 100, (int)1e3, 100), (int)1e3);
		System.out.println("Elapsed time: " + elapsedTime);
	}
}
