package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.primitives.Indexes;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Selects elements using dimension coordinates like {@code x[1,2] or x[3,]}
 */
public class MatrixSelection implements Selection2 {

  private List<SEXP> subscripts;
  

  public MatrixSelection(List<SEXP> subscripts) {
    this.subscripts = subscripts;
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
  public Vector replaceElements(AtomicVector source, Vector replacements) {
    
    Subscript2[] subscripts = parseSubscripts(source);

    //   Subscript2[] subscripts = parseSubscripts(source, source);

    // Case 0: Single element x[1,3]

    // Case 1: Single row selection x[1, ]
    // Case 2: Multiple row selection x[1:2, ], x[-1, ]

    // Case 3: Single column selection x[, 1]    -> can be handled as offset + length
    // Case 4: Multiple column selection x[ , 1:2] 

    // Case 5: Complex pattern x[1:3, -3]



    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());
    int[] dim = source.getAttributes().getDimArray();
    
    if(dim.length == 2) {
      return buildMatrixReplacement(result, dim, subscripts, replacements);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private Vector buildMatrixReplacement(Vector.Builder result, int[] dim, Subscript2[] subscripts, Vector replacements) {

    IndexIterator2 columnIt = subscripts[1].computeIndexes();
    int columnLength = dim[1];

    int replacementIndex = 0;

    int columnIndex;
    while((columnIndex=columnIt.next())!=IndexIterator2.EOF) {
      int colStart = columnIndex * columnLength;
      
      IndexIterator2 rowIt = subscripts[0].computeIndexes();
      int rowIndex;
      while((rowIndex=rowIt.next())!=IndexIterator2.EOF) {
        result.setFrom(colStart + rowIndex, replacements, replacementIndex++);

        if(replacementIndex >= replacements.length()) {
          replacementIndex = 0;
        }
      }
    }
    return result.build();
  }


  private Subscript2[] parseSubscripts(SEXP source) {
    Subscript2[] array = new Subscript2[this.subscripts.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = parseSubscript(source, this.subscripts.get(i), i);
    }
    return array;
  }

  private Subscript2 parseSubscript(SEXP source, SEXP sexp, int dimensionIndex) {
    int[] dim = source.getAttributes().getDimArray();
    if(dimensionIndex >= dim.length) {
      throw new EvalException("incorrect number of dimensions");
    }
    
    if(sexp == Symbol.MISSING_ARG) {
      return new MissingSubscript2();

    } else if(sexp instanceof LogicalVector) {
      return new LogicalSubscript2((LogicalVector) sexp, dim[dimensionIndex]);

    } else if(sexp instanceof StringVector) {
      if(sexp.getAttributes().getDimNames() == Null.INSTANCE) {
        throw new EvalException("no 'dimnmaes' attribute for array");
      }
      ListVector dimNames = (ListVector) sexp.getAttributes().getDimNames();
      return new NameSubscript2((StringVector)sexp);
    
    } else if(sexp instanceof DoubleVector || sexp instanceof IntVector) {
      return new IndexSubscript((AtomicVector) sexp, dim[dimensionIndex]);

    } else {
      throw new EvalException("Invalid subscript type '%s'", sexp.getTypeName());
    }
  }

  private int computeUniqueIndex(SEXP source) {
    Subscript2[] subscripts = parseSubscripts(source);
    int[] index = new int[subscripts.length];
    for(int i=0;i!=subscripts.length;++i) {
      index[i] = subscripts[i].computeUniqueIndex();
    }
    return Indexes.arrayIndexToVectorIndex(index, source.getAttributes().getDimArray());
  }
  
}
