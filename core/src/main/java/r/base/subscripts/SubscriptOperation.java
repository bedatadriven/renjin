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

package r.base.subscripts;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SubscriptOperation {

  private Vector source;
  private int sourceDim[];

  private boolean drop = true;

  private List<SEXP> subscriptArguments;

  public SubscriptOperation() {
  }

  public SubscriptOperation setSource(SEXP source) {
    if(source instanceof PairList.Node) {
      this.source = ((PairList.Node) source).toVector();
    } else {
      this.source = EvalException.checkedCast(source);
    }
    return this;
  }

  public SubscriptOperation setSubscripts(ListVector arguments, int skipBeginning, int skipEnd) {
    subscriptArguments = Lists.newArrayList();
    for(int i=skipBeginning; i+skipEnd<arguments.length();++i) {
      subscriptArguments.add(arguments.getElementAsSEXP(i));
    }
    return this;
  }

  public SubscriptOperation setDrop(boolean drop) {
    this.drop = drop;
    return this;
  }

  /**
   *
   * @return true if the source vector was an array
   * (had an explicit DIM attribute with length 1)
   */
  private boolean sourceIsArray() {
    return source.getAttribute(Symbol.DIM).length() == 1;
  }

  private int[] computeSourceDimensions() {

    SEXP dim = source.getAttribute(Symbol.DIM);
    if(dim == Null.INSTANCE) {
      sourceDim = new int[] { source.length() };
    } else if(dim instanceof IntVector) {
      sourceDim = ((IntVector) dim).toIntArray();

      if(subscriptArguments.size() == 1) {
        sourceDim = new int[] { source.length() };
      }
    } else {
      throw new AssertionError("DIM attribute must be NULL or an IntVector");
    }

    if( subscriptArguments.size()!=1 && subscriptArguments.size() != sourceDim.length) {
      throw new EvalException("incorrect number of dimensions");
    }
    return sourceDim;
  }

  public SEXP extract() {

    if(source instanceof AtomicVector && source.length() == 0) {
      return Null.INSTANCE;

    } else {
      computeSourceDimensions();
      Subscripts subscripts = new Subscripts();

      StringVector.Builder names = null;
      if(source.getAttribute(Symbol.NAMES) != Null.INSTANCE) {
        names = new StringVector.Builder();
      }
      Vector.Builder result = source.newBuilder(subscripts.getLength());
      int count = 0;

      for(Integer index : subscripts) {
        if(!IntVector.isNA(index) && index < source.length()) {
          result.setFrom(count++, source, index);
          if(names != null) {
            names.add(source.getName(index));
          }
        } else {
          result.setNA(count++);
          if(names != null) {
            names.addNA();
          }
        }
      }
      result.setAttribute(Attributes.DIM, subscripts.dimensionAttribute());
      if(names != null) {
        result.setAttribute(Attributes.NAMES, names.build());
      }
      return result.build();
    }
  }

  public SEXP replace(SEXP elements, boolean single) {

    // [[<- and [<- seem to have a special meaning when
    // the replacement value is NULL and the vector is a list
    if(source instanceof ListVector && elements == Null.INSTANCE) {
      return remove();

    } else if(subscriptArguments.size() == 1 && subscriptArguments.get(0) instanceof StringVector) {
      return replaceByName(elements, single);
    }

    Vector.Builder result = createReplacementBuilder(elements);
    computeSourceDimensions();
    Subscripts subscripts = new Subscripts();
    if(!subscripts.isEmpty() && elements.length() == 0) {
      throw new EvalException("replacement has zero length");
    }

    int replacement = 0;
    for(int index : subscripts) {
      assert index < source.length() || sourceDim.length == 1;
      if(!IntVector.isNA(index)) {
        if(single) {
          result.set(index, elements);
        } else {
          result.setFrom(index, elements, replacement++);
          if(replacement >= elements.length()) {
            replacement = 0;
          }
        }
      }
    }
    return result.build();
  }

  private Vector replaceByName(SEXP elements, boolean single) {
    StringVector namesToReplace = (StringVector) subscriptArguments.get(0);
    Vector.Builder result = createReplacementBuilder(elements);
    StringVector.Builder names = source.getNames() == Null.INSTANCE ? StringVector.newBuilder() :
        (StringVector.Builder) source.getNames().newCopyBuilder();

    int replacementIndex = 0;

    for(String nameToReplace : namesToReplace) {
      int index = source.getIndexByName(nameToReplace);
      if(index == -1) {
        index = result.length();
        names.set(index, nameToReplace);
      }
      if(single) {
        result.set(index, elements);
      } else {
        result.setFrom(index, elements, replacementIndex++);
      }
    }

    result.setAttribute(Attributes.NAMES, names.build());
    return result.build();
  }

  public Vector remove() {
    computeSourceDimensions();
    Subscripts subscripts = new Subscripts();
    Set<Integer> indicesToRemove = Sets.newHashSet();

    for(int index : subscripts) {
      if(!IntVector.isNA(index)) {
        indicesToRemove.add(index);
      }
    }

    Vector.Builder result = source.newBuilder(0);
    result.copyAttributesFrom(source);
    for(int i=0;i!=source.length();++i) {
      if(!indicesToRemove.contains(i)) {
        result.addFrom(source, i);
      }
    }
    return result.build();
  }


  private Vector.Builder createReplacementBuilder(SEXP elements) {
    Vector.Builder result;

    Vector.Type replacementType;
    if(elements instanceof AtomicVector) {
      replacementType = ((AtomicVector) elements).getVectorType();
    } else {
      replacementType = ListVector.VECTOR_TYPE;
    }

    if(source.getVectorType().isWiderThan(replacementType)) {
      result = source.newCopyBuilder();
    } else {
      result = replacementType.newBuilder();
      result.copyAttributesFrom(source);
      for(int i=0;i!= source.length();++i) {
        result.setFrom(i, source, i);
      }
    }
    return result;
  }


  private class Subscripts implements Iterable<Integer> {

    private Subscript subscripts[];
    private int[] dim;
    private int length;

    public Subscripts() {
      subscripts = new Subscript[subscriptArguments.size()];

      for(int i=0; i!=subscripts.length;++i) {
        SEXP argument = subscriptArguments.get(i);

        if(argument == Symbol.MISSING_ARG) {
          subscripts[i] = new MissingSubscript(sourceDim[i]);

        } else if(argument instanceof LogicalVector) {
          subscripts[i] = new LogicalSubscript(sourceDim[i], (LogicalVector)argument);

        } else if(argument instanceof StringVector) {
          subscripts[i] = new NamedSubscript(sourceDim[i], names(i), (StringVector)argument);

        } else if(argument instanceof DoubleVector || argument instanceof IntVector) {
          AtomicVector vector = (AtomicVector)argument;
          if(PositionalSubscript.arePositions(vector)) {
            subscripts[i] = new PositionalSubscript(vector);
          } else {
            subscripts[i] = new NegativeSubscript(sourceDim[i], vector);
          }
        } else {
          throw new EvalException("invalid subscript type '%s'", argument.getTypeName());
        }
      }

      this.dim = new int[sourceDim.length];
      length = 1;
      for(int d=0;d!=sourceDim.length;++d) {
        int count = subscripts[d].getCount();
        dim[d] = count;
        length *= count;
      }
    }

    private AtomicVector names(int dimensionIndex) {
      if(subscripts.length == 1 && !sourceIsArray()) {
        return source.getNames();
      } else {
        Vector dimNames = (Vector) source.getAttribute(Symbol.DIMNAMES);
        return dimNames.getElementAsSEXP(dimensionIndex);
      }
    }

    public int getLength() {
      return length;
    }

    public boolean isEmpty() {
      return length == 0;
    }

    private int[] dropUnitDimensions() {
      int newDim[] = new int[dim.length];
      int count = 0;
      for(int i=0;i!= dim.length;++i) {
        if(dim[i] > 1) {
          newDim[count++] = dim[i];
        }
      }
      return java.util.Arrays.copyOf(newDim, count);
    }

    public SEXP dimensionAttribute() {
      int attribute[];
      if(drop) {
        attribute = dropUnitDimensions();
      } else {
        attribute = dim;
      }
      if(attribute.length == 0) {
        return Null.INSTANCE;
      } else if(attribute.length == 1 && !sourceIsArray()) {
        return Null.INSTANCE;
      } else {
        return new IntVector(attribute);
      }
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
     * Iterators over the indices indicated by the subscripts
     */
    private class IndexIterator extends UnmodifiableIterator<Integer> {

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
        int sourceIndices[] = new int[sourceDim.length];
        for(int i=0;i!=sourceDim.length;++i) {
          sourceIndices[i] = subscripts[i].getAt(subscriptIndex[i]);
        }

        int index = Indexes.arrayIndexToVectorIndex(sourceIndices, sourceDim);
        hasNext = Indexes.incrementArrayIndex(subscriptIndex, dim);

        return index;
      }
    }
  }
}
