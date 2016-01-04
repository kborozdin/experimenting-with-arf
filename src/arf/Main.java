package arf;

import java.util.Random;

public class Main {
	public static void main(String[] args) {
		int falsePositivesCount = new Runner(new SimpleArf(10000), new SimpleColdStore(), new RandomQueryMaker(new Random(123)), 100).
				runAndGetFalsePositivesCount(100000);
		System.out.println("False positives: " + falsePositivesCount);
	}
}
