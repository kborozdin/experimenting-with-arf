package arf;

//TODO : why public?
public class Segment {
	public int left;
	public int right;
	
	public Segment(int left, int right) {
		if (left > right) {
			int temp = left;
			left = right;
			right = temp;
		}
		this.left = left;
		this.right = right;
	}
}