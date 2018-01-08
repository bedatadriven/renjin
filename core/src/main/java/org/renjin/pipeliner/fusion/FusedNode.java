/**
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
package org.renjin.pipeliner.fusion;

import org.renjin.eval.EvalException;
import org.renjin.pipeliner.fusion.kernel.CompiledKernel;
import org.renjin.pipeliner.fusion.kernel.LoopKernel;
import org.renjin.pipeliner.fusion.node.*;
import org.renjin.pipeliner.node.DeferredNode;
import org.renjin.pipeliner.node.FunctionNode;
import org.renjin.pipeliner.node.NodeShape;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A {@code {@link DeferredNode } that represents a loop operation, such as {@code sum} or
 * {@code mean} into which one more vector operations have been fused.
 */
public class FusedNode extends DeferredNode implements Runnable {

  private LoopKernel kernel;
  private LoopNode[] kernelOperands;

  private MemoizedComputation memoizedComputation;
  private DoubleArrayVector resultVector;
  private Future<CompiledKernel> compiledKernel;

  public FusedNode(FunctionNode node) {
    super();

    this.kernel = LoopKernels.INSTANCE.get(node);
    this.kernelOperands = new LoopNode[node.getOperands().size()];
    this.memoizedComputation = (MemoizedComputation) node.getVector();

    for (int i = 0; i < kernelOperands.length; i++) {
      kernelOperands[i] = addLoopNode(node.getOperand(i));
    }
  }
  
  private LoopNode addLoopNode(DeferredNode node) {


    // Fused nodes are not available yet, but their result will be always
    // be a DoubleArrayVector

    if(node instanceof FusedNode) {
      int inputIndex = this.addInput(node);
      node.addOutput(this);

      return new DoubleArrayNode(inputIndex, Type.getType(DoubleArrayVector.class));
    }

    // If this Deferred is a binary or unary vector operator, then 
    // we can inline into the loop
    
    if(node instanceof FunctionNode) {

      FunctionNode computation = (FunctionNode) node;
      String name = computation.getComputationName();

      if(name.equals("dist")) {
        return new DistanceMatrixNode(addLoopNode(computation.getOperand(0)));
      }

      if(name.equals("rep")) {
        return new RepeatingNode(
            addLoopNode(node.getOperand(0)),
            addLoopNode(node.getOperand(1)));
      }

      if(name.equals("t")) {
        return new TransposeNode(
            addLoopNode(node.getOperand(0)),
            addLoopNode(node.getOperand(1)));
      }

      int arity = node.getOperands().size();

      if (arity == 1) {
        Method unaryOperator = UnaryVectorOpNode.findMethod(node.getVector());
        if (unaryOperator != null) {
          return new UnaryVectorOpNode(
              name,
              unaryOperator, addLoopNode(node.getOperand(0)));
        }
      }

      if (arity == 2) {
        Method binaryOperator = BinaryVectorOpNode.findMethod(node.getVector());
        if (binaryOperator != null) {
          return new BinaryVectorOpNode(
              name,
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

    if(node.getVector() instanceof DoubleArrayVector) {
      return new DoubleArrayNode(inputIndex, node.getResultVectorType());
    }

    if(node.getVector() instanceof IntArrayVector) {
      return new IntArrayNode(inputIndex, node.getResultVectorType());
    }
    
    if(node.getVector() instanceof LogicalArrayVector) {
      return new IntArrayNode(inputIndex, node.getResultVectorType());
    }

    return new VirtualVectorNode(inputIndex, node.getVector());
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

  public void startCompilation(LoopKernelCache loopKernelCache) {
    this.compiledKernel = loopKernelCache.get(kernel, kernelOperands);
  }

  @Override
  public void run() {

    CompiledKernel kernel;
    try {
      kernel = compiledKernel.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new EvalException("Exception compiling kernel", e);
    }

    Vector[] vectorOperands = new Vector[getOperands().size()];
    for (int i = 0; i < vectorOperands.length; i++) {
      vectorOperands[i] = getOperand(i).getVector();
    }

    double[] result = kernel.compute(vectorOperands);

    resultVector = DoubleArrayVector.unsafe(result, memoizedComputation.getAttributes());

    memoizedComputation.setResult(resultVector);
  }
  
  public DoubleArrayVector getVector() {
    if(resultVector == null) {
      throw new IllegalStateException("Not computed yet.");
    }
    return resultVector;
  }


}
