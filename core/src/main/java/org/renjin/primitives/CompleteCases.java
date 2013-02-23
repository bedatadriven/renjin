package org.renjin.primitives;

import java.util.BitSet;

import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.ArgumentList;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.LogicalBitSetVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class CompleteCases {

  @Primitive("complete.cases")
  public static LogicalVector completeCases(@ArgumentList ListVector args) {
    int numCases = countNumCases(args);
    
    BitSet bitSet = new BitSet(numCases);
    for(int caseIndex=0;caseIndex!=numCases;++caseIndex) {
      bitSet.set(caseIndex);
    }
    for(int varIndex=0;varIndex!=args.length();++varIndex) {
      Vector vector = (Vector) args.getElementAsSEXP(varIndex);
      for(int caseIndex=0;caseIndex!=numCases;++caseIndex) {
        if(vector.isElementNA(caseIndex)) {
          bitSet.clear(caseIndex);
        } 
      }
    }
    return new LogicalBitSetVector(bitSet, numCases);
  }

  private static int countNumCases(ListVector args) {
    int n = -1;
    for(SEXP arg : args) {
      if(n == -1) {
        n = arg.length();
      } else {
        if(n != arg.length()) {
          throw new EvalException("not all arguments have the same length");
        }
       }
    }
    return n;
  }
}
