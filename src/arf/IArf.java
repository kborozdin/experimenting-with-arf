package arf;

public interface IArf {
	boolean hasAnythingProbably(BitArray left, BitArray right);
	void learnFalsePositive(BitArray left, BitArray right);
}
