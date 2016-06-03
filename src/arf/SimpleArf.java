package arf;

import java.util.BitSet;

/*
 * Original ARF
 */

public class SimpleArf implements IArf {
	private final int LEFT_BOUND = Integer.MIN_VALUE;
	private final int RIGHT_BOUND = Integer.MAX_VALUE;
	private final int VERTEX_SIZE = 2;
	
	private int sizeLimitInBits;
	private int clockPointer;
	private int verticesSize, leavesSize;
	private int[] verticesStart, leavesStart;
	private BitSet vertices, leaves;

	private void addToArray(int[] array, int from, int value) {
		for (int i = from; i < array.length; i++)
			array[i] += value;
	}
	
	private class Node {
		private int depth;
		private int left, right;
		private int verticesShift, leavesShift;
		private Node parent;
		
		public Node() {
			depth = 0;
			left = LEFT_BOUND;
			right = RIGHT_BOUND;
			verticesShift = leavesShift = 0;
			parent = null;
		}
		
		public Node(int depth, int left, int right, int verticesShift, int leavesShift, Node parent) {
			this.depth = depth;
			this.left = left;
			this.right = right;
			this.verticesShift = verticesShift;
			this.leavesShift = leavesShift;
			this.parent = parent;
		}

		public int getLeft() {
			return left;
		}
		
		public int getRight() {
			return right;
		}
		
		public int getMiddle() {
			long sum = (long)left + right;
			if (sum >= 0)
				sum /= 2;
			else
				sum = sum / 2 - 1;
			return (int)sum;
		}
		
		public boolean isLeftSon() {
			return left == parent.left;
		}
		
		public boolean isLeaf() {
			if (depth == 0)
				return verticesSize == 0;
			return !vertices.get(parent.verticesShift + (isLeftSon() ? 0 : 1));
		}
		
		public boolean isLastLeaf() {
			return isLeaf() && right == RIGHT_BOUND;
		}
		
		private void assertLeaf() {
			if (!isLeaf())
				throw new IllegalArgumentException("Node must be a leaf");
		}
		
		private void assertVertex() {
			if (isLeaf())
				throw new IllegalArgumentException("Node must be a vertex");
		}
		
		public boolean getOccupiedBit() {
			assertLeaf();
			return leaves.get(leavesShift);
		}
		
		public void setOccupiedBit(boolean value) {
			assertLeaf();
			leaves.set(leavesShift, value);
		}
		
		public boolean getUsedBit() {
			assertLeaf();
			return leaves.get(leavesShift + 1);
		}

		public void setUsedBit(boolean value) {
			assertLeaf();
			leaves.set(leavesShift + 1, value);
		}
		
		private int getAdditionalVerticesShift() {
			return verticesShift - verticesStart[depth];
		}

		private Node goForward(boolean toLeft, int additionalVerticesShift) {
			assertVertex();
			
			if (additionalVerticesShift == -1)
				additionalVerticesShift = vertices.get(verticesStart[depth], verticesShift).cardinality() * VERTEX_SIZE;
			int newVerticesShift = verticesStart[depth + 1] + additionalVerticesShift;
			int additionalLeavesShift = (verticesShift - verticesStart[depth]) * VERTEX_SIZE - additionalVerticesShift;
			int newLeavesShift = leavesStart[depth + 1] + additionalLeavesShift;
			
			if (toLeft)
				return new Node(depth + 1, left, getMiddle(), newVerticesShift, newLeavesShift, this);
			
			if (vertices.get(verticesShift))
				newVerticesShift += VERTEX_SIZE;
			else
				newLeavesShift += VERTEX_SIZE;
			
			return new Node(depth + 1, getMiddle() + 1, right, newVerticesShift, newLeavesShift, this);
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
		
		public Node navigateToLeaf(int element) {
			Node node = this;
			int additionalVerticesShift = -1;
			while (!(node.left <= element && element <= node.right)) {
				additionalVerticesShift = node.getAdditionalVerticesShift();
				node = node.goUp();
			}
			while (!node.isLeaf()) {
				if (element <= node.getMiddle())
					node = node.goLeft(additionalVerticesShift);
				else
					node = node.goRight(additionalVerticesShift);
				additionalVerticesShift = -1;
			}
			return node;
		}
		
		public Node goToNextLeaf() {
			if (isLeaf()) {
				if (right == RIGHT_BOUND)
					throw new IllegalArgumentException("Node must not be a last leaf");
				return navigateToLeaf(right + 1);
			}
			return navigateToLeaf(left);
		}
		
		public Node goToSibling() {
			if (isLeftSon())
				return parent.goRight(getAdditionalVerticesShift());
			return parent.goLeft(getAdditionalVerticesShift());
		}

		public void split() {
			assertLeaf();
			if (left == right)
				throw new IllegalArgumentException("Node must have bigger than one-element range");
			
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
			assertVertex();
			if (vertices.get(verticesShift) || vertices.get(verticesShift + 1))
				throw new IllegalArgumentException("Both sons must be leaves");
			
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
	
	public SimpleArf(int sizeLimitInBits) {
		if (sizeLimitInBits < VERTEX_SIZE)
			throw new IllegalArgumentException("Size limit must be at least " + VERTEX_SIZE);
		this.sizeLimitInBits = sizeLimitInBits;
		
		verticesSize = 0;
		leavesSize = VERTEX_SIZE;
		clockPointer = LEFT_BOUND;
		verticesStart = new int[Integer.SIZE + 1];
		leavesStart = new int[Integer.SIZE + 1];
		for (int i = 1; i < leavesStart.length; i++)
			leavesStart[i] = VERTEX_SIZE;
		vertices = new BitSet();
		leaves = new BitSet();
		leaves.set(0);
		leaves.set(1);
	}
	
	public boolean hasAnythingProbably(BitArray leftBits, BitArray rightBits) {
		int left = leftBits.toInt();
		int right = rightBits.toInt();
		Node node = new Node().navigateToLeaf(left);
		while (true) {
			node.setUsedBit(true);
			if (node.getOccupiedBit())
				return true;
			if (right <= node.getRight())
				break;
			node = node.goToNextLeaf();
		}
		return false;
	}
	
	private void markEmpty(int left, int right) {
		Node node = new Node().navigateToLeaf(left);
		while (true) {
			node.setOccupiedBit(false);
			if (right <= node.getRight())
				break;
			node = node.goToNextLeaf();
		}
	}

	public void learnFalsePositive(BitArray leftBits, BitArray rightBits) {
		int left = leftBits.toInt();
		int right = rightBits.toInt();
		Node node = new Node().navigateToLeaf(left);
		while (node.getLeft() != left && node.getOccupiedBit()) {
			node.split();
			node = node.navigateToLeaf(left);
		}
		node = new Node().navigateToLeaf(right);
		while (node.getRight() != right && node.getOccupiedBit()) {
			node.split();
			node = node.navigateToLeaf(right);
		}
		markEmpty(left, right);
		while (getSizeInBits() > sizeLimitInBits)
			shrink();
	}
	
	private int addOneCyclic(int value) {
		return value == RIGHT_BOUND ? LEFT_BOUND : value + 1;
	}
	
	private void shrink() {
		Node node = new Node().navigateToLeaf(clockPointer);
		clockPointer = addOneCyclic(node.getRight());
		while (node.getUsedBit()) {
			node.setUsedBit(false);
			if (node.isLastLeaf())
				node = new Node().navigateToLeaf(LEFT_BOUND);
			else
				node = node.goToNextLeaf();
			clockPointer = addOneCyclic(node.getRight());
		}
		
		if (node.isLastLeaf())
			return;
		Node sibling = node.goToSibling();
		if (!sibling.isLeaf())
			return;
		
		if (sibling.getOccupiedBit() == node.getOccupiedBit())
			clockPointer = node.getLeft();
		node.parent.mergeSons(node.isLeftSon() ? node : sibling);
	}

	private int getSizeInBits() {
		return verticesSize + leavesSize;
	}
}
