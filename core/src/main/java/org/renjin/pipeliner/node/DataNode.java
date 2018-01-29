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
package org.renjin.pipeliner.node;

import org.renjin.primitives.vector.DeferredFunction;
import org.renjin.primitives.vector.MemoizedVector;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Vector;

/**
 * A node in the graph that is already computed, for example, 
 * a DoubleArrayVector.
 */
public class DataNode extends DeferredNode {

  private Vector vector;
  
  public DataNode(Vector vector) {
    super();
    if(vector instanceof MemoizedVector) {
      this.vector = ((MemoizedVector) vector).forceResult();
    } else {
      this.vector = vector;
    }
  }

  @Override
  public String getDebugLabel() {
    if(vector.length() == 1) {
      if(vector.isElementNA(0)) {
        return "NA";
      }
      if(vector instanceof IntVector) {
        return vector.getElementAsInt(0) + "L";
      }
      if(vector instanceof DoubleVector) {
        return Double.toString(vector.getElementAsDouble(0));
      } 
      if(vector instanceof LogicalVector) {
        return vector.getElementAsRawLogical(0) == 0 ? "F" : "T";
      }
    } 
    return "[" + vector.length() + "]";
  }

  @Override
  public Vector getVector() {
    return vector;
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.BOX;
  }

  @Override
  public Type getResultVectorType() {
    return Type.getType(vector.getClass());
  }

  @Override
  public boolean hasValue(double x) {
    return vector.length() == 1 && vector.getElementAsDouble(0) == x;
  }

  public boolean equivalent(DeferredNode other) {
    if(!(other instanceof DataNode)) {
      return false;
    }
    DataNode otherData = (DataNode) other;
    Vector.Type vectorType = this.getVector().getVectorType();
    if(!vectorType.equals(otherData.vector.getVectorType())) {
      return false;
    }
    if(this.vector.length() > 10 || this.vector.length() != otherData.vector.length()) {
      return false;
    }
    for (int i = 0; i < this.vector.length(); i++) {
      if(vectorType.compareElements(this.vector, i, otherData.vector, i) != 0) {
        return false;
      }
    }
    return true;
  }

}
