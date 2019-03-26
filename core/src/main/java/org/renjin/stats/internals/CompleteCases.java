/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.stats.internals;

import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.BitSet;
import java.util.List;

public class CompleteCases {

  /**
   * Return a logical vector indicating which cases are complete, i.e., 
   * have no missing values.
   *
   * @param args a sequence of vectors, matrices and data frames.
   */
  @Internal("complete.cases")
  public static LogicalVector completeCases(@ArgumentList ListVector args) {
    
    List<AtomicVector> vectors = collectVectors(args);
  
    int numCases = countNumCases(vectors);
    
    /*
     * Create a bit vector, initialized with all bits 
     * for true for each case 
     */
    BitSet bitSet = allocBitVector(numCases);
    
    for(AtomicVector vector : vectors) {
      if(vector != Null.INSTANCE) {
        int caseIndex = 0;
        for (int i = 0; i != vector.length(); ++i) {
          if (vector.isElementNA(i)) {
            bitSet.clear(caseIndex);
          }
          caseIndex++;
          if (caseIndex == numCases) {
            caseIndex = 0;
          }
        }
      }
    }
    return new LogicalBitSetVector(bitSet, numCases);
  }

  private static BitSet allocBitVector(int numCases) {
    BitSet bitSet = new BitSet(numCases);
    for(int caseIndex=0;caseIndex!=numCases;++caseIndex) {
      bitSet.set(caseIndex);
    }
    return bitSet;
  }

  private static List<AtomicVector> collectVectors(ListVector args) {
    List<AtomicVector> variables = Lists.newArrayList();
    for(SEXP arg : args) {
      if(arg instanceof AtomicVector) {
        variables.add((AtomicVector) arg);
      } else if(arg instanceof ListVector) {
        for(SEXP vector : ((ListVector)arg)) {
          if(vector instanceof AtomicVector) {
            variables.add((AtomicVector) vector);
          } else {
            throw new EvalException("invalid list member type: " + vector.getTypeName());
          }
        }
      } else {
        throw new EvalException("invalid argument type: " + arg.getTypeName());
      }
    }
    return variables;
  }
  
  private static int countNumCases(List<AtomicVector> variables) {
    int n = -1;
    for(AtomicVector arg : variables) {
      if(arg != Null.INSTANCE) {
        if (n == -1) {
          n = numCases(arg);
        } else {
          if (n != numCases(arg)) {
            throw new EvalException("not all arguments have the same length");
          }
        }
      }
    }
    if(n == -1) {
      throw new EvalException("no input has determined the number of cases");
    }
    return n;
  }
  
  private static int numCases(AtomicVector vector) {
    Vector dim = vector.getAttributes().getDim();
    if(dim.length() == 2) {
      return dim.getElementAsInt(0);
    } else {
      
      // even if there is no dim attribute, or
      // there are more than 2 dimensions, we treat the input
      // as a plain vector
      return vector.length();
    }
  }
}
