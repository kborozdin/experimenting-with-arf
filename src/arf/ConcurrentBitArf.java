package arf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConcurrentBitArf implements IArf {
	private int queriesCountToRebuildAfter;
	private int processedQueriesCount;
	private BlockingQueue<Segment> queue;
	private volatile SimpleBitArf stableArf;
	private SimpleBitArf workingArf;
	
	private class Worker implements Runnable {
		@Override
		public void run() {
			while (true) {
				Segment segment = null;
				try {
					segment = queue.take();
				} catch (InterruptedException e) {}
				
				workingArf.learnFalsePositive(segment.left, segment.right);
				processedQueriesCount++;
				
				if (processedQueriesCount == queriesCountToRebuildAfter) {
					stableArf = (SimpleBitArf)workingArf.clone();
					processedQueriesCount = 0;
				}
			}
		}
	}
	
	public ConcurrentBitArf(int sizeLimitInBits, int queriesCountToRebuildAfter) {
		this.queriesCountToRebuildAfter = queriesCountToRebuildAfter;
		queue = new LinkedBlockingQueue<>();
		workingArf = new SimpleBitArf(sizeLimitInBits);
		stableArf = (SimpleBitArf)workingArf.clone();
		Thread workerThread = new Thread(new Worker());
		workerThread.setDaemon(true);
		workerThread.start();
	}
	
	@Override
	public boolean hasAnythingProbably(BitArray left, BitArray right) {
		return stableArf.hasAnythingProbably(left, right);
	}

	@Override
	public void learnFalsePositive(BitArray left, BitArray right) {
		queue.add(new Segment(left, right));
	}
}
