package org.renjin.primitives.subset;

import com.google.common.collect.UnmodifiableIterator;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Indexes;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.*;

import java.util.Iterator;


/**
 * In the rarest of cases, the single subscript provided 
 * can be a matrix that contains cell coordinates in rows.
 */
public class CoordinateMatrixSelection extends Selection {

  
  private Matrix coordinateMatrix;
  private int sourceDim[];
  
  public static boolean isCoordinateMatrix(SEXP source, SEXP subscript) {
    
    if(!(subscript instanceof IntVector) &&
       !(subscript instanceof DoubleVector)) {
      return false;
    }
    
    Vector subscriptDim = subscript.getAttributes().getDim();
    if(subscriptDim.length() != 2) {
      return false;
    }
    
    // now check that the columns in the subscript match the number of
    // dimensions in the source.
    
    SEXP sourceDim = source.getAttribute(Symbols.DIM);
    return sourceDim.length() == subscriptDim.getElementAsInt(1);
  }
  
  public CoordinateMatrixSelection(SEXP source, SEXP subscript) {
    super(source);
    this.coordinateMatrix = new Matrix((Vector)subscript);
    this.sourceDim = dimAsIntArray(source);
    
    if(sourceDim.length != coordinateMatrix.getNumCols()) {
      throw new EvalException("The number of dimensions in the source (%d) does not " +
      		"match the number of columns in the provided coordinate matrix (%d)", 
      		sourceDim.length,
      		coordinateMatrix.getNumCols());
    }    
  }

  @Override
  public int getSourceDimensions() {
    return sourceDim.length;
  }

  @Override
  public int getElementCount() {
    return coordinateMatrix.getNumRows();
  }
  
  @Override
  protected AtomicVector getNames(int dimensionIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] getSubscriptDimensions() {
    return new int[] { getElementCount() };
  }
  
  private int[] getCoordinate(int row) {
    int[] coord = new int[sourceDim.length];
    for(int i=0;i!=coord.length;++i) {
      coord[i] = coordinateMatrix.getElementAsInt(row, i) - 1;
    }
    return coord;
  }

  @Override
  public Iterator<Integer> iterator() {
    
    return new UnmodifiableIterator<Integer>() {
      private int row = 0;
      
      @Override
      public boolean hasNext() {
        return row < coordinateMatrix.getNumRows();
      }

      @Override
      public Integer next() {
        return Indexes.arrayIndexToVectorIndex(getCoordinate(row++), sourceDim);
      }
    }; 
  }

  @Override
  public Iterable<Integer> getSelectionAlongDimension(final int dimensionIndex) {
    return new Iterable<Integer>() {
      
      @Override
      public Iterator<Integer> iterator() {
        return new UnmodifiableIterator<Integer>() {
          int i=0;
          
          @Override
          public boolean hasNext() {
            return i < coordinateMatrix.getNumRows();
          }

          @Override
          public Integer next() {
            return coordinateMatrix.getElementAsInt(i++, dimensionIndex);
          }
        };
      }
    };
  }
}
