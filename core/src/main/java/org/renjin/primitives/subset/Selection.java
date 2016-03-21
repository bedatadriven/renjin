package org.renjin.primitives.subset;

import com.google.common.collect.Sets;
import org.renjin.eval.EvalException;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.util.Set;


/**
 * Encapsulates a set of elements that have been selected by 
 * the arguments to the subset function.
 */
public abstract class Selection {

  private final SEXP source;
 
  public Selection(SEXP source) {
    super();
    this.source = source;
  }

  /**
   * 
   * @return the number of dimensions in the source object 
   * (depends not only on the DIM attribute of the provided vector
   * but the number of subscripts provided)
   */
  public abstract int getSourceDimensions();

  /**
   * 
   * @return an iterator over all the selected source indices.
   */
  public abstract IndexIterator iterator();
  
  public abstract IndexIterator getSelectionAlongDimension(int dimensionIndex);
  
  /**
   * 
   * @return true if this ElementSet selects no elements
   */
  public boolean isEmpty() {
    return !iterator().hasNext();
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

  public Vector getDimensionNames(int dimIndex) {
    Vector dimNames = (Vector) source.getAttribute(Symbols.DIMNAMES);
    if(dimNames != Null.INSTANCE) {
      Vector sourceNames = RowNamesVector.purify(dimNames.getElementAsSEXP(dimIndex));
      if(sourceNames != Null.INSTANCE) {
        return selectDimensionNames(dimIndex, sourceNames);
      }
    }
    return Null.INSTANCE;
  }

  protected Vector selectDimensionNames(int dimIndex, Vector sourceNames) {
    StringArrayVector.Builder names = new StringArrayVector.Builder();
    IndexIterator it = getSelectionAlongDimension(dimIndex);
    while(it.hasNext()) {
      int index = it.next();
      if(index >= sourceNames.length()) {
        throw new EvalException("subscript out of bounds: ");
      }
      names.add( sourceNames.getElementAsString(index) );
    }
    return names.build();
  }

  public IndexPredicate computePredicate() {
    final Set<Integer> selected = Sets.newHashSet();

    IndexIterator it = iterator();
    while(it.hasNext()) {
      int index = it.next();
      if(!IntVector.isNA(index)) {
        selected.add(index);
      }
    }
    return new IndexPredicate() {
      @Override
      public boolean apply(int index) {
        return selected.contains(index);
      }
    };
  }
  
  public boolean isByName() {
    return false;
  }
  
  public StringVector getNames() {
    throw new UnsupportedOperationException();
  }

}
