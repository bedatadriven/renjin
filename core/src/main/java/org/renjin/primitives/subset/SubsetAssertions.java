package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

/**
 * Common assertion checks for subset operations. 
 *
 */
public final class SubsetAssertions {

  private SubsetAssertions() {}
 
  /**
   * Checks that count == 1
   * 
   * @throws EvalException if {@code count != 1}
   */
  public static void checkUniqueCount(int count) {
    if(count < 1) {
      throw new EvalException("attempt to select less than one element");
    }
    if(count > 1) {
      throw new EvalException("attempt to select more than one element");
    }
  }

  /**
   * Checks that index is not {@code NA} and a valid index in {@code source}
   * 
   * @throws EvalException if {@code index} is out of bounds.
   */
  public static void checkBounds(SEXP source, int index) {
    if(IntVector.isNA(index) || index >= source.length()) {
      throw outOfBounds();
    }
  }

  /**
   * Checks that the given {@code sexp} has a length of exactly 1.
   * 
   * @throws EvalException if the length of {@code sexp} is not equal to one.
   */
  public static void checkUnitLength(SEXP sexp) {
    checkUniqueCount(sexp.length());
  }

  public static EvalException outOfBounds() {
    return new EvalException("subscript out of bounds");
  }
}
