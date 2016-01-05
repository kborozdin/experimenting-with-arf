package arf;

public interface IColdStore {
	boolean hasAnything(int left, int right);
	void fillWith(IColdStoreFiller coldStoreFiller, int count);
}
