package arf;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestConcurrency {
	private ArfMode arfMode;
	
	public TestConcurrency(ArfMode arfMode) {
		this.arfMode = arfMode;
	}
	
	@Parameters(name = "arfMode = {0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				//{ArfMode.DEBUG},
				{ArfMode.ENABLED}
		});
	}
	
	public class WorkerForConcurrency implements Runnable {
		private Runner runner;
		
		public WorkerForConcurrency(Runner runner) {
			this.runner = runner;
		}
		
		@Override
		public void run() {
			assertFalse(Double.isNaN(runner.runAndMeasureTimeInMilliseconds((int)1e4, arfMode)));
		}
	}
	
	@Test
	public void testConcurrency() {
		Random random = new Random(777);
		IArf arf = new ConcurrentBitArf(new SimpleBitArf((int)1e6), 50);
		IColdStore coldStore = new SimpleColdStore(10);
		coldStore.fillWith(new RandomColdStoreFiller(random, 200, 800, 200).getElements((int)1e6));
		long startTime = System.nanoTime();

		Thread[] threads = new Thread[4];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new WorkerForConcurrency(new Runner(arf, coldStore, new RandomQueryMaker(random, 1, 800))));
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {}
		}
		
		System.out.println("Time elapsed (Concurrency, " + arfMode.toString() + "): " + (System.nanoTime() - startTime) / 1e6);
	}
}
