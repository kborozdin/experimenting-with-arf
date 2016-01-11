package arf;

import java.util.ArrayList;
import java.util.BitSet;

public class SimpleBitArf implements IArf {
	private final int VERTEX_SIZE = 2;
	
	private int sizeLimitInBits;
	private BitArray clockPointer;
	private int verticesSize, leavesSize;
	private ArrayList<Integer> verticesStart, leavesStart;
	private BitSet vertices, leaves;

	// TODO : merge unused suffix ?
	private void addToArray(ArrayList<Integer> array, int from, int value) {
		while (from >= array.size())
			array.add(array.get(array.size() - 1));
		for (int i = from; i < array.size(); i++)
			array.set(i, array.get(i) + value);
	}
	
	private class Node {
		private int depth;
		private int verticesShift, leavesShift;
		private boolean lastBit;
		private Node parent;
		
		public Node() {}
		
		public Node(int depth, int verticesShift, int leavesShift, boolean lastBit, Node parent) {
			this.depth = depth;
			this.verticesShift = verticesShift;
			this.leavesShift = leavesShift;
			this.lastBit = lastBit;
			this.parent = parent;
		}
		
		public boolean getLastBit() {
			return lastBit;
		}
		
		public boolean isLeftSon() {
			return !lastBit;
		}
		
		//TODO left/right son getter/setter ?
		public boolean isLeaf() {
			if (depth == 0)
				return verticesSize == 0;
			return !vertices.get(parent.verticesShift + (isLeftSon() ? 0 : 1));
		}
		
		public boolean getOccupiedBit() {
			return leaves.get(leavesShift);
		}
		
		public void setOccupiedBit(boolean value) {
			leaves.set(leavesShift, value);
		}
		
		public boolean getUsedBit() {
			return leaves.get(leavesShift + 1);
		}

		public void setUsedBit(boolean value) {
			leaves.set(leavesShift + 1, value);
		}
		
		private int getAdditionalVerticesShift() {
			return verticesShift - verticesStart.get(depth);
		}
		
		private int getDepth() {
			return depth;
		}

		private Node goForward(boolean toLeft, int additionalVerticesShift) {
			if (additionalVerticesShift == -1)
				additionalVerticesShift = vertices.get(verticesStart.get(depth), verticesShift).cardinality() * VERTEX_SIZE;
			int newVerticesShift = verticesStart.get(depth + 1) + additionalVerticesShift;
			int additionalLeavesShift = (verticesShift - verticesStart.get(depth)) * VERTEX_SIZE - additionalVerticesShift;
			int newLeavesShift = leavesStart.get(depth + 1) + additionalLeavesShift;
			
			if (toLeft)
				return new Node(depth + 1, newVerticesShift, newLeavesShift, false, this);
			
			if (vertices.get(verticesShift))
				newVerticesShift += VERTEX_SIZE;
			else
				newLeavesShift += VERTEX_SIZE;
			
			return new Node(depth + 1, newVerticesShift, newLeavesShift, true, this);
		}
		
		public Node goLeft(int additionalVerticesShift) {
			return goForward(true, additionalVerticesShift);
		}
		
		public Node goRight(int additionalVerticesShift) {
			return goForward(false, additionalVerticesShift);
		}
		
		public Node goUp() {
			return parent;
		}
		
		public BitArray getPath() {
			BitArray path = new BitArray(depth);
			Node node = this;
			for (int i = depth - 1; i >= 0; i--) {
				path.set(i, node.getLastBit());
				node = node.goUp();
			}
			return path;
		}
		
		// TODO : a better use of known shifts is possible
		// TODO : think
		public Node navigateToLeaf(BitArray element) {
			Node node = this;
			int commonPrefix = getPath().getLongestCommonPrefixWith(element);
			int additionalVerticesShift = -1;
			for (int i = 0; i < depth - commonPrefix; i++) {
				additionalVerticesShift = node.getAdditionalVerticesShift();
				node = node.goUp();
			}
			for (int i = commonPrefix; i < element.getSize() && !node.isLeaf(); i++) {
				if (!element.get(i))
					node = node.goLeft(additionalVerticesShift);
				else
					node = node.goRight(additionalVerticesShift);
				additionalVerticesShift = -1;
			}
			while (!node.isLeaf()) {
				node = node.goLeft(additionalVerticesShift);
				additionalVerticesShift = -1;
			}
			return node;
		}
		
		// TODO : think
		public Node goToNextLeaf() {
			BitArray path = getPath();
			if (isLeaf()) {
				while (path.getSize() > 0 && path.get(path.getSize() - 1))
					path.popBack();
				if (path.getSize() == 0)
					return null;
				path.flip(path.getSize() - 1);
				return navigateToLeaf(path);
			}
			return navigateToLeaf(path);
		}
		
		public Node goToNextCyclicallyLeaf() {
			Node node = goToNextLeaf();
			if (node == null)
				node = new Node().goToNextLeaf();
			return node;
		}
		
		public Node goToSibling() {
			if (isLeftSon())
				return parent.goRight(getAdditionalVerticesShift());
			return parent.goLeft(getAdditionalVerticesShift());
		}

		// TODO : refactor ?
		public void split() {
			boolean occupiedBit = getOccupiedBit();
			addToArray(leavesStart, depth + 1, -VERTEX_SIZE);
			leaves = BitSetUtils.shiftLeft(leaves, leavesShift + VERTEX_SIZE, leavesSize, VERTEX_SIZE);
			leavesSize -= VERTEX_SIZE;
			
			addToArray(verticesStart, depth + 1, VERTEX_SIZE);
			vertices = BitSetUtils.shiftRight(vertices, verticesShift, verticesSize, VERTEX_SIZE);
			verticesSize += VERTEX_SIZE;
			vertices.clear(verticesShift);
			vertices.clear(verticesShift + 1);
			
			if (parent != null)
				vertices.set(parent.verticesShift + (isLeftSon() ? 0 : 1));
			
			Node leftSon = goLeft(-1);
			addToArray(leavesStart, depth + 2, 2 * VERTEX_SIZE);
			leaves = BitSetUtils.shiftRight(leaves, leftSon.leavesShift, leavesSize, 2 * VERTEX_SIZE);
			leavesSize += 2 * VERTEX_SIZE;
			leaves.set(leftSon.leavesShift, occupiedBit);
			leaves.set(leftSon.leavesShift + 2, occupiedBit);
			leaves.set(leftSon.leavesShift + 1);
			leaves.set(leftSon.leavesShift + 3);
		}
		
		public void mergeSons(Node leftSon) {
			boolean occupiedBit = leaves.get(leftSon.leavesShift) || leaves.get(leftSon.leavesShift + 2);
			addToArray(leavesStart, depth + 2, -2 * VERTEX_SIZE);
			leaves = BitSetUtils.shiftLeft(leaves, leftSon.leavesShift + 2 * VERTEX_SIZE, leavesSize, 2 * VERTEX_SIZE);
			leavesSize -= 2 * VERTEX_SIZE;
			
			if (parent != null)
				vertices.clear(parent.verticesShift + (isLeftSon() ? 0 : 1));
			
			addToArray(verticesStart, depth + 1, -VERTEX_SIZE);
			vertices = BitSetUtils.shiftLeft(vertices, verticesShift + VERTEX_SIZE, verticesSize, VERTEX_SIZE);
			verticesSize -= VERTEX_SIZE;
			
			addToArray(leavesStart, depth + 1, VERTEX_SIZE);
			leaves = BitSetUtils.shiftRight(leaves, leavesShift, leavesSize, VERTEX_SIZE);
			leavesSize += VERTEX_SIZE;
			leaves.set(leavesShift, occupiedBit);
			leaves.set(leavesShift + 1);
		}
	}
	
	public SimpleBitArf(int sizeLimitInBits) {
		if (sizeLimitInBits < VERTEX_SIZE)
			throw new IllegalArgumentException("Size limit must be at least " + VERTEX_SIZE);
		this.sizeLimitInBits = sizeLimitInBits;
		
		verticesSize = 0;
		leavesSize = VERTEX_SIZE;
		clockPointer = new BitArray();
		verticesStart = new ArrayList<>();
		verticesStart.add(0);
		leavesStart = new ArrayList<>();
		leavesStart.add(0);
		leavesStart.add(VERTEX_SIZE);
		vertices = new BitSet();
		leaves = new BitSet();
		leaves.set(0);
		leaves.set(1);
	}

	@Override
	public boolean hasAnythingProbably(BitArray left, BitArray right) {
		if (left.compareTo(right) > 0)
			throw new IllegalArgumentException("The left <= right inequality must hold true");
		Node node = new Node().navigateToLeaf(left);
		do {
			node.setUsedBit(true);
			if (node.getOccupiedBit())
				return true;
			node = node.goToNextLeaf();
		}
		while (node != null && node.getPath().compareTo(right) <= 0);
		return false;
	}
	
	private void markEmpty(BitArray left, BitArray right) {
		Node node = new Node().navigateToLeaf(left);
		do {
			node.setOccupiedBit(false);
			node = node.goToNextLeaf();
		}
		while (node != null && node.getPath().compareTo(right) <= 0);
	}

	@Override
	public void learnFalsePositive(BitArray left, BitArray right) {
		if (left.compareTo(right) > 0)
			throw new IllegalArgumentException("The left <= right inequality must hold true");
		Node node = new Node().navigateToLeaf(left);
		while (node.getDepth() < left.getSize() && node.getOccupiedBit()) {
			node.split();
			node = node.navigateToLeaf(left);
		}
		node = new Node().navigateToLeaf(right);
		while (node.getDepth() < right.getSize() && node.getOccupiedBit()) {
			node.split();
			node = node.navigateToLeaf(right);
		}
		markEmpty(left, right);
		while (getSizeInBits() > sizeLimitInBits)
			shrink();
	}
	
	// TODO : think
	private void shrink() {
		Node node = new Node().navigateToLeaf(clockPointer);
		clockPointer = node.goToNextCyclicallyLeaf().getPath(); // TODO : such slow
		while (node.getUsedBit()) {
			node.setUsedBit(false);
			node = node.goToNextCyclicallyLeaf();
			clockPointer = node.goToNextCyclicallyLeaf().getPath();
		}

		Node sibling = node.goToSibling();
		if (!sibling.isLeaf())
			return;
		
		if (sibling.getOccupiedBit() == node.getOccupiedBit())
			clockPointer = node.getPath();
		node.parent.mergeSons(node.isLeftSon() ? node : sibling);
	}

	private int getSizeInBits() {
		// TODO : actual BitSets size is greater
		return verticesSize + leavesSize;
	}
}
