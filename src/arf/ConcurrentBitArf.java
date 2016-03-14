package arf;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConcurrentBitArf implements IArf {
	private int queriesCountToRebuildAfter;
	private IArf arf;
	private Queue<Segment> queue = new LinkedBlockingQueue<Segment>();
	private Object rebuildDoneNotifier = new Object();
	private boolean rebuild;
	private Object queriesLock = new Object();
	private volatile int queriesCounter;
	
	private class Worker implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (queue.size() < queriesCountToRebuildAfter)
					continue;
				
				synchronized (queriesLock) {
					rebuild = true;
				}
				while (queriesCounter > 0) {}
				
				while (queue.size() > 0) {
					Segment segment = queue.poll();
					arf.learnFalsePositive(segment.left, segment.right);
				}
				
				rebuild = false;
				synchronized (rebuildDoneNotifier) {
					rebuildDoneNotifier.notifyAll();
				}
			}
		}
	}
	
	public ConcurrentBitArf(IArf arf, int queriesCountToRebuildAfter) {
		this.queriesCountToRebuildAfter = queriesCountToRebuildAfter;
		this.arf = arf;
		Thread workerThread = new Thread(new Worker());
		workerThread.setDaemon(true);
		workerThread.start();
	}
	
	private void queryBegin() {
		synchronized (queriesLock) {
			if (rebuild) {
				synchronized (rebuildDoneNotifier) {
					try {
						rebuildDoneNotifier.wait();
					} catch (InterruptedException e) {}
				}
			}
			queriesCounter++;
		}
	}
	
	private void queryEnd() {
		synchronized (queriesLock) {
			queriesCounter--;
		}
	}
	
	@Override
	public boolean hasAnythingProbably(BitArray left, BitArray right) {
		queryBegin();
		boolean result = arf.hasAnythingProbably(left, right);
		queryEnd();
		return result;
	}
	
	@Override
	public void learnFalsePositive(BitArray left, BitArray right) {
		queryBegin();
		queue.add(new Segment(left, right));
		queryEnd();
	}
}
