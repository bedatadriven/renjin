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
package org.renjin.compiler.pipeline.accessor;

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
      
//    } else if(node.getVector() instanceof DistanceMatrix) {
//      return new DistanceMatrixAccessor(node, inputGraph);
////    } else if(node.isComputation()) {
////      return new ComputationAccessor(node, dataSlot);
    } else {
      return new VirtualAccessor(node.getVector(), inputGraph.getOperandIndex(node));
    }
  }
}
