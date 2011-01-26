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

package r.lang.primitive;

import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Evaluate;
import r.lang.primitive.annotations.Indices;
import r.lang.primitive.annotations.Primitive;

import java.util.Arrays;
import java.util.List;


public class Subset {

  @Primitive("$")
  public static SEXP getElementByName(PairList list, @Evaluate(false) SymbolExp symbol) {
    SEXP match = null;
    int matchCount = 0;

    for(PairList.Node node : list.nodes()) {
      if(node.hasTag()) {
        if(node.getTag().getPrintName().startsWith(symbol.getPrintName())) {
          match = node.getValue();
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Primitive("$")
  public static SEXP getElementByName(Environment env, @Evaluate(false) SymbolExp symbol) {
    SEXP value = env.getVariable(symbol);
    if(value == SymbolExp.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }

  @Primitive("$")
  public static SEXP getElementByName(ListVector list, @Evaluate(false) SymbolExp name) {
    SEXP match = null;
    int matchCount = 0;

    for(int i=0;i!=list.length(); ++i) {
      if(list.getName(i).startsWith(name.getPrintName())) {
        match = list.get(i);
        matchCount++;
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }

  @Primitive("$<-")
  public static SEXP setElementByName(ListVector list, @Evaluate(false) SymbolExp name, SEXP value) {
    ListVector.Builder result = ListVector.buildFromClone(list);

    int index = list.getIndexByName(name.getPrintName());
    if(index == -1) {
      result.add(name, value);
    } else {
      result.set(index, value);
    }
    return result.build();
  }

  @Primitive("[")
  public static SEXP getSubset(Context context, Environment rho, FunctionCall call) {

    Arguments arguments = new Arguments(context, rho, call);
    return new Subscription(arguments).getResult();
  }
//
//  @Primitive("[")
//  public static SEXP getSubset(Vector vector, LogicalVector include) {
//    Vector.Builder result = vector.newBuilder(0);
//    int resultLen = 0;
//    for(int i=0;i!=vector.length();++i) {
//      int b = include.getElementAsInt(i % include.length());
//      if( b == LogicalVector.NA ) {
//        result.setNA(i);
//      } else if( b != 0) {
//        result.setFrom(resultLen++, vector, i);
//      }
//    }
//    return result.build();
//  }
//
//  @Primitive("[")
//  public static SEXP getSubset(Vector vector, @Indices int indices[]) {
//    if(vector == Null.INSTANCE) {
//      return Null.INSTANCE;
//    }
//
//    Vector.Builder builder = vector.newBuilder(0);
//    int resultLen = 0;
//
//    if(arePositions(indices)) {
//
//      for(int index : indices) {
//        if(index > vector.length()) {
//          builder.setNA(resultLen++);
//        } else if(index > 0) {
//          builder.setFrom(resultLen++, vector, index-1);
//        }
//      }
//      return builder.build();
//
//    } else {
//
//      /* Negative indices indicate elements that should not be
//         returned.
//
//         For example, -1, means don't include the first element.
//       */
//
//      boolean excluded[] = toMask(indices, vector.length());
//      for(int i=0;i!=vector.length();++i) {
//        if(!excluded[i]) {
//          builder.setFrom(resultLen++, vector, i);
//        }
//      }
//    }
//
//    return builder.build();
//  }

  @Primitive("[[")
  public static SEXP getSingleElement(Vector vector, @Indices int index) {
    EvalException.check(index >= 0, "attempt to select more than one element");
    EvalException.check(index != 0, "attempt to select less than one element");
    EvalException.check(index <= vector.length(), "subscript out of bounds" );

    return vector.getElementAsSEXP(index-1);
  }

  @Primitive("[[")
  public static SEXP getSingleElementByExactName(Vector vector, String subscript) {
    int index = vector.getIndexByName(subscript);
    return index == -1 ? Null.INSTANCE : vector.getElementAsSEXP(index);
  }

  @Primitive("[[")
  public static SEXP getSingleElementByName(Vector vector, String subscript, boolean exact) {
    if(exact) {
      return getSingleElementByExactName(vector, subscript);
    } else {
      int matchCount = 0;
      SEXP match = Null.INSTANCE;

      for(int i=0;i!=vector.length();++i) {
        if(vector.getName(i).startsWith(subscript)) {
          match = vector.getElementAsSEXP(i);
          matchCount ++;
        }
      }

      return matchCount == 1 ? match : Null.INSTANCE;
    }
  }


  @Primitive("[<-")
  public static SEXP setSubset(Vector target, @Indices int indices[], Vector values) {
    EvalException.check(indices.length % values.length() == 0,
        "number of items to replace is not a multiple of replacement length");

    Vector.Builder result = copyWideningIfNecessary(target, values);

    for(int i=0;i!=indices.length;++i) {
      int index = indices[i];
      if(index > 0) {
        result.setFrom(index-1, values, i % values.length());
      }
    }
    return result.build();
  }

  @Primitive("[<-")
  public static SEXP setSubset(Vector target, LogicalVector subscripts, Vector values) {
    int replaceCount = 0;
    for(int i=0;i!=target.length();++i) {
      int subscriptIndex = i % subscripts.length();
      EvalException.check(!subscripts.isElementNA(subscriptIndex),
          "NAs are not allowed in subscripted assignments");

      if(subscripts.getElementAsInt(subscriptIndex) != 0) {
        replaceCount++;
      }
    }

    if(replaceCount == 0 && values.length() == 0) {
      return target;
    }

    EvalException.check(values.length() != 0, "replacement has zero length");
    EvalException.check(replaceCount % values.length() == 0,
        "number of items to replace is not a multiple of replacement length");

    Vector.Builder result = copyWideningIfNecessary(target, values);
    int replacedCount = 0;
    for(int i=0;i!=target.length();++i) {
      int subscriptIndex = i % subscripts.length();
      if(subscripts.getElementAsInt(subscriptIndex) != 0) {
        int valueIndex = replacedCount % values.length();
        result.setFrom(i, values, valueIndex);
        replacedCount++;
      }
    }

    return result.build();
  }

  private static Vector.Builder copyWideningIfNecessary(Vector toCopy, Vector otherElements) {
    Vector.Builder result;

    if(toCopy.isWiderThan(otherElements)) {
      result = toCopy.newCopyBuilder();
    } else {
      result = otherElements.newBuilder(0);
      for(int i=0;i!= toCopy.length();++i) {
        result.setFrom(i, toCopy, i);
      }
    }
    return result;
  }
//
//  @Primitive("[")
//  public static SEXP getSubset(Vector vector, StringVector names) {
//    Vector.Builder builder = vector.newBuilder(names.length());
//
//    int resultLen = 0;
//    for(String name : names) {
//      int index = vector.getIndexByName(name);
//      if(index == -1) {
//        builder.setNA(resultLen++);
//      } else {
//        builder.setFrom(resultLen++, vector, index);
//      }
//    }
//    return builder.build();
//  }

  /**
   * @return  true if the indices are all zero or positive
   */
  private static boolean arePositions(int indices[]) {
    boolean hasNeg = false;
    boolean hasPos = false;

    for(int i=0;i!=indices.length;++i) {
      if(indices[i] > 0 || IntVector.isNA(indices[i])) {
        hasPos = true;
      } else if(indices[i] < 0) {
        hasNeg = true;
      }
    }
    if(hasNeg && hasPos) {
      throw new EvalException("only 0's may be mixed with negative subscripts");
    }
    return !hasNeg;
  }

  private static boolean[] toMask(int indices[], int vectorLength) {
    boolean mask[] = new boolean[vectorLength];
    for(int i=0;i!=indices.length;++i) {
      if(indices[i] != 0) {
        int index = (-indices[i]) - 1;
        if( index < vectorLength ) {
          mask[ index ] = true;
        }
      }
    }
    return mask;
  }

  private static abstract class Subscript {

    public int getCount() {
      throw new UnsupportedOperationException();
    }

    public int getAt(int i) {
      throw new UnsupportedOperationException();
    }
  }


  private static class MissingSubscript extends Subscript {

    private final int sourceDimensionLength;

    public MissingSubscript(int sourceDimensionLength) {
      this.sourceDimensionLength = sourceDimensionLength;
    }

    @Override
    public int getCount() {
      return sourceDimensionLength;
    }

    @Override
    public int getAt(int i) {
      return i;
    }
  }

  private static class LogicalSubscripts extends Subscript {
    private int count;
    private int[] indices;

    public LogicalSubscripts(Vector source, LogicalVector subscript) {
      indices = new int[source.length()];
      count = 0;
      for(int i=0;i!=indices.length;++i) {
        int subscriptIndex = i % subscript.length();
        int value = subscript.getElementAsRawLogical(subscriptIndex);
        if(value == 1) {
          indices[count++] = i;
        } else if(IntVector.isNA(value)) {
          indices[count++] = IntVector.NA;
        }
      }
    }

    @Override
    public int getCount() {
      return count;
    }

    @Override
    public int getAt(int i) {
      return indices[i];
    }
  }

  private static class NamedSubscript extends Subscript {
    private int count;
    private int[] indices;

    public NamedSubscript(Vector source, StringVector names) {
      indices = new int[names.length()];
      count = names.length();

      for(int i=0;i!=names.length();++i) {
        String name = names.getElementAsString(i);
        int index = source.getIndexByName(name);
        indices[i] = (index == -1) ? IntVector.NA : index;
      }
    }

    @Override
    public int getCount() {
      return count;
    }

    @Override
    public int getAt(int i) {
      return indices[i];
    }
  }

  private static class PositionalSubscript extends Subscript {
    private final int indices[];
    private int count;

    public PositionalSubscript(Vector source, AtomicVector vector) {
      indices = new int[vector.length()];
      for(int i=0;i!=indices.length;++i) {
        int index = vector.getElementAsInt(i);
        if(index != 0) {
          if(IntVector.isNA(index) || index > source.length()) {
            indices[count++] = IntVector.NA;
          } else {
            indices[count++] = index-1;
          }
        }
      }
    }


    @Override
    public int getCount() {
      return count;
    }

    @Override
    public int getAt(int i) {
      return indices[i];
    }
  }

  private static class NegativeSubscript extends Subscript {
    private int[] indices;
    private int count;

    public NegativeSubscript(Vector source, AtomicVector subscript) {
      int mask[] = new int[source.length()];
      for(int i=0;i!=subscript.length();++i) {
        int index = -subscript.getElementAsInt(i);
        if(index != 0 && index <= mask.length) {
          mask[index-1] = 1;
        }
      }

      count = 0;
      indices = new int[mask.length];
      for(int i=0;i!=mask.length;++i) {
        if(mask[i] == 0) {
          indices[count++] = i;
        }
      }
    }

    @Override
    public int getCount() {
      return count;
    }

    @Override
    public int getAt(int i) {
      return indices[i];
    }
  }

  private static class Arguments {

    private Vector source;
    private boolean drop = true;
    List<SEXP> subscripts;

    public Arguments(Context context, Environment rho, FunctionCall call) {
      source = EvalException.checkedCast( call.evalArgument(context, rho, 0) );
      subscripts = Lists.newArrayList();

      for(PairList.Node node : ((PairList.Node)call.getArguments()).getNextNode().nodes()) {
        if(node.getName().equals("drop")) {
          drop = evaluateToBoolean(context, rho, node);

        } else {
          if(node.getValue() == SymbolExp.MISSING_ARG) {
            subscripts.add(node.getValue());
          } else {
            subscripts.add(node.getValue().evalToExp(context, rho));
          }
        }
      }
    }

    private boolean evaluateToBoolean(Context context, Environment rho, PairList.Node node) {
      return node.getValue().evalToExp(context, rho).asReal() != 0;
    }

    public Vector getSource() {
      return source;
    }

    public boolean isDrop() {
      return drop;
    }

    public int getSubscriptCount() {
      return subscripts.size();
    }

    public SEXP getSubscript(int index) {
      return subscripts.get(index);
    }
  }


  private static class Subscription {


    private Arguments arguments;

    private Vector source;
    private int sourceDim[];
    private boolean sourceIsArray = false;

    private Subscript subscripts[];
    private int subscriptCount;


    private int dimensionCount;
    private int[] resultDim;
    private int resultLength;

    private SEXP result = Null.INSTANCE;


    public Subscription(Arguments arguments) {
      this.arguments = arguments;
      this.source = arguments.getSource();

      if(arguments.getSource().length() > 0) {
        computeSourceDimensions();
        buildSubscripts();
        computeResult();
      }
    }

    private void computeSourceDimensions() {

      SEXP sourceDimExp = source.getAttribute(SymbolExp.DIM);
      if(sourceDimExp == Null.INSTANCE) {
        sourceDim = new int[] { source.length() };
      } else if(sourceDimExp instanceof IntVector) {
        sourceDim = ((IntVector) sourceDimExp).toIntArray();
        if(sourceDim.length == 1) {
          sourceIsArray = true;
        }
      } else {
        throw new AssertionError("DIM attribute must be NULL or an IntVector");
      }

      if( arguments.getSubscriptCount() != dimensionCount) {
        throw new EvalException("incorrect number of dimensions");
      }
    }

    private void buildSubscripts() {
      subscripts = new Subscript[arguments.getSubscriptCount()];

      for(int i=0; i!=subscripts.length;++i) {
        SEXP argument = arguments.getSubscript(i);

        if(argument == SymbolExp.MISSING_ARG) {
          subscripts[i] = new MissingSubscript(sourceDim[i]);

        } else if(argument instanceof LogicalVector) {
          subscripts[i] = new LogicalSubscripts(arguments.getSource(), (LogicalVector)argument);

        } else if(argument instanceof StringVector) {
          subscripts[i] = new NamedSubscript(arguments.getSource(), (StringVector)argument);

        } else if(argument instanceof DoubleVector || argument instanceof IntVector) {
          AtomicVector vector = (AtomicVector)argument;
          if(arePositions(vector)) {
            subscripts[i] = new PositionalSubscript(arguments.getSource(), (AtomicVector)argument);
          } else {
            subscripts[i] = new NegativeSubscript(arguments.getSource(), (AtomicVector)argument);
          }
        } else {
          throw new EvalException("invalid subscript type '%s'", argument.getTypeName());
        }
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

    public void computeResult() {

      StringVector.Builder names = null;
      if(source.getAttribute(SymbolExp.NAMES) != Null.INSTANCE) {
        names = new StringVector.Builder();
      }

      this.resultDim = new int[dimensionCount];
      resultLength = 1;
      for(int d=0;d!=dimensionCount;++d) {
        int count = subscripts[d].getCount();
        resultDim[d] = count;
        resultLength *= count;
      }

      Vector.Builder result = source.newBuilder(resultLength);


      if(resultLength > 0) {
        int count = 0;
        int[] subscriptIndex = new int[dimensionCount];
        do {
          int index = computeSourceIndex(subscriptIndex);
          if(!IntVector.isNA(index)) {
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

        } while(IndexUtils.incrementArrayIndex(subscriptIndex, this.resultDim));

        result.setAttribute(Attributes.DIM, computeResultDimensionAttribute());
        if(names != null) {
          result.setAttribute(Attributes.NAMES, names.build());
        }
      }

      this.result =  result.build();
    }

    private int computeSourceIndex(int subscriptIndex[]) {
      int sourceIndices[] = new int[dimensionCount];
      for(int i=0;i!=dimensionCount;++i) {
        sourceIndices[i] = subscripts[i].getAt(subscriptIndex[i]);
      }

      return IndexUtils.arrayIndexToVectorIndex(sourceIndices, sourceDim);
    }

    private SEXP computeResultDimensionAttribute() {
      int dim[];
      if(arguments.isDrop()) {
        dim = dropUnitDimensions();
      } else {
        dim = resultDim;
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
      int dim[] = new int[resultDim.length];
      int count = 0;
      for(int i=0;i!=resultDim.length;++i) {
        if(resultDim[i] > 1) {
          dim[count++] = resultDim[i];
        }
      }
      return Arrays.copyOf(dim, count);
    }

    public SEXP getResult() {
      return result;
    }
  }
}
