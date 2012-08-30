package org.renjin.primitives.subset;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Indexes;
import org.renjin.sexp.*;

import java.util.Iterator;
import java.util.List;

/**
 * When multiple subscripts are provided, they are treated as selecting/deselecting 
 * whole rows, columns or higher-level dimensions.
 * 
 * <p>For example, {@code x[1,2]} selects the intersection of the first row and second
 * column, while {@code x[1,]} selects the intersection of the first row and all columns,
 * thus the entire first row.
 * 
 */
public class DimensionSelection extends Selection {

  private final SEXP source;
  private final int sourceDim[];

  /**
   * Array containing the provided subscripts, for example:
   * <ul>
   * <li>[1,2] => [PositionalSubscript(1), PositionalSubscript(2)]</li>
   * <li>[,1:3] => [MissingSubscript, PositionalSubscript(1,2,3)]</li>
   * </ul>
   */
  private Subscript subscripts[];

  /**
   * The dimensions of the subscripts, for example:
   * <ul>
   * <li>given subscripts x[1,2], the subscript {@code dim}s are (1,1)</li>
   * <li>given subscripts x[1,1:10], the subscript {@code dim}s are (1,10)</li> 
   * <li>given a matrix {@code x} with {@code dim}s (3,4) and subscripts {@code x[,1]}, 
   *    the subscript {@code dim}s are (3,1)</li>
   * </ul>
   */
  private int[] dim;

  /**
   * The total number of elements indicated by the given subscripts. This is
   * equal to the product of {@code dim}.
   */
  private int elementCount;


  public DimensionSelection(SEXP source, List<SEXP> subscriptArguments) {
    super(source);
    
    Preconditions.checkArgument(subscriptArguments.size() > 1, 
        "ArrayElementSet at least 2 subscripts");

    this.source = source;
    this.sourceDim = dimAsIntArray(source);

    if( subscriptArguments.size() != sourceDim.length) {
      throw new EvalException("Incorrect number of dimensions. Subscripts: " + subscriptArguments.size() + ", source dimension: "+ sourceDim.length );
    }

    subscripts = new Subscript[subscriptArguments.size()];
    for(int i=0; i!=subscripts.length;++i) {
      subscripts[i] = parseSubscript(subscriptArguments.get(i), i, sourceDim[i]);
    }

    this.dim = new int[sourceDim.length];
    elementCount = 1;
    for(int d=0;d!=sourceDim.length;++d) {
      int count = subscripts[d].getCount();
      dim[d] = count;
      elementCount *= count;
    }

  }

  
  @Override
  public int getSourceDimensions() {
    return sourceDim.length;
  }

  @Override
  public int getElementCount() {
    return elementCount;
  }
  
  @Override
  public int[] getSubscriptDimensions() {
    return dim;
  }

  @Override
  public Iterator<Integer> iterator() {
    if(isEmpty()) {
      return Iterators.emptyIterator();
    } else {
      return new IndexIterator();
    }
  }


  /**
   * Iterators over the indices selected by the subscripts
   */
  private class IndexIterator extends UnmodifiableIterator<Integer> {

    /**
     * Indices within the subscript matrix. 
     * 
     * <p>If we are
     * given subscripts like [1,3:4], then we have a 
     * subscript matrix that looks like this:
     * 
     * <pre>
     * 1  3
     * 1  4
     * </pre>
     * 
     * {@code subscriptIndex} is the current position within 
     * this matrix.
     */
    private int subscriptIndex[];
    private boolean hasNext = true;

    private IndexIterator() {
      subscriptIndex = new int[dim.length];
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public Integer next() {
      // sourceIndices is the matrix coordinates of the 
      // the next value indicated by the subscripts

      int sourceIndices[] = new int[sourceDim.length];
      for(int i=0;i!=sourceDim.length;++i) {
        sourceIndices[i] = subscripts[i].getAt(subscriptIndex[i]);
      }

      // index refers to the position within the storage array
      int index = Indexes.arrayIndexToVectorIndex(sourceIndices, sourceDim);
      hasNext = Indexes.incrementArrayIndex(subscriptIndex, dim);

      return index;
    }
  }


  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    Vector dimNames = (Vector) source.getAttribute(Symbols.DIMNAMES);
    if(dimNames == Null.INSTANCE) {
      throw new EvalException("no 'dimnames' attribute for array");
    }
    return (AtomicVector)dimNames.getElementAsSEXP(dimensionIndex);
  }


  @Override
  public Iterable<Integer> getSelectionAlongDimension(int dimensionIndex) {
    final Subscript subscript = subscripts[dimensionIndex];
    final int length = subscript.getCount();
    return new Iterable<Integer>() {

      @Override
      public Iterator<Integer> iterator() {
        return new UnmodifiableIterator<Integer>() {
          int i = 0 ;
          @Override
          public boolean hasNext() {
            return i < length;
          }

          @Override
          public Integer next() {
            return subscript.getAt(i++);
          }
        };
      }
    };
  }
}
