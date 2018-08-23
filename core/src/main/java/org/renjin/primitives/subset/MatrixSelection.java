/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.subset;

import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Indexes;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Selects elements using dimension coordinates like {@code x[1,2] or x[3,]}
 */
public class MatrixSelection implements SelectionStrategy {

  private List<SEXP> subscripts;
  
  public MatrixSelection(List<SEXP> subscripts) {
    this.subscripts = subscripts;
  }

  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {

    Subscript[] subscripts = parseSubscripts(source);
    
    int[] sourceDim = source.getAttributes().getDimArray();

    Vector result = extractSubset(context, source, subscripts, sourceDim);

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

    AttributeMap.Builder attributes = new AttributeMap.Builder();

    int dimCount = countDims(droppedDim);
    if(drop && (dimCount == 0 || dimCount == 1)) {

      // DO transform the dimnames to a names attribute if present
      if(dimNames.length() > 0) {
        attributes.setNames(dimNames.<SEXP>getElementAsSEXP(0));
      }
      
    } else {
      attributes.setDim(buildDimAttribute(subscriptDim, droppedDim));
      attributes.setDimNames(dimNames);
    }
    
    return result.setAttributes(attributes);
  }

  private Vector extractSubset(Context context, Vector source, Subscript[] subscripts, int[] sourceDim) {

    if(source instanceof ArraySubsettable) {
      Vector result = ((ArraySubsettable) source).subscript(context, sourceDim, subscripts);
      if(result != null) {
        return result;
      }
    }

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
        if(index >= materializedSource.length()) {
          throw new EvalException("subscript out of bounds");
        }
        result.addFrom(materializedSource, index);
      }
    }
    return result.build();
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
  private static int countDims(boolean[] droppedDim) {
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
  private static boolean[] dropRedundantDimensions(int[] subscriptDim, boolean drop) {
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

  private static IntVector buildDimAttribute(int[] subscriptDim, boolean[] dropped) {
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
      dim[i] = subscripts[i].computeCount();
    }
    return dim; 
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
        if (element instanceof StringVector && element.length() != 0) {
          newDimNames.add(extractDimNames(subscripts[d], (StringVector)element));
        } else {
          newDimNames.add(Null.INSTANCE);
        }
      }
    }
    return newDimNames.build();
  }

  private StringVector extractDimNames(Subscript subscript, StringVector sourceNames) {

    if(subscript instanceof MissingSubscript) {
      return sourceNames;
    }

    StringVector.Builder newNames = StringArrayVector.newBuilder();
    IndexIterator it = subscript.computeIndexes();

    int index;
    while ((index = it.next()) != IndexIterator.EOF) {
      newNames.add(sourceNames.getElementAsString(index));
    }
    return newNames.build();
  }


  /**
   * Computes the {@code ValueBounds} of the result of this matrix selection based on available type information.
   * @param source
   * @param subscripts
   * @param drop
   * @return
   */
  public static ValueBounds computeResultBounds(ValueBounds source, List<ValueBounds> subscripts, boolean drop) {

    
    // What we can infer depends on what we know about the inputs. Specifically, it depends on three pieces
    // of information:
    // A. The source type
    // B. The subscriptDim (function of subscript lengths and possibly source dimensions)
    // C. The the source dim names
    
    // Depending on the inputs, there are three cases:
    // A only => result type only
    // B => result length and result dims
    // B+C => complete result attributes
    
    // Here we go:
    
    ValueBounds.Builder resultBounds = new ValueBounds.Builder();
    
    
    // (A) The type of the result will generally be the same as the source vector,
    // EXCEPT if the source is a pairlist, in which case it is first converted to a list
    resultBounds.setTypeSet(computeResultTypeBounds(source));
    resultBounds.setAttributeSetOpen(false);
    
    // (B) Compute the dimensions of the subscripts based on available
    // subscript types
    int[] subscriptDims = computeSubscriptDimBounds(source, subscripts);
    boolean subscriptDimsKnown = subscriptDims != null;
    
    
    // (C) Are the dimnames known (or known to be absent) ?
    Vector sourceDimNames = null;
    if(source.isAttributeConstant()) {
      sourceDimNames = source.getConstantAttributes().getDimNames();
    }
    
    // If we were able to infer the subscript dimensions, then we know the length
    // as well as the result dims
    if(subscriptDimsKnown) {
      resultBounds.setLength(computeLength(subscriptDims));

      boolean[] droppedDim = dropRedundantDimensions(subscriptDims, drop);
      int dimCount = countDims(droppedDim);

      AtomicVector resultDim;
      if (drop && (dimCount == 0 || dimCount == 1)) {
        // Drop the dim attribute. If the source has a dimnames attribute, this is converted
        // to a names attribute
        if (source.attributeCouldBePresent(Symbols.DIMNAMES)) {
          resultBounds.attributeCouldBePresent(Symbols.NAMES);
        }
      } else {
        // Keep the dim and (possibly) dimnames attribute
        resultBounds.setDimAttribute(buildDimAttribute(subscriptDims, droppedDim));
        if(source.attributeCouldBePresent(Symbols.DIMNAMES)) {
          resultBounds.attributeCouldBePresent(Symbols.DIMNAMES);
        }
      }
    } else {
      // all we know is what we could end up with
      resultBounds.attributeCouldBePresent(Symbols.DIM);
      if(source.attributeCouldBePresent(Symbols.DIMNAMES)) {
        resultBounds.attributeCouldBePresent(Symbols.DIMNAMES);
      }
    }

    return resultBounds.build();
  }

  private static int computeLength(int dim[]) {
    int length = 1;
    for (int i = 0; i < dim.length; i++) {
      length *= dim[i];
    }
    return length;
  }
  
  private static int computeResultTypeBounds(ValueBounds source) {
    
    // The only possible result types are vectors. 
    
    int resultTypeSet = source.getTypeSet() & TypeSet.ANY_VECTOR;

    // Pairlists are converted to ListVectors first, so if the input type set includes
    // a pair list, then the result type set must contain a list vector.

    if( (source.getTypeSet() & TypeSet.PAIRLIST) != 0) {
      resultTypeSet |= TypeSet.LIST;
    }
    return resultTypeSet;
  }

  private static int[] computeSubscriptDimBounds(ValueBounds source, List<ValueBounds> subscripts) {
    int subscriptDims[] = new int[subscripts.size()];

    // Are the dimensions of the source known? this will help
    // calculate the result values
    int sourceDim[] = null;
    if (source.isDimAttributeConstant()) {
      AtomicVector dimAttr = source.getConstantDimAttribute();
      if(dimAttr.length() != subscripts.size()) {
        throw new InvalidSyntaxException("incorrect number of dimensions");
      }
      sourceDim = dimAttr.toIntArray();
    }

    for (int i = 0; i < subscripts.size(); i++) {
      subscriptDims[i] = -1;
      ValueBounds subscript = subscripts.get(i);
      if(subscript.isConstant(Symbol.MISSING_ARG)) {
        // x[ , 1] for example
        if(sourceDim != null) {
          subscriptDims[i] = sourceDim[i];
        } else {
          // Without knowing dim(x), we don't know how many elements
          // will be selected.
          return null;
        }
      } else if(subscript.isLengthConstant() && TypeSet.isDefinitelyNumeric(subscript)) {
        subscriptDims[i] = subscript.getLength();
      } else {
        // we don't know enough about the subscript, could be anything
        return null;
      }
    }
    return subscriptDims;
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
  public Vector replaceSingleElement(Context context, AtomicVector source, Vector replacement) {
   
    if(replacement instanceof ListVector) {
      // Another special case...
      return replaceSingleAtomicVectorElementWithList(source, replacement);
    }
    
    int index = computeUniqueIndex(source);
    if(replacement.length() != 1) {
      throw new EvalException("more elements supplied than there are to replace");
    }

    Vector.Builder builder = source.newCopyBuilder(replacement.getVectorType());
    builder.setFrom(index, replacement, 0);
    
    return builder.build();
  }

  private Vector replaceSingleAtomicVectorElementWithList(AtomicVector source, Vector replacement) {

    // Note that we drop ALL attributes even though this is a matrix assignment...
    
    int indexToReplace = computeUniqueIndex(source);
    
    ListVector.Builder list = new ListVector.Builder();
    for (int i = 0; i < source.length(); i++) {
      if(i == indexToReplace) {
        list.add(replacement);
      } else {
        list.addFrom(source, i);
      }
    }
    return list.build();
  }

  @Override
  public ListVector replaceListElements(Context context, ListVector list, Vector replacement) {
    return (ListVector) replaceVectorElements(context, list, replacement);
  }

  @Override
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {
    return replaceVectorElements(context, source, replacements);
  }

  private Vector replaceVectorElements(Context context, Vector source, Vector replacements) {
    Subscript[] subscripts = parseSubscripts(source);

    ArrayIndexIterator it = new ArrayIndexIterator(source.getAttributes().getDimArray(), subscripts);
    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());

    Vector materializedReplacement = context.materialize(replacements);

    int replacementIndex = 0;
    int replacementLength = replacements.length();

    int index;
    while((index=it.next())!= IndexIterator.EOF) {
      if(!IntVector.isNA(index)) {
        if(index >= source.length()) {
          throw new EvalException("subscript out of bounds");
        }
        result.setFrom(index, materializedReplacement, replacementIndex++);
        if (replacementIndex >= replacementLength) {
          replacementIndex = 0;
        }
      }
    }

    if(replacementIndex != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
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
