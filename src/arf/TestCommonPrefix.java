package arf;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestCommonPrefix {
	private ArfMode arfMode;
	
	public TestCommonPrefix(ArfMode arfMode) {
		this.arfMode = arfMode;
	}
	
	@Parameters(name = "arfMode = {0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				//{ArfMode.DEBUG},
				{ArfMode.ENABLED}
		});
	}
	
	@Test
	public void testCommonPrefix() {
		Random random = new Random(12345);
		double time = Runner.runWithDefaults(arfMode, new SimpleBitArf((int)1e6), new RandomColdStoreFiller(random, 200, 800, 200),
				(int)1e6, new RandomQueryMaker(random, 1, 800), (int)1e4);
		assertFalse(Double.isNaN(time));
		System.out.println("Time elapsed (Common prefix, " + arfMode.toString() + "): " + time);
	}
}
