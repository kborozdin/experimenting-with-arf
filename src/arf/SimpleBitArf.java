package arf;

import java.util.ArrayList;

public class SimpleBitArf implements IArf, Cloneable {
	private final int VERTEX_SIZE = 2;
	
	private int sizeLimitInBits;
	private BitArray clockPointer;
	private ArrayList<Integer> verticesStart, leavesStart;
	private BitArray vertices, leaves;

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
		private int pathHash;
		
		public Node() {}
		
		public Node(int depth, int verticesShift, int leavesShift, boolean lastBit, Node parent, int pathHash) {
			this.depth = depth;
			this.verticesShift = verticesShift;
			this.leavesShift = leavesShift;
			this.lastBit = lastBit;
			this.parent = parent;
			this.pathHash = pathHash;
		}
		
		public boolean getLastBit() {
			return lastBit;
		}
		
		public boolean isLeftSon() {
			return !lastBit;
		}

		public boolean isLeaf() {
			if (depth == 0)
				return vertices.getSize() == 0;
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
		
		private int getPathHash() {
			return pathHash;
		}
		
		private int recalcHash(int hash, int character) {
			character++;
			return (hash * 17239 + character) % ((int)1e9 + 7);
		}

		// TODO
		private Node goForward(boolean toLeft, int additionalVerticesShift) {
			if (additionalVerticesShift == -1)
				additionalVerticesShift = vertices.countOnes(verticesStart.get(depth), verticesShift) * VERTEX_SIZE;
			int newVerticesShift = verticesStart.get(depth + 1) + additionalVerticesShift;
			int additionalLeavesShift = (verticesShift - verticesStart.get(depth)) * VERTEX_SIZE - additionalVerticesShift;
			int newLeavesShift = leavesStart.get(depth + 1) + additionalLeavesShift;
			
			if (toLeft)
				return new Node(depth + 1, newVerticesShift, newLeavesShift, false, this, recalcHash(pathHash, 0));
			
			if (vertices.get(verticesShift))
				newVerticesShift += VERTEX_SIZE;
			else
				newLeavesShift += VERTEX_SIZE;
			
			return new Node(depth + 1, newVerticesShift, newLeavesShift, true, this, recalcHash(pathHash, 1));
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
		
		public Node goToNextLeaf() {
			Node node = this;
			
			if (isLeaf()) {
				while (node.depth > 0 && !node.isLeftSon())
					node = node.goUp();
				if (node.depth == 0)
					return null;
				node = node.goToSibling();
			}
			
			while (!node.isLeaf())
				node = node.goLeft(-1);
			return node;
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

		public void split() {
			boolean occupiedBit = getOccupiedBit();
			addToArray(leavesStart, depth + 1, -VERTEX_SIZE);
			leaves.shiftSuffixLeft(leavesShift + VERTEX_SIZE, VERTEX_SIZE);
			
			addToArray(verticesStart, depth + 1, VERTEX_SIZE);
			vertices.shiftSuffixRight(verticesShift, VERTEX_SIZE);
			vertices.set(verticesShift, false);
			vertices.set(verticesShift + 1, false);
			
			if (parent != null)
				vertices.set(parent.verticesShift + (isLeftSon() ? 0 : 1), true);
			
			Node leftSon = goLeft(-1);
			addToArray(leavesStart, depth + 2, 2 * VERTEX_SIZE);
			leaves.shiftSuffixRight(leftSon.leavesShift, 2 * VERTEX_SIZE);
			leaves.set(leftSon.leavesShift, occupiedBit);
			leaves.set(leftSon.leavesShift + 2, occupiedBit);
			leaves.set(leftSon.leavesShift + 1, true);
			leaves.set(leftSon.leavesShift + 3, true);
		}
		
		public void mergeSons(Node leftSon) {
			boolean occupiedBit = leaves.get(leftSon.leavesShift) || leaves.get(leftSon.leavesShift + 2);
			addToArray(leavesStart, depth + 2, -2 * VERTEX_SIZE);
			leaves.shiftSuffixLeft(leftSon.leavesShift + 2 * VERTEX_SIZE, 2 * VERTEX_SIZE);
			
			if (parent != null)
				vertices.set(parent.verticesShift + (isLeftSon() ? 0 : 1), false);
			
			addToArray(verticesStart, depth + 1, -VERTEX_SIZE);
			vertices.shiftSuffixLeft(verticesShift + VERTEX_SIZE, VERTEX_SIZE);
			
			addToArray(leavesStart, depth + 1, VERTEX_SIZE);
			leaves.shiftSuffixRight(leavesShift, VERTEX_SIZE);
			leaves.set(leavesShift, occupiedBit);
			leaves.set(leavesShift + 1, true);
		}
		
		public boolean equals(Node other) {
			return depth == other.depth && pathHash == other.getPathHash() &&
					getPath().equals(other.getPath());
		}
	}
	
	public Node navigateToLeaf(BitArray path, boolean toFirstLeaf) {
		Node node = new Node();
		for (int i = 0; i < path.getSize() && !node.isLeaf(); i++)
			node = node.goForward(!path.get(i), -1);
		while (!node.isLeaf())
			node = node.goForward(toFirstLeaf, -1);
		return node;
	}
	
	public SimpleBitArf() {}
	
	public SimpleBitArf(int sizeLimitInBits) {
		if (sizeLimitInBits < VERTEX_SIZE)
			throw new IllegalArgumentException("Size limit must be at least " + VERTEX_SIZE);
		this.sizeLimitInBits = sizeLimitInBits;

		clockPointer = new BitArray();
		verticesStart = new ArrayList<>();
		verticesStart.add(0);
		leavesStart = new ArrayList<>();
		leavesStart.add(0);
		leavesStart.add(VERTEX_SIZE);
		vertices = new BitArray();
		leaves = new BitArray();
		leaves.pushBack(true);
		leaves.pushBack(true);
	}

	@Override
	public boolean hasAnythingProbably(BitArray left, BitArray right) {
		if (left.compareTo(right) > 0)
			throw new IllegalArgumentException("The left <= right inequality must hold true");
		Node node = navigateToLeaf(left, true);
		Node lastNode = navigateToLeaf(right, false);
		while (true) {
			node.setUsedBit(true);
			if (node.getOccupiedBit())
				return true;
			if (node.equals(lastNode))
				break;
			node = node.goToNextLeaf();
		}
		return false;
	}
	
	private void markEmpty(BitArray left, BitArray right) {
		Node node = navigateToLeaf(left, true);
		Node lastNode = navigateToLeaf(right, false);
		while (true) {
			node.setOccupiedBit(false);
			if (node.equals(lastNode))
				break;
			node = node.goToNextLeaf();
		}
	}

	@Override
	public void learnFalsePositive(BitArray left, BitArray right) {
		if (left.compareTo(right) > 0)
			throw new IllegalArgumentException("The left <= right inequality must hold true");
		Node node = navigateToLeaf(left, true);
		while (node.getDepth() < left.getSize() && node.getOccupiedBit()) {
			node.split();
			node = node.goForward(!left.get(node.getDepth()), -1);
		}
		node = navigateToLeaf(right, true);
		while (node.getDepth() < right.getSize() && node.getOccupiedBit()) {
			node.split();
			node = node.goForward(!right.get(node.getDepth()), -1);
		}
		markEmpty(left, right);
		shrink();
	}

	private void shrink() {
		Node node = navigateToLeaf(clockPointer, true);
		
		while (getSizeInBits() > sizeLimitInBits) {
			if (node.getUsedBit()) {
				node.setUsedBit(false);
				node = node.goToNextCyclicallyLeaf();
				continue;
			}

			Node sibling = node.goToSibling();
			if (!sibling.isLeaf()) {
				node = node.goToNextCyclicallyLeaf();
				continue;
			}

			boolean canCascade = sibling.getOccupiedBit() == node.getOccupiedBit();
			Node parent = node.goUp();
			parent.mergeSons(node.isLeftSon() ? node : sibling);
			node = parent;
			if (!canCascade)
				node = node.goToNextCyclicallyLeaf();
			break;
		}
		
		clockPointer = node.getPath();
	}

	// TODO : actual BitSets size is greater
	private int getSizeInBits() {
		return vertices.getSize() + leaves.getSize();
	}

	@Override
	public Object clone() {
		SimpleBitArf result = new SimpleBitArf();
		result.sizeLimitInBits = sizeLimitInBits;
		result.clockPointer = (BitArray)clockPointer.clone();
		result.verticesStart = new ArrayList<>(verticesStart);
		result.leavesStart = new ArrayList<>(leavesStart);
		result.vertices = (BitArray)vertices.clone();
		result.leaves = (BitArray)leaves.clone();
		return result;
	}
}
