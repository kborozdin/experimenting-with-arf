package arf;

import java.util.Random;

public class SimilarQueryMaker implements IQueryMaker {
	private Random random;
	private Segment[] baseSegments;
	private int pointer;
	private int stablePrefixLength, maximalLength;
	
	public SimilarQueryMaker(Random random, int baseSegmentsCount, int commonPrefixLength, int stablePrefixLength, int maximalLength) {
		if (!(commonPrefixLength < stablePrefixLength && stablePrefixLength <= maximalLength))
			throw new IllegalArgumentException("The commonPrefixLength < stablePrefixLength <= maximalLength inequality must hold true");
		this.random = random;
		this.stablePrefixLength = stablePrefixLength;
		this.maximalLength = maximalLength;
		baseSegments = new Segment[baseSegmentsCount];
		
		for (int i = 0; i < baseSegmentsCount; i++) {
			BitArray prefix = BitArray.generateRandom(random, commonPrefixLength);

			BitArray left = (BitArray)prefix.clone();
			left.pushBack(false);
			for (int j = commonPrefixLength + 1; j < stablePrefixLength; j++)
				left.pushBack(random.nextBoolean());
			
			BitArray right = (BitArray)prefix.clone();
			right.pushBack(true);
			for (int j = commonPrefixLength + 1; j < stablePrefixLength; j++)
				right.pushBack(random.nextBoolean());
			
			baseSegments[i] = new Segment(left, right);
		}
	}
	
	@Override
	public Segment generateSegment() {
		Segment segment = new Segment(baseSegments[pointer].left, baseSegments[pointer].right);
		pointer++;
		if (pointer == baseSegments.length)
			pointer = 0;

		segment.left = (BitArray)segment.left.clone();
		int additionalLeftLength = random.nextInt(maximalLength - stablePrefixLength + 1);
		for (int i = 0; i < additionalLeftLength; i++)
			segment.left.pushBack(random.nextBoolean());
		
		segment.right = (BitArray)segment.right.clone();
		int additionalRightLength = random.nextInt(maximalLength - stablePrefixLength + 1);
		for (int i = 0; i < additionalRightLength; i++)
			segment.right.pushBack(random.nextBoolean());
		
		return segment;
	}
}
