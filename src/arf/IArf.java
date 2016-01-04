package arf;

public interface IArf {
	boolean hasAnythingProbably(int left, int right);
	void learnFalsePositive(int left, int right);
}
