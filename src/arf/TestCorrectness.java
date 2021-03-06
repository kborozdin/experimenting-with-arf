package arf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

public class TestCorrectness {
	@Test
	public void testArfSizeMatters() {
		ArrayList<Integer> results = new ArrayList<>();
		Random random = new Random(31415);
		
		for (int arfSize = 10; arfSize <= (int)1e4; arfSize *= 5) {
			IQueryMaker queryMaker = new RandomQueryMaker(random, 1, 100);
			IColdStoreFiller coldStoreFiller = new RandomColdStoreFiller(random, 20, 100, 20);
			IColdStore coldStore = new SimpleColdStore(0);
			coldStore.fillWith(coldStoreFiller.getElements(100));
			
			IArf arf = new SimpleBitArf(arfSize);
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
			assertTrue(results.get(i - 1) > results.get(i));
		assertTrue(results.get(results.size() - 1) < 50);
	}
	
	@Test
	public void testSmallStrings() {
		IArf arf = new SimpleBitArf(1000);
		IColdStore coldStore = new SimpleColdStore(0);
		BitArray[] content = {
			BitArray.fromString("abc"),
			BitArray.fromString("a"),
			BitArray.fromString("aa"),
			BitArray.fromString("abb"),
			BitArray.fromString("c"),
			BitArray.fromString("eaa"),
			BitArray.fromString("")
		};
		coldStore.fillWith(content);
		
		assertTrue(coldStore.hasAnything(BitArray.fromString("b"), BitArray.fromString("cb")));
		assertFalse(coldStore.hasAnything(BitArray.fromString("abcd"), BitArray.fromString("abd")));
		
		arf.learnFalsePositive(BitArray.fromString("aaa"), BitArray.fromString("abb"));
		arf.learnFalsePositive(BitArray.fromString("abba"), BitArray.fromString("abbb"));
		
		assertTrue(arf.hasAnythingProbably(BitArray.fromString("abb"), BitArray.fromString("abba")));
		assertTrue(arf.hasAnythingProbably(BitArray.fromString(""), BitArray.fromString("a")));
		assertTrue(arf.hasAnythingProbably(BitArray.fromString("caba"), BitArray.fromString("cabca")));
		
		assertFalse(arf.hasAnythingProbably(BitArray.fromString("aab"), BitArray.fromString("abb")));
		assertFalse(arf.hasAnythingProbably(BitArray.fromString("abba"), BitArray.fromString("abbaa")));
	}
}
