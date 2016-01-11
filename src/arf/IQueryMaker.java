package arf;

public interface IQueryMaker {
	public class Segment {
		public BitArray left;
		public BitArray right;
		
		public Segment(BitArray left, BitArray right) {
			if (left.compareTo(right) > 0) {
				BitArray temp = left;
				left = right;
				right = temp;
			}
			this.left = left;
			this.right = right;
		}
	}

	Segment generateSegment();
}
