package arf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import arf.IQueryMaker.Segment;

public class TestCorrectness {
	@Test
	public void test() {
		ArrayList<Integer> results = new ArrayList<>();
		
		for (int arfSize = 10; arfSize <= (int)1e6; arfSize *= 10) {
			Random random = new Random(31415);
			IQueryMaker queryMaker = new RandomQueryMaker(random, (int)1e7);
			IColdStoreFiller coldStoreFiller = new RandomColdStoreFiller(random, (int)1e6);
			IColdStore coldStore = new SimpleColdStore(0);
			coldStore.fillWith(coldStoreFiller, (int)1e6);
			
			IArf arf = new SimpleArf(arfSize);
			int falsePositivesCount = 0;

			for (int i = 0; i < (int)1e4; i++) {
				Segment query = queryMaker.generateSegment();
				if (!arf.hasAnythingProbably(query.left, query.right)) {
					assertFalse(coldStore.hasAnything(query.left, query.right));
					continue;
				}
				if (!coldStore.hasAnything(query.left, query.right)) {
					falsePositivesCount++;
					arf.learnFalsePositive(query.left, query.right);
				}
			}
			
			results.add(falsePositivesCount);
		}
		
		for (int i = 1; i < results.size(); i++)
			assertTrue(results.get(i - 1) >= results.get(i));
		assertTrue(results.get(results.size() - 1) < 50);
	}
}
