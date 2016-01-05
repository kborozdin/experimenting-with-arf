package arf;

import java.util.Random;

public class SimilarQueryMaker implements IQueryMaker {
	Random random;
	Segment[] baseSegments;
	int maxSpread, pointer;
	
	public SimilarQueryMaker(Random random, int baseSegmentsCount, int maxSegmentRange, int maxSpread) {
		this.random = random;
		this.maxSpread = maxSpread;
		baseSegments = new Segment[baseSegmentsCount];
		for (int i = 0; i < baseSegmentsCount; i++) {
			int left = random.nextInt();
			baseSegments[i] = new Segment(left, left + random.nextInt(maxSegmentRange));
		}
	}
	
	@Override
	public Segment generateSegment() {
		Segment segment = baseSegments[pointer++];
		if (pointer == baseSegments.length)
			pointer = 0;
		return new Segment(segment.left + random.nextInt(2 * maxSpread + 1) - maxSpread,
				segment.right + random.nextInt(2 * maxSpread + 1) - maxSpread);
	}
}
