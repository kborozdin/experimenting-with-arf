package arf;

public interface IColdStore {
	boolean hasAnything(BitArray left, BitArray right);
	void fillWith(BitArray[] elements);
}
