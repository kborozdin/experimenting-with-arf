package arf;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestIntegers {
	private ArfMode arfMode;
	private boolean useSimpleArf;
	
	public TestIntegers(ArfMode arfMode, boolean useSimpleArf) {
		this.arfMode = arfMode;
		this.useSimpleArf = useSimpleArf;
	}
	
	@Parameters(name = "arfMode = {0}, useSimpleArf = {1}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				//{ArfMode.DEBUG, false},
				//{ArfMode.DEBUG, true},
				{ArfMode.ENABLED, false},
				{ArfMode.ENABLED, true},
		});
	}
	
	@Test
	public void testIntegers() {
		Random random = new Random(12345);
		final int arfSize = (int)1e6;
		double time = Runner.runWithDefaults(arfMode, useSimpleArf ? new SimpleArf(arfSize) : new SimpleBitArf(arfSize),
				new RandomColdStoreFiller(random, 31, 31, 10), (int)1e6, new RandomQueryMaker(random, 31, 31), (int)1e4);
		assertFalse(Double.isNaN(time));
		System.out.println("Time elapsed (Integers, " + arfMode.toString() + ", " + (useSimpleArf ? "Old" : "New") +
				" ARF ): " + time);
	}
}
