package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Indexes;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Selects elements using dimension coordinates like {@code x[1,2] or x[3,]}
 */
class MatrixSelection implements SelectionStrategy {

  private List<SEXP> subscripts;
  
  public MatrixSelection(List<SEXP> subscripts) {
    this.subscripts = subscripts;
  }

  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {

    Subscript[] subscripts = parseSubscripts(source);
    
    int[] sourceDim = source.getAttributes().getDimArray();

    // We need random access to the array, so make sure that 
    // any deferred calculations are triggered.
    Vector materializedSource = context.materialize(source);

    // Build the vector with the selected elements
    ArrayIndexIterator it = new ArrayIndexIterator(sourceDim, subscripts);
    Vector.Builder result = source.getVectorType().newBuilder();
    int index;
    while((index=it.next())!= IndexIterator.EOF) {
      if(IntVector.isNA(index)) {
        result.addNA();
      } else {
        result.addFrom(materializedSource, index);
      }
    }
    
    // Calculate dimension of the subscript
    // For example, the expression m[1:3, 4:5] yields a selection of 
    // three rows and two columns, so subscriptDim = [3, 2]
    int[] subscriptDim = computeSubscriptDim(subscripts);
    
    // If drop = TRUE, then remove any redundant dimensions
    boolean[] droppedDim = dropRedundantDimensions(subscriptDim, drop);
    
    // Build the dimnames attribute for any remaining dimensions
    Vector dimNames = computeDimNames(source, subscripts, droppedDim);

    // If there is only a single dimension remaining, then drop
    // the dim and dimnames entirely
    // UNLESS, the input source was already one-dimensional
    int dimCount = countDims(droppedDim);
    if(drop && (dimCount == 0 || (dimCount == 1 && sourceDim.length > 1))) {

      // DO transform the dimnames to a names attribute if present
      if(dimNames.length() > 0) {
        result.setAttribute(Symbols.NAMES, dimNames.getElementAsSEXP(0));
      }
      
    } else {
      result.setAttribute(Symbols.DIM, buildDimAttribute(subscriptDim, droppedDim));
      result.setAttribute(Symbols.DIMNAMES, dimNames);
    }
    
    return result.build();
  }

  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {
    // The dim() attribute cannot be set on a lang object, so we shouldn't
    // need to check here
    assert call.getArguments().getAttribute(Symbols.DIM) == Null.INSTANCE;
    
    throw new EvalException("incorrect number of dimensions");
  }

  @Override
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    return getSingleElement(source);
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {
    return (AtomicVector)getSingleElement(source);
  }
  
  private SEXP getSingleElement(Vector source) {
    int index = computeUniqueIndex(source);

    SubsetAssertions.checkBounds(source, index);

    return source.getElementAsSEXP(index);
  }

  /**
   * Counts the number of dimensions that are not dropped.
   */
  private int countDims(boolean[] droppedDim) {
    int count = 0;
    for (int i = 0; i < droppedDim.length; i++) {
      if(!droppedDim[i]) {
        count++;
      }
    }
    return count;
  }

  /**
   * Drop any redundant dimensions.
   * 
   * <p>For example, if {@code x} is a 3x4 matrix, and you select {@code x[,1]}, then
   * the resulting matrix is 3x1. Unless {@code x[,1, drop=FALSE]} is specified, then
   * the second dimension of length 1 will be dropped.</p>
   * 
   * @param subscriptDim the dimension of the selected region
   * @param drop the drop flag from the {@code [} operator
   * @return an array of booleans of the same length as subscript dim, with the
   * a value of {@code true} for each dimension to be dropped.
   */
  private boolean[] dropRedundantDimensions(int[] subscriptDim, boolean drop) {
    boolean[] dropped = new boolean[subscriptDim.length];
    if(drop) {
      // If drop = TRUE, then drop any dimensions with a length of exactly one
      for (int i = 0; i < subscriptDim.length; i++) {
        if (subscriptDim[i] == 1) {
          dropped[i] = true;
        }
      }
    }
    return dropped;
  }

  private IntVector buildDimAttribute(int[] subscriptDim, boolean[] dropped) {
    IntArrayVector.Builder vector = new IntArrayVector.Builder(0, subscriptDim.length);
    for (int i = 0; i < subscriptDim.length; i++) {
      if(!dropped[i]) {
        vector.add(subscriptDim[i]);
      }
    }
    return vector.build();
  }

  private int[] computeSubscriptDim(Subscript[] subscripts) {
    int[] dim = new int[subscripts.length];
    for (int i = 0; i < subscripts.length; i++) {
      dim[i] = computeCount(subscripts[i]);
    }
    return dim; 
  }

  private int computeCount(Subscript subscript) {
    IndexIterator it = subscript.computeIndexes();
    int count = 0;
    while(it.next() != IndexIterator.EOF) {
      count++;
    }
    return count;
  }


  private Vector computeDimNames(Vector source, Subscript[] subscripts, boolean[] dropped) {
    Vector sourceDimNames = source.getAttributes().getDimNames();
    if(sourceDimNames == Null.INSTANCE) {
      return Null.INSTANCE;
    }
    ListVector.Builder newDimNames = ListVector.newBuilder();
    for (int d = 0; d < subscripts.length; d++) {
      if(!dropped[d]) {
        SEXP element = sourceDimNames.getElementAsSEXP(d);
        if (element instanceof StringVector) {
          StringVector sourceNames = ((StringVector) element);
          StringVector.Builder newNames = StringArrayVector.newBuilder();
          IndexIterator it = subscripts[d].computeIndexes();

          int index;
          while ((index = it.next()) != IndexIterator.EOF) {
            newNames.add(sourceNames.getElementAsString(index));
          }

          newDimNames.add(newNames.build());
        } else {
          newDimNames.add(Null.INSTANCE);
        }
      }
    }
    return newDimNames.build();
  }


  @Override
  public ListVector replaceSingleListElement(ListVector source, SEXP replacement) {
    
    if(replacement == Null.INSTANCE) {
      throw new EvalException("incompatible types (from NULL to list) in [[ assignment");
    }
    
    ListVector.NamedBuilder builder = source.newCopyNamedBuilder();
    int index = computeUniqueIndex(source);

    builder.set(index, replacement);
    
    return builder.build();
  }

  @Override
  public SEXP replaceSinglePairListElement(PairList.Node source, SEXP replacement) {
    // Note: assignment with NULL apparently allowed, at least as of GNU R 3.2.4
    
    PairList.Builder builder = source.newCopyBuilder();
    int index = computeUniqueIndex(source);

    builder.set(index, replacement);

    return builder.build();
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
   
    int index = computeUniqueIndex(source);
    if(replacement.length() != 1) {
      throw new EvalException("more elements supplied than there are to replace");
    }

    Vector.Builder builder = source.newCopyBuilder(replacement.getVectorType());
    builder.setFrom(index, replacement, 0);
    
    return builder.build();
  }


  @Override
  public Vector replaceListElements(ListVector list, Vector replacement) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {
    
    Subscript[] subscripts = parseSubscripts(source);


    ArrayIndexIterator it = new ArrayIndexIterator(source.getAttributes().getDimArray(), subscripts);
    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());
    
    Vector materializedReplacement = context.materialize(replacements);
    
    int replacementIndex = 0;
    int replacementLength = replacements.length();
    
    int index;
    while((index=it.next())!= IndexIterator.EOF) {
      
      result.setFrom(index, materializedReplacement, replacementIndex++);
      if(replacementIndex >= replacementLength) {
        replacementIndex = 0;
      }
    }
    
    if(replacementIndex != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }
    
    return result.build();
  }
  
  private Vector buildMatrixReplacement(Vector.Builder result, int[] dim, Subscript[] subscripts, Vector replacements) {

    IndexIterator columnIt = subscripts[1].computeIndexes();
    int columnLength = dim[1];

    int replacementIndex = 0;

    int columnIndex;
    while((columnIndex=columnIt.next())!= IndexIterator.EOF) {
      int colStart = columnIndex * columnLength;
      
      IndexIterator rowIt = subscripts[0].computeIndexes();
      int rowIndex;
      while((rowIndex=rowIt.next())!= IndexIterator.EOF) {
        result.setFrom(colStart + rowIndex, replacements, replacementIndex++);

        if(replacementIndex >= replacements.length()) {
          replacementIndex = 0;
        }
      }
    }
    return result.build();
  }


  private Subscript[] parseSubscripts(SEXP source) {
    Subscript[] array = new Subscript[this.subscripts.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = parseSubscript(source, this.subscripts.get(i), i);
    }
    return array;
  }

  private Subscript parseSubscript(SEXP source, SEXP sexp, int dimensionIndex) {
    int[] dim = source.getAttributes().getDimArray();
    if(dimensionIndex >= dim.length) {
      throw new EvalException("incorrect number of dimensions");
    }
    
    if(sexp == Symbol.MISSING_ARG) {
      return new MissingSubscript(dim[dimensionIndex]);

    } else if(sexp instanceof LogicalVector) {
      if(sexp.length() > dim[dimensionIndex]) {
        throw new EvalException("(subscript) logical subscript too long");
      }
      return new LogicalSubscript((LogicalVector) sexp, dim[dimensionIndex]);

    } else if(sexp instanceof StringVector) {
      Vector dimNamesList = source.getAttributes().getDimNames();
      if(dimNamesList == Null.INSTANCE) {
        throw new EvalException("no 'dimnames' attribute for array");
      }
      
      return new NameSubscript((StringVector)sexp, 
          (AtomicVector)dimNamesList.getElementAsSEXP(dimensionIndex), false);
    
    } else if(sexp instanceof DoubleVector || sexp instanceof IntVector) {
      return new IndexSubscript((AtomicVector) sexp, dim[dimensionIndex]);

    } else if(sexp == Null.INSTANCE) {
      return new IndexSubscript(Null.INSTANCE, dim[dimensionIndex]);
      
    } else {
      throw new EvalException("Invalid subscript type '%s'", sexp.getTypeName());
    }
  }

  private int computeUniqueIndex(SEXP source) {
    Subscript[] subscripts = parseSubscripts(source);
    int[] index = new int[subscripts.length];
    for(int i=0;i!=subscripts.length;++i) {
      index[i] = subscripts[i].computeUniqueIndex();
    }
    return Indexes.arrayIndexToVectorIndex(index, source.getAttributes().getDimArray());
  }
  
}
