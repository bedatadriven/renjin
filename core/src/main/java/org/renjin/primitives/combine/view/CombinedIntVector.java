package org.renjin.primitives.combine.view;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class CombinedIntVector extends IntVector implements DeferredComputation {

	private final Vector[] vectors;
	private final int endIndex[];
	private final int totalLength;

	public static IntVector combine(Vector[] vectors, AttributeMap attributeMap) {
		if (vectors.length == 1) {
			return (IntVector) vectors[0].setAttributes(attributeMap);
		} else {
			return new CombinedIntVector(vectors, attributeMap);
		}
	}

	private CombinedIntVector(Vector[] vectors, AttributeMap attributeMap) {
		super(attributeMap);

		this.vectors = vectors;
		this.endIndex = new int[vectors.length];

		int totalLength = 0;
		for (int i = 0; i != vectors.length; ++i) {
			totalLength += vectors[i].length();
			endIndex[i] = totalLength;
		}
		this.totalLength = totalLength;
	}

	@Override
	public Vector[] getOperands() {
		return vectors;
	}

	@Override
	public String getComputationName() {
		return "c";
	}

	@Override
	protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
		return new CombinedIntVector(vectors, attributes);
	}

	@Override
	public int getElementAsInt(int index) {
		if (index < endIndex[0])
			return vectors[0].getElementAsInt(index);
		if (index < endIndex[1])
			return vectors[1].getElementAsInt(index - endIndex[0]);
		if (index < endIndex[2])
			return vectors[2].getElementAsInt(index - endIndex[1]);
		if (index < endIndex[3])
			return vectors[3].getElementAsInt(index - endIndex[2]);
		for (int i = 4; i < vectors.length; ++i) {
			if (index < endIndex[i]) {
				return vectors[i].getElementAsInt(index - endIndex[i-1]);
			}
		}
		throw new IllegalArgumentException("index: " + index);
	}

	@Override
	public boolean isConstantAccessTime() {
		return true;
	}

	@Override
	public int length() {
		return totalLength;
	}
}
