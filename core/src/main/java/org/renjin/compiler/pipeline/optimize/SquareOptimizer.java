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
package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class SquareOptimizer implements Optimizer {

  public SquareOptimizer() {

  }

  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    if(isEligible(node)) {

      DeferredNode operand = node.getOperand(0);
      node.replaceVector(new Square(operand.getVector(), operand.getVector().getAttributes()));
      node.replaceOperands(operand);
      return true;
    }
    return false;
  }

  private boolean isEligible(DeferredNode node) {
    return node.isComputation() &&
           node.getComputation().getComputationName().equals("*") &&
           node.getOperand(0) == node.getOperand(1);
  }

  public static class Square extends DoubleVector implements DeferredComputation {
    private Vector vector;

    protected Square(Vector vector, AttributeMap attributes) {
      super(attributes);
      this.vector = vector;
    }

    @Override
    public Vector[] getOperands() {
      return new Vector[] { vector };
    }

    @Override
    public String getComputationName() {
      return "sqr";
    }

    @Override
    protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
      return new Square(vector, attributes);
    }

    @Override
    public double getElementAsDouble(int index) {
      return compute(vector.getElementAsDouble(index));
    }

    @Override
    public boolean isConstantAccessTime() {
      return true;
    }

    @Override
    public int length() {
      return vector.length();
    }

    public static double compute(double x) {
      return x * x;
    }

    @Override
    public boolean isDeferred() {
      return true;
    }
  }

}
