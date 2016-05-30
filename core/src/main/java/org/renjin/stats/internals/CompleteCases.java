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
      int caseIndex = 0;
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          bitSet.clear(caseIndex);
        } 
        caseIndex ++;
        if(caseIndex == numCases) {
          caseIndex = 0;
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
      if(n == -1) {
        n = numCases(arg);
      } else {
        if(n != numCases(arg)) {
          throw new EvalException("not all arguments have the same length");
        }
      }
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
