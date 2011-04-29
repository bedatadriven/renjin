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

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.List;

public class SubscriptOperation {


  private Vector source;
  private int sourceDim[];
  private boolean sourceIsArray = false;

  private boolean drop = true;

  private List<SEXP> subscriptArguments;
  private Subscript subscripts[];

  private int[] subscriptDim;
  private int subscriptLength;

  public SubscriptOperation() {

  }

  public SubscriptOperation setSource(SEXP source) {
    this.source = EvalException.checkedCast(source);
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

  private void computeSourceDimensions() {

    SEXP sourceDimExp = source.getAttribute(Symbol.DIM);
    if(sourceDimExp == Null.INSTANCE) {
      sourceDim = new int[] { source.length() };
    } else if(sourceDimExp instanceof IntVector) {
      sourceDim = ((IntVector) sourceDimExp).toIntArray();
      if(sourceDim.length == 1) {
        sourceIsArray = true;
      }
      if(subscriptArguments.size() == 1) {
        sourceDim = new int[] { source.length() };
      }
    } else {
      throw new AssertionError("DIM attribute must be NULL or an IntVector");
    }


    if( subscriptArguments.size()!=1 && subscriptArguments.size() != sourceDim.length) {
      throw new EvalException("incorrect number of dimensions");
    }

  }

  public SEXP extract() {

    if(source.length() == 0) {
      return Null.INSTANCE;
    }

    computeSourceDimensions();
    buildSubscripts();

    StringVector.Builder names = null;
    if(source.getAttribute(Symbol.NAMES) != Null.INSTANCE) {
      names = new StringVector.Builder();
    }

    computeSubscriptDim();
    Vector.Builder result = source.newBuilder(subscriptLength);

    if(subscriptLength > 0) {
      int count = 0;
      int[] subscriptIndex = new int[sourceDim.length];
      do {
        int index = subscriptIndexToSource(subscriptIndex);
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

      } while(Indexes.incrementArrayIndex(subscriptIndex, this.subscriptDim));

      result.setAttribute(Attributes.DIM, computeResultDimensionAttribute());
      if(names != null) {
        result.setAttribute(Attributes.NAMES, names.build());
      }
    }

    return result.build();
  }

  private void computeSubscriptDim() {
    this.subscriptDim = new int[sourceDim.length];
    subscriptLength = 1;
    for(int d=0;d!=sourceDim.length;++d) {
      int count = subscripts[d].getCount();
      subscriptDim[d] = count;
      subscriptLength *= count;
    }
  }

  private int subscriptIndexToSource(int subscriptIndex[]) {
    int sourceIndices[] = new int[sourceDim.length];
    for(int i=0;i!=sourceDim.length;++i) {
      sourceIndices[i] = subscripts[i].getAt(subscriptIndex[i]);
    }

    return Indexes.arrayIndexToVectorIndex(sourceIndices, sourceDim);
  }

  private SEXP computeResultDimensionAttribute() {
    int dim[];
    if(drop) {
      dim = dropUnitDimensions();
    } else {
      dim = subscriptDim;
    }
    if(dim.length == 0) {
      return Null.INSTANCE;
    } else if(dim.length == 1 && !sourceIsArray) {
      return Null.INSTANCE;
    } else {
      return new IntVector(dim);
    }
  }

  private int[] dropUnitDimensions() {
    int dim[] = new int[subscriptDim.length];
    int count = 0;
    for(int i=0;i!= subscriptDim.length;++i) {
      if(subscriptDim[i] > 1) {
        dim[count++] = subscriptDim[i];
      }
    }
    return java.util.Arrays.copyOf(dim, count);
  }



  public SEXP replace(SEXP elements) {

    Vector.Type replacementType;
    if(elements instanceof AtomicVector) {
      replacementType = ((AtomicVector) elements).getVectorType();
    } else {
      replacementType = ListVector.VECTOR_TYPE;
    }

    Vector.Builder result = copyWideningIfNecessary(source, replacementType);
    computeSourceDimensions();
    buildSubscripts();
    computeSubscriptDim();

    if(subscriptLength > 0) {
      if(elements.length() == 0) {
        throw new EvalException("replacement has zero length");
      }
      int replacement = 0;
      int[] subscriptIndex = new int[sourceDim.length];
      do {
        int index = subscriptIndexToSource(subscriptIndex);
        assert !sourceIsArray ||  index < source.length();
        if(!IntVector.isNA(index)) {
          result.setFrom(index, elements, replacement++);
          if(replacement >= elements.length()) {
            replacement = 0;
          }
        }
      } while(Indexes.incrementArrayIndex(subscriptIndex, this.subscriptDim));
    }

    return result.build();
  }



  private Vector.Builder copyWideningIfNecessary(Vector toCopy, Vector.Type replacementType) {
    Vector.Builder result;

    if(toCopy.getVectorType().isWiderThan(replacementType)) {
      result = toCopy.newCopyBuilder();
    } else {
      result = replacementType.newBuilder();
      result.copyAttributesFrom(toCopy);
      for(int i=0;i!= toCopy.length();++i) {
        result.setFrom(i, toCopy, i);
      }
    }
    return result;
  }



  private void buildSubscripts() {
    subscripts = new Subscript[subscriptArguments.size()];

    for(int i=0; i!=subscripts.length;++i) {
      SEXP argument = subscriptArguments.get(i);

      if(argument == Symbol.MISSING_ARG) {
        subscripts[i] = new MissingSubscript(sourceDim[i]);

      } else if(argument instanceof LogicalVector) {
        subscripts[i] = new LogicalSubscript(sourceDim[i], (LogicalVector)argument);

      } else if(argument instanceof StringVector) {
        subscripts[i] = new NamedSubscript(names(i), (StringVector)argument);

      } else if(argument instanceof DoubleVector || argument instanceof IntVector) {
        AtomicVector vector = (AtomicVector)argument;
        if(arePositions(vector)) {
          subscripts[i] = new PositionalSubscript(source, (AtomicVector)argument);
        } else {
          subscripts[i] = new NegativeSubscript(source, (AtomicVector)argument);
        }
      } else {
        throw new EvalException("invalid subscript type '%s'", argument.getTypeName());
      }
    }
  }

  private AtomicVector names(int i) {
    if(subscripts.length == 1 && !sourceIsArray) {
      return source.getNames();
    } else {
      Vector dimNames = (Vector) source.getAttribute(Symbol.DIMNAMES);
      return dimNames.getElementAsSEXP(i);
    }
  }

  private static boolean arePositions(AtomicVector indices) {
    boolean hasNeg = false;
    boolean hasPos = false;

    for(int i=0;i!=indices.length();++i) {
      int index = indices.getElementAsInt(i);
      if(index > 0 || IntVector.isNA(index)) {
        hasPos = true;
      } else if(index < 0) {
        hasNeg = true;
      }
    }
    if(hasNeg && hasPos) {
      throw new EvalException("only 0's may be mixed with negative subscripts");
    }
    return !hasNeg;
  }

  public SEXP replaceSingle(SEXP exp) {
              Vector.Type replacementType;
    if(exp instanceof AtomicVector) {
      replacementType = ((AtomicVector) exp).getVectorType();
    } else {
      replacementType = ListVector.VECTOR_TYPE;
    }

    Vector.Builder result = copyWideningIfNecessary(source, replacementType);
    computeSourceDimensions();
    buildSubscripts();
    computeSubscriptDim();

    if(subscriptLength != 1) {
      throw new EvalException("must replace exactly one element");
    }
    int index = subscriptIndexToSource(new int[sourceDim.length]);
    result.set(index, exp);

    return result.build();
  }


}
