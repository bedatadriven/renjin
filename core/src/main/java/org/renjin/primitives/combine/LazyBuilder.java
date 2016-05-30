package org.renjin.primitives.combine;


import org.renjin.primitives.combine.view.CombinedDoubleVector;
import org.renjin.primitives.combine.view.CombinedIntVector;
import org.renjin.primitives.combine.view.CombinedStringVector;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.List;

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
      nameVectors.add(CombinedNames.combine(prefix, vectorElement));
      if(CombinedNames.hasNames(prefix, vectorElement)) {
        hasNames = true;
      }
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
