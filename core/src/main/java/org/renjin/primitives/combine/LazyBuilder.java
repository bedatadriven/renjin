/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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

  public static boolean resultTypeSupported(Vector.Type vectorType) {
    return vectorType == IntVector.VECTOR_TYPE ||
           vectorType == DoubleVector.VECTOR_TYPE ||
           vectorType == StringVector.VECTOR_TYPE;
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
