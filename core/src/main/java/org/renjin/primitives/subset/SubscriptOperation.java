/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.subset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.eval.EvalException;
import org.renjin.iterator.IntIterator;
import org.renjin.primitives.subset.views.DoubleDenseMap;
import org.renjin.primitives.subset.views.DoubleMap;
import org.renjin.primitives.subset.views.DoubleReplace1;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SubscriptOperation {
  
  private static final int MAX_DENSE_MAP_LENGTH = 100;

  private Vector source;

  private boolean drop = true;

  private List<SEXP> subscripts;

  private Selection selection;
  
  public SubscriptOperation() {
  }

  public SubscriptOperation setSource(SEXP source, ListVector arguments, int skipBeginning, int skipEnd) {
    if(source instanceof PairList.Node) {
      this.source = ((PairList.Node) source).toVector();
    } else if(source instanceof Vector) {
      this.source = (Vector) source;
    } else {
      throw new EvalException("Invalid source: " + source);
    }
    subscripts = Lists.newArrayList();
    for(int i=skipBeginning; i+skipEnd<arguments.length();++i) {
      subscripts.add(arguments.getElementAsSEXP(i));
    }
    
    // how the subscripts are interpreted depends both on how many
    // and what kind of subscripts are provided, and the dimension of the
    // source vector
   
  
    if(subscripts.isEmpty()) {  
      selection = new CompleteSelection(source);
    
    } else if(subscripts.size() == 1) {
      
      SEXP subscript = subscripts.get(0);
      
      // if the single argument is a matrix or greater, then
      // we treat it as a matrix of coordinates
      if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {
        
        selection = new CoordinateMatrixSelection(source, subscript);
      
      } else {
      
        // otherwise we treat the source
        // as a vector, regardless of whether it has dimensions or not
        
        selection = new VectorIndexSelection(source, subscript);
      }
      
    } else {
      
      // otherwise we have multiple subscripts, and we treat each subscript
      // as applying as whole to its dimensions (including whole rows or columns
      // in the case of matrices)
      
      selection = new DimensionSelection(source, subscripts);
      
    }
      
    return this;
  }
  
  public SubscriptOperation setSource(SEXP source, ListVector arguments) {
    return setSource(source, arguments, 0, 0);
  }

  public SubscriptOperation setDrop(boolean drop) {
    this.drop = drop;
    return this;
  }

  public SEXP extractSingle() {

    // this seems like an abritrary limitation,
    // that is x[[TRUE]] happily takes the first item but
    // x[[1:2]] will throw an error, may be we can
    // just drop the distinction across the board?
    if(selection instanceof VectorIndexSelection ||
       selection instanceof CoordinateMatrixSelection) {
      if(selection.getElementCount() > 1) {
        throw new EvalException("attempt to select more than one element");
      }
    }

    if(selection.getElementCount() < 1) {
      throw new EvalException("attempt to select less than one element");
    }

    int index = selection.intIterator().nextInt();
    if(index < 0 || index >= source.length()) {
      throw new EvalException("subscript out of bounds");
    }
    return source.getElementAsSEXP(index);

  }

  public Vector extract() {

    if(source == Null.INSTANCE) {
      return Null.INSTANCE;

    } else if(source instanceof DoubleMap) {
      return ((DoubleMap)source).select(selection, computeExtractedAttributes());

    } else {

      Vector.Builder result = source.newBuilderWithInitialSize(selection.getElementCount());
      int count = 0;

      IntIterator it = selection.intIterator();
      while(it.hasNext()) {
        int index = it.nextInt();
        if(!IntVector.isNA(index) && index < source.length()) {
          result.setFrom(count++, source, index);
        } else {
          result.setNA(count++);
        }
      }
      result.setAttributes(computeExtractedAttributes());

      return result.build();
    }
  }

  private AttributeMap computeExtractedAttributes() {

    AttributeMap.Builder attributes = AttributeMap.builder();

    // COMPUTE DIM:
    attributes.set(Symbols.DIM, extractionDimension());

    // COMPUTE NAMES:
    // if only subscript is used, always draw names from the NAMES attribute
    // of the source
    if(subscripts.size() == 1 && !sourceIsSingleDimensionArray()) {
      // (no DIMs attribute)
      if(source.getAttribute(Symbols.NAMES) != Null.INSTANCE) {
        attributes.setNames(extractNames());
      }
    } else {
      // otherwise treat as an array and use dimnames
      IntArrayVector.Builder dim = new IntArrayVector.Builder();
      ListVector.Builder dimNames = new ListVector.Builder();
      boolean hasDimNames = false;
      int[] selectedDim = selection.getSubscriptDimensions();
      for(int d=0;d!=selectedDim.length;++d) {
        if(selectedDim[d] > 1 || !drop) {
          dim.add(selectedDim[d]);

          Vector dimNamesElement = selection.getDimensionNames(d);
          hasDimNames |= (dimNamesElement != Null.INSTANCE);
          dimNames.add(dimNamesElement);
        }
      }
      if(dim.length() > 1 || !drop) {
        attributes.setDim(dim.build());
        if(hasDimNames) {
          attributes.set(Symbols.DIMNAMES, dimNames.build());
        }
      } else {
        if(hasDimNames) {
          attributes.setNames((StringVector)dimNames.build().getElementAsSEXP(0));
        }
      }
    }
    return attributes.build();
  }

  private StringVector extractNames() {
    StringArrayVector.Builder names = new StringVector.Builder(0, selection.getElementCount());
    IntIterator it = selection.intIterator();
    while(it.hasNext()) {
      int index = it.nextInt();
      if(!IntVector.isNA(index) && index < source.length()) {
        names.add(source.getName(index));
      } else {
        names.addNA();
      }
    }
    return names.build();
  }

  private boolean sourceIsSingleDimensionArray() {
    return source.getAttribute(Symbols.DIM).length() == 1;
  }

  private Vector extractionDimension() {
    
    if(source.getAttribute(Symbols.DIM) == Null.INSTANCE) {
      // if the source has no dimension attribute,
      // the result will never have one
      
      return Null.INSTANCE;
      
    } else {
      
      int[] selectedDim = selection.getSubscriptDimensions();
      
      IntArrayVector.Builder result = new IntArrayVector.Builder();
      
      for(int i=0;i!=selectedDim.length;++i) {
        
        // by default, we ignore dimensions with length 1 unless
        // drop has been explicitly set to false
        if(!drop || selectedDim[i] != 1) {
          result.add(selectedDim[i]);
        }
      }
      
      if(result.length() == 0) {
        return Null.INSTANCE;
      }
     
      // we ONLY preserve a single-dimensioned array IF
      // drop = false OR
      // the source was also a single-dimensioned array
      if(drop && result.length() == 1 && 
          source.getAttribute(Symbols.DIM).length() != 1) {
        return Null.INSTANCE;
      } 
      
      return result.build();
    }
  }
  
  public Vector replace(SEXP elements) {

    // [[<- and [<- seem to have a special meaning when
    // the replacement value is NULL and the vector is a list
    if(source instanceof ListVector && elements == Null.INSTANCE) {
      return remove();

    } else if(subscripts.size() == 1 && subscripts.get(0) instanceof StringVector) {
      return replaceByName(elements);
    }
    
    if(!selection.isEmpty() && elements.length() == 0) {
      throw new EvalException("replacement has zero length");
    }

    Vector.Type replacementType = replacementResultType(elements);

    // if either the source or the replacement values are deferred, try to avoid computation
    if(elements instanceof  DeferredComputation || source instanceof DeferredComputation) {

      if(replacementType == DoubleVector.VECTOR_TYPE) {
        // try different strategies depending on the size / shape of data
        if(DoubleMap.accept(source, selection, (Vector)elements)) {
          return DoubleMap.replace(source, selection, (Vector)elements);
        }
        if(DoubleDenseMap.accept(source, selection, (Vector)elements)) {
          return DoubleDenseMap.replace(source, selection, (Vector)elements);
        }
      }
    }
    return materializeReplacement(elements, replacementType);
  }

  private Vector materializeReplacement(SEXP elements, Vector.Type replacementType) {
    Vector.Builder result = createReplacementBuilder(replacementType);

    int replacement = 0;
    IntIterator it = selection.intIterator();
    while(it.hasNext()) {
      int index = it.nextInt();
      assert index < source.length() || selection.getSourceDimensions() == 1;
      if(!IntVector.isNA(index)) {
        result.setFrom(index, elements, replacement++);
        if(replacement >= elements.length()) {
          replacement = 0;
        }
      }
    }
    return result.build();
  }


  private Vector replaceByName(SEXP elements) {
    StringVector namesToReplace = (StringVector) subscripts.get(0);
    Vector.Builder result = createReplacementBuilder(replacementResultType(elements));
    StringArrayVector.Builder names = source.getNames() == Null.INSTANCE ? StringVector.newBuilder() :
        (StringArrayVector.Builder) source.getNames().newCopyBuilder();

    int replacementIndex = 0;

    for(String nameToReplace : namesToReplace) {
      int index = source.getIndexByName(nameToReplace);
      if(index == -1) {
        index = result.length();
        names.set(index, nameToReplace);
      }

      result.setFrom(index, elements, replacementIndex++);

      if(replacementIndex >= elements.length()) {
        replacementIndex = 0;
      }
    }

    result.setAttribute(Symbols.NAMES, names.build());
    return result.build();
  }

  public Vector remove() {
    Set<Integer> indicesToRemove = Sets.newHashSet();

    IntIterator it = selection.intIterator();
    while(it.hasNext()) {
      int index = it.nextInt();
      if(!IntVector.isNA(index)) {
        indicesToRemove.add(index);
      }
    }

    Vector.Builder result = source.newBuilderWithInitialSize(0);
    result.copyAttributesFrom(source);
    for(int i=0;i!=source.length();++i) {
      if(!indicesToRemove.contains(i)) {
        result.addFrom(source, i);
      }
    }
    return result.build();
  }


  private Vector.Builder createReplacementBuilder(Vector.Type replacementType) {
    Vector.Builder result;

    if(source.getVectorType().isWiderThanOrEqualTo(replacementType)) {
      result = source.newCopyBuilder();
    } else {
      result = replacementType.newBuilderWithInitialSize(source.length());
      result.copyAttributesFrom(source);
      for(int i=0;i!= source.length();++i) {
        result.setFrom(i, source, i);
      }
    }
    return result;
  }

  private Vector.Type replacementResultType(SEXP elements) {
    Vector.Type replacementType;
    if(elements instanceof AtomicVector) {
      replacementType = ((AtomicVector) elements).getVectorType();
    } else {
      replacementType = ListVector.VECTOR_TYPE;
    }
    return replacementType;
  }
}
