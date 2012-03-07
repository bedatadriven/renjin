package org.renjin.primitives.subset;

import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;

/**
 * Encapsulates a set of elements that have been selected by 
 * the arguments to the subset function
 */
public abstract class Selection implements Iterable<Integer> {

  /**
   * 
   * @return the number of dimensions in the source object 
   * (depends not only on the DIM attribute of the provided vector
   * but the number of subscripts provided)
   */
  public abstract int getSourceDimensions();

  /**
   * 
   * @return the number of elements within this element set.
   */
  public abstract int getElementCount();

  /**
   * 
   * @return true if this ElementSet selects no elements
   */
  public boolean isEmpty() {
    return getElementCount() == 0;
  }

  /**
   * 
   * @return the {@code DIM} attribute for this ElementSet
   */
  public abstract int[] getSubscriptDimensions();


  protected abstract AtomicVector getNames(int dimensionIndex);
  
  
  protected final Subscript parseSubscript(SEXP argument, int dimensionIndex, int dimensionLength) {
    if(argument == Symbol.MISSING_ARG) {
      return new MissingSubscript(dimensionLength);

    } else if(argument instanceof LogicalVector) {
      return new LogicalSubscript(dimensionLength, (LogicalVector)argument);

    } else if(argument instanceof StringVector) {
      return new NamedSubscript(dimensionLength, getNames(dimensionIndex), (StringVector)argument);

    } else if(argument instanceof DoubleVector || argument instanceof IntVector) {
      AtomicVector vector = (AtomicVector)argument;
      if(PositionalSubscript.arePositions(vector)) {
        return new PositionalSubscript(vector);
      } else {
        return new NegativeSubscript(dimensionLength, vector);
      }
    } else if(argument == Null.INSTANCE) {
      return NullSubscript.INSTANCE;
    } else {
      throw new EvalException("invalid subscript type '%s'", argument.getTypeName());
    }
  }
  
  protected static int[] dimAsIntArray(SEXP source) {
    SEXP dim = source.getAttribute(Symbols.DIM);
    if(!(dim instanceof IntVector)) {
      return new int[] { source.length() };
    }
    return ((IntVector) dim).toIntArray();
  }


}
