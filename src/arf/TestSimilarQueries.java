package arf;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestSimilarQueries {
	private ArfMode arfMode;
	
	public TestSimilarQueries(ArfMode arfMode) {
		this.arfMode = arfMode;
	}
	
	@Parameters(name = "arfMode = {0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ArfMode.DEBUG},
				{ArfMode.ENABLED}
		});
	}
	
	@Test
	public void testBothRandom() {
		Random random = new Random(12345);
		double time = Runner.runWithDefaults(arfMode, (int)1e6, new RandomColdStoreFiller(random, 400, 0),
				(int)1e6, new SimilarQueryMaker(random, 100, 100, 200, 400), (int)1e3);
		assertFalse(Double.isNaN(time));
		System.out.println("Time elapsed (Similar queries, " + arfMode.toString() + "): " + time);
	}
}
