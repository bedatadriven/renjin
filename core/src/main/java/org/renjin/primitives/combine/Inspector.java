/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.renjin.sexp.*;

/**
 * Inspects a list of elements to combine to determine the result type
 * and the most efficient merge
 */
class Inspector extends SexpVisitor<Vector.Type> {

  public static final int DEFERRED_THRESHOLD = 2000;
  public static final int DEFERRED_ARGUMENT_LIMIT = 200;

  private boolean recursive = false;

  /**
   * Total number of vectors found
   */
  private int vectorCount = 0;

  /**
   * Total number of elements within all vectors found
   */
  private int elementCount = 0;
  private Vector.Type resultType = Null.VECTOR_TYPE;

  private boolean deferredElements = false;

  /**
   * Visits each element of {@code ListExp}
   */
  Inspector(boolean recursive) {
    this.recursive = recursive;
  }

  @Override
  public void visit(DoubleVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    deferredElements = deferredElements || vector.isDeferred();
    vectorCount++;
  }

  @Override
  public void visit(IntVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    deferredElements = deferredElements || vector.isDeferred();
    vectorCount++;
  }

  @Override
  public void visit(LogicalVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    deferredElements = deferredElements || vector.isDeferred();
    vectorCount++;
  }

  @Override
  public void visit(Null nullExpression) {
    // ignore
  }

  @Override
  public void visit(StringVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    deferredElements = deferredElements || vector.isDeferred();
    vectorCount++;
  }

  @Override
  public void visit(ComplexVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    deferredElements = deferredElements || vector.isDeferred();
    vectorCount++;
  }

  @Override
  public void visit(RawVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    deferredElements = deferredElements || vector.isDeferred();
    vectorCount++;
  }

  @Override
  public void visit(ListVector list) {
    if(recursive) {
      acceptAll(list);
    } else {
      resultType = Vector.Type.widest(resultType, list);
      elementCount += list.length();
      vectorCount++;
    }
  }

  @Override
  protected void unhandled(SEXP exp) {
    resultType = Vector.Type.widest(resultType, ListVector.VECTOR_TYPE);
    elementCount++;
    vectorCount++;
  }

  public CombinedBuilder newBuilder() {
    if (LazyBuilder.resultTypeSupported(resultType)) {
      if( deferredElements ||
              (elementCount > DEFERRED_THRESHOLD &&
               vectorCount <= DEFERRED_ARGUMENT_LIMIT)) {

        return new LazyBuilder(resultType, vectorCount);
      }
    }

    return new MaterializedBuilder(resultType);
  }

  @Override
  public Vector.Type getResult() {
    return resultType;
  }

}
