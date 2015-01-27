package org.renjin.primitives.combine;


import java.util.List;

import org.renjin.primitives.combine.view.CombinedDoubleVector;
import org.renjin.primitives.combine.view.CombinedIntVector;
import org.renjin.primitives.combine.view.CombinedStringVector;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.sequence.RepStringVector;
import org.renjin.primitives.vector.PrefixedStringVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class LazyBuilder implements CombinedBuilder {

  private final Vector.Type vectorType;
  private int elementCount;
  private boolean useNames;
  private List<Vector> vectors;
  private List<Vector> nameVectors;
  private boolean hasNames = false;

  public LazyBuilder(Vector.Type vectorType, int elementCount) {
    this.vectorType = vectorType;
    this.elementCount = elementCount;
    this.vectors = Lists.newArrayListWithCapacity(elementCount);
  }

  @Override
  public CombinedBuilder useNames(boolean useNames) {
    if(useNames) {
      this.useNames = true;
      this.nameVectors = Lists.newArrayListWithCapacity(elementCount);
    }
    return this;
  }

  @Override
  public void add(String prefix, SEXP sexp) {
    throw new UnsupportedOperationException("LazyCombiner can only handle Vector inputs");
  }

  @Override
  public void addElements(String prefix, Vector vectorElement) {
    vectors.add(vectorElement);

    if(useNames) {
      nameVectors.add(composeNames(prefix, vectorElement));
    }
  }

  private Vector composeNames(String prefix, Vector vector) {

    Vector names = vector.getNames();
    int numElements = vector.length();

    if(!Strings.isNullOrEmpty(prefix) || (names !=  Null.INSTANCE && names != null)) {
      hasNames = true;
    }

    if(Strings.isNullOrEmpty(prefix) && (names ==  Null.INSTANCE || names == null)) {
      // both argument name and names() vector are absent
      return RepStringVector.createConstantVector("", numElements);

    } else if(Strings.isNullOrEmpty(prefix)) {
      // argument name is missing, but we have names() vector
      return names;

    } else if(names == Null.INSTANCE) {
      // have argument name, but no names() vector, return a1, a2, a3...
      return new PrefixedStringVector(prefix, new IntSequence(1,1,numElements), AttributeMap.EMPTY);

    } else {
      // we have both argument name and names() vector, return a.x, a.y, a.z
      return new PrefixedStringVector(prefix + ".", names, AttributeMap.EMPTY);
    }
  }

  @Override
public Vector build() {

    Vector[] vectors = toArray(this.vectors);
    if(vectorType == IntVector.VECTOR_TYPE) {
      return CombinedIntVector.combine(vectors, buildAttributes());

    } else if(vectorType == DoubleVector.VECTOR_TYPE) {
      return CombinedDoubleVector.combine(vectors, buildAttributes());

    } else if(vectorType == StringVector.VECTOR_TYPE) {
      return CombinedStringVector.combine(vectors, buildAttributes());

    } else {

      throw new UnsupportedOperationException("vector type: " + vectorType);
    }
  }

  private AttributeMap buildAttributes() {
    if(hasNames) {
      return new AttributeMap.Builder()
          .setNames(CombinedStringVector.combine(nameVectors, AttributeMap.EMPTY))
          .build();
    } else {
      return AttributeMap.EMPTY;
    }
  }

  private Vector[] toArray(List<Vector> list) {
    return list.toArray(new Vector[list.size()]);
  }
}
