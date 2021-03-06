package arf;

import java.util.ArrayList;

public class SimpleBitArf implements IArf, Cloneable {
	private int sizeLimitInBits;
	private BitArray clockPointer;
	// TODO : start lists have actual size about O(max query length), is it a problem?
	private ArrayList<Integer> verticesStart, leavesStart;
	// TODO : the same argument applies to underlying BitSets, actual size can be slightly bigger
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
			if (!isLeaf())
				return;
			leaves.set(leavesShift + 1, value);
		}
		
		private int getAdditionalVerticesShift() {
			return verticesShift - verticesStart.get(depth);
		}
		
		private int getDepth() {
			return depth;
		}

		// TODO : try to reduce number of calculations of 'additionalVerticesShift'
		private Node goForward(boolean toLeft, int additionalVerticesShift) {
			if (additionalVerticesShift == -1)
				additionalVerticesShift = vertices.countOnes(verticesStart.get(depth), verticesShift) * 2;
			int newVerticesShift = verticesStart.get(depth + 1) + additionalVerticesShift;
			int additionalLeavesShift = (verticesShift - verticesStart.get(depth)) * 2 - additionalVerticesShift / 2;
			int newLeavesShift = leavesStart.get(depth + 1) + additionalLeavesShift;
			
			if (toLeft)
				return new Node(depth + 1, newVerticesShift, newLeavesShift, false, this);
			
			if (vertices.get(verticesShift)) {
				newVerticesShift += 2;
				newLeavesShift++;
			}
			else
				newLeavesShift += 2;
			
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
		
		public Node goToNextNode() {
			Node node = this;
			
			if (node.isLeaf()) {
				while (node.depth > 0 && !node.isLeftSon())
					node = node.goUp();
				if (node.depth == 0)
					return null;
				return node.goToSibling();
			}
			
			return node.goLeft(-1);
		}
		
		public Node goToNextLeaf() {
			Node node = goToNextNode();
			while (node != null && !node.isLeaf())
				node = node.goToNextNode();
			return node;
		}

		public Node goToNextCyclicallyLeaf() {
			Node node = goToNextLeaf();
			if (node == null) {
				node = new Node();
				while (!node.isLeaf())
					node = node.goLeft(-1);
			}
			return node;
		}

		public Node goToSibling() {
			if (isLeftSon())
				return parent.goRight(getAdditionalVerticesShift());
			return parent.goLeft(getAdditionalVerticesShift());
		}

		public void split() {
			boolean occupiedBit = getOccupiedBit();
			addToArray(leavesStart, depth + 1, -1);
			leaves.shiftSuffixLeft(leavesShift + 2, 1);
			
			addToArray(verticesStart, depth + 1, 2);
			vertices.shiftSuffixRight(verticesShift, 2);
			vertices.set(verticesShift, false);
			vertices.set(verticesShift + 1, false);
			
			if (parent != null)
				vertices.set(parent.verticesShift + (isLeftSon() ? 0 : 1), true);
			
			Node leftSon = goLeft(-1);
			addToArray(leavesStart, depth + 2, 4);
			leaves.shiftSuffixRight(leftSon.leavesShift, 4);
			leaves.set(leftSon.leavesShift, occupiedBit);
			leaves.set(leftSon.leavesShift + 2, occupiedBit);
			leaves.set(leftSon.leavesShift + 1, false);
			leaves.set(leftSon.leavesShift + 3, false);
		}
		
		public void mergeSons(Node leftSon) {
			boolean occupiedBit = leaves.get(leftSon.leavesShift) ||
					leaves.get(leftSon.leavesShift + 2) || leaves.get(leavesShift);
			addToArray(leavesStart, depth + 2, -4);
			leaves.shiftSuffixLeft(leftSon.leavesShift + 4, 4);
			
			if (parent != null)
				vertices.set(parent.verticesShift + (isLeftSon() ? 0 : 1), false);
			
			addToArray(verticesStart, depth + 1, -2);
			vertices.shiftSuffixLeft(verticesShift + 2, 2);
			
			addToArray(leavesStart, depth + 1, 1);
			leaves.shiftSuffixRight(leavesShift + 1, 1);
			leaves.set(leavesShift, occupiedBit);
			leaves.set(leavesShift + 1, false);
		}
		
		public boolean equals(Node other) {
			if (other == null)
				return false;
			return depth == other.depth && verticesShift == other.verticesShift &&
					leavesShift == other.leavesShift;
		}
	}
	
	public Node navigateToNode(BitArray path) {
		Node node = new Node();
		for (int i = 0; i < path.getSize() && !node.isLeaf(); i++)
			node = node.goForward(!path.get(i), -1);
		return node;
	}
	
	public Node navigateToExcludingNode(BitArray path) {
		Node node = navigateToNode(path);
		if (path.getSize() > node.getDepth())
			node = node.goToNextNode();
		return node;
	}
	
	public Node navigateToLeaf(BitArray path) {
		Node node = navigateToNode(path);
		while (!node.isLeaf())
			node = node.goLeft(-1);
		return node;
	}

	private SimpleBitArf() {}

	public SimpleBitArf(int sizeLimitInBits) {
		if (sizeLimitInBits < 2)
			throw new IllegalArgumentException("Size limit must be at least 2");
		this.sizeLimitInBits = sizeLimitInBits;

		clockPointer = new BitArray();
		verticesStart = new ArrayList<>();
		verticesStart.add(0);
		leavesStart = new ArrayList<>();
		leavesStart.add(0);
		leavesStart.add(2);
		vertices = new BitArray();
		leaves = new BitArray();
		leaves.pushBack(true);
		leaves.pushBack(true);
	}

	@Override
	public boolean hasAnythingProbably(BitArray left, BitArray right) {
		if (left.compareTo(right) >= 0)
			return false;
		Node lastNode = navigateToExcludingNode(right);
		for (Node node = navigateToNode(left); !node.equals(lastNode); node = node.goToNextNode()) {
			node.setUsedBit(true);
			if (node.getOccupiedBit())
				return true;
		}
		return false;
	}
	
	private void markEmpty(BitArray left, BitArray right) {
		Node lastNode = navigateToExcludingNode(right);
		for (Node node = navigateToNode(left); !node.equals(lastNode); node = node.goToNextNode())
			node.setOccupiedBit(false);
	}

	@Override
	public void learnFalsePositive(BitArray left, BitArray right) {
		if (left.compareTo(right) >= 0)
			return;
		Node node = navigateToNode(left);
		while (node.getDepth() < left.getSize() && node.getOccupiedBit()) {
			node.split();
			node = node.goForward(!left.get(node.getDepth()), -1);
		}
		node = navigateToNode(right);
		while (node.getDepth() < right.getSize() && node.getOccupiedBit()) {
			node.split();
			node = node.goForward(!right.get(node.getDepth()), -1);
		}
		markEmpty(left, right);
		shrink();
	}

	private void shrink() {
		Node node = navigateToLeaf(clockPointer);
		
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
			continue;
		}
		
		clockPointer = node.getPath();
	}

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