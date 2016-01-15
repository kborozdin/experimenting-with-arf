package arf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import arf.IQueryMaker.Segment;

public class TestCorrectness {
	@Test
	public void testStress() {
		ArrayList<Integer> results = new ArrayList<>();
		
		for (int arfSize = 10; arfSize <= (int)1e6; arfSize *= 10) {
			Random random = new Random(31415);
			IQueryMaker queryMaker = new RandomQueryMaker(random, 30);
			IColdStoreFiller coldStoreFiller = new RandomColdStoreFiller(random, 20, 0);
			IColdStore coldStore = new SimpleColdStore(0);
			coldStore.fillWith(coldStoreFiller.getElements((int)1e6));
			
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
			assertTrue(results.get(i - 1) > results.get(i) || results.get(i) < 50);
		assertTrue(results.get(results.size() - 1) < 50);
	}
	
	@Test
	public void testStrings() {
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
		
		arf.learnFalsePositive(BitArray.fromString("abbb"), BitArray.fromString("baza"));
		arf.learnFalsePositive(BitArray.fromString("aaa"), BitArray.fromString("aaa"));
		
		assertTrue(arf.hasAnythingProbably(BitArray.fromString("abb"), BitArray.fromString("baza")));
		assertTrue(arf.hasAnythingProbably(BitArray.fromString(""), BitArray.fromString("")));
		assertTrue(arf.hasAnythingProbably(BitArray.fromString("aaa"), BitArray.fromString("c")));
		
		assertFalse(arf.hasAnythingProbably(BitArray.fromString("abbba"), BitArray.fromString("baaza")));
		assertFalse(arf.hasAnythingProbably(BitArray.fromString("aaa"), BitArray.fromString("aaa")));
	}
}
