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
package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.fusion.LoopKernelCompiler;
import org.renjin.compiler.pipeline.fusion.LoopKernels;
import org.renjin.compiler.pipeline.fusion.kernel.CompiledKernel;
import org.renjin.compiler.pipeline.fusion.kernel.LoopKernel;
import org.renjin.compiler.pipeline.fusion.node.*;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

import java.lang.reflect.Method;

/**
 * A {@code {@link DeferredNode} that represents a loop operation, such as {@code sum} or 
 * {@code mean} into which one more vector operations have been fused.
 */
public class FusedNode extends DeferredNode implements Runnable {

  private LoopKernel kernel;
  private LoopNode[] kernelOperands;
  
  private DoubleArrayVector resultVector;
  
  public FusedNode(FunctionNode node) {
    super();
    
    this.kernel = LoopKernels.INSTANCE.get(node);
    this.kernelOperands = new LoopNode[node.getOperands().size()];

    for (int i = 0; i < kernelOperands.length; i++) {
      kernelOperands[i] = addLoopNode(node.getOperand(i));
    }
  }
  
  private LoopNode addLoopNode(DeferredNode node) {


    // If this Deferred is a binary or unary vector operator, then 
    // we can inline into the loop
    
    if(node instanceof FunctionNode) {

      FunctionNode computation = (FunctionNode) node;

      int arity = node.getOperands().size();

      if (arity == 1) {
        Method unaryOperator = UnaryVectorOpNode.findMethod(node.getVector());
        if (unaryOperator != null) {
          return new UnaryVectorOpNode(
              computation.getComputationName(), 
              unaryOperator, addLoopNode(node.getOperand(0)));
        }
      }

      if (arity == 2) {
        Method binaryOperator = BinaryVectorOpNode.findMethod(node.getVector());
        if (binaryOperator != null) {
          return new BinaryVectorOpNode(
              computation.getComputationName(),
              binaryOperator,
              addLoopNode(node.getOperand(0)),
              addLoopNode(node.getOperand(1)));
        }
      }
    }
    
    
    // Otherwise we expect that it will be computed and stored
    // into an array before this DeferredNode is evaluated.

    return addLoopInput(node);
  }

  private LoopNode addLoopInput(DeferredNode node) {
    int inputIndex = this.addInput(node);
    node.addOutput(this);

    if(node.getVector() instanceof IntBufferVector) {
      return new IntBufferNode(inputIndex);
    }
    
    if(node.getVector() instanceof IntSequence) {
      return new IntSeqNode(inputIndex);
    }
    
    if(node.getVector() instanceof RepDoubleVector) {
      return new RepeatingNode(
          addLoopNode(node.getOperand(0)),
          addLoopNode(node.getOperand(1)));
    }

    if(node.getVector() instanceof DoubleVector) {
      return new DoubleArrayNode(inputIndex, node.getResultVectorType());
    }

    if(node.getVector() instanceof IntVector) {
      return new IntArrayNode(inputIndex, node.getResultVectorType());
    }
    
    if(node.getVector() instanceof LogicalVector) {
      return new IntArrayNode(inputIndex, node.getResultVectorType());
    }

    throw new UnsupportedOperationException("operand: " + node.getVector().getClass().getName());
  }
  
  @Override
  public String getDebugLabel() {
    return kernel.debugLabel(kernelOperands);
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.ELLIPSE;
  }

  @Override
  public Type getResultVectorType() {
    return Type.getType(DoubleArrayVector.class);
  }

  @Override
  public void run() {

    Vector[] vectorOperands = new Vector[getOperands().size()];
    for (int i = 0; i < vectorOperands.length; i++) {
      vectorOperands[i] = getOperand(i).getVector();
    }
    
    LoopKernelCompiler compiler = new LoopKernelCompiler();
    CompiledKernel compiledKernel = compiler.compile(kernel, kernelOperands);

    double[] result = compiledKernel.compute(vectorOperands);
    resultVector = new DoubleArrayVector(result);
  }
  
  public DoubleArrayVector getVector() {
    if(resultVector == null) {
      throw new IllegalStateException("Not computed yet.");
    }
    return resultVector;
  }
}
