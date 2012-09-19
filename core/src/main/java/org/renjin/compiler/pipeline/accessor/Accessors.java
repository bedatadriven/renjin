package org.renjin.compiler.pipeline.accessor;

import org.renjin.DistanceMatrix;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.matrix.TransposingMatrix;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;

public class Accessors {

  public static Accessor create(DeferredNode node, InputGraph inputGraph) {
    if(node.getVector() instanceof DoubleArrayVector) {
      return new DoubleArrayAccessor(inputGraph.getOperandIndex(node));
    
    } else if(node.getVector() instanceof IntArrayVector) {
      return new IntArrayAccessor(inputGraph.getOperandIndex(node));
      
    } else if(UnaryVectorOpAccessor.accept(node)) {
      return new UnaryVectorOpAccessor(node, inputGraph);
    
    } else if(BinaryVectorOpAccessor.accept(node)) {
      return new BinaryVectorOpAccessor(node, inputGraph);
    
    } else if(node.getVector() instanceof TransposingMatrix) {
      return new TransposingAccessor(node, inputGraph);
    
    } else if(RepeatingAccessor.accept(node)) {
      return new RepeatingAccessor(node, inputGraph);
      
    } else if(node.getVector() instanceof DistanceMatrix) {
      return new DistanceMatrixAccessor(node, inputGraph);
//    } else if(node.isComputation()) {
//      return new ComputationAccessor(node, dataSlot);
    } else {
      return new VirtualAccessor(node.getVector(), inputGraph.getOperandIndex(node));
    }
  }
}
