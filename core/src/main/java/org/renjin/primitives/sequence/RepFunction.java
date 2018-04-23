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
package org.renjin.primitives.sequence;

import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.MatchedArguments;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;

public class RepFunction extends SpecialFunction {

  private static final ArgumentMatcher MATCHER =
      new ArgumentMatcher("x", "times", "length.out", "each", "...");

  private static final IntArrayVector DEFAULT_TIMES = new IntArrayVector(1);

  public RepFunction() {
    super("rep");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList arguments) {

    // rep is one of the very few primitives that uses argument matching
    // *ALMOST* like that employed for closures.
    //
    // the one gotcha is that generic dispatch is done on the FIRST argument,
    // even if 'x' is provided as named argument elsewhere

    // check for zero args -- the result should be null
    if (arguments == Null.INSTANCE) {
      context.setInvisibleFlag();
      return Null.INSTANCE;
    }

    // evaluate the first arg
    ArgumentIterator argIt = new ArgumentIterator(context, rho, arguments);
    PairList.Node firstArgNode = argIt.nextNode();
    SEXP firstArg = context.evaluate(firstArgNode.getValue(), rho);
    if (firstArg.isObject()) {
      SEXP result = S3.tryDispatchFromPrimitive(context, rho, call, "rep", firstArg, arguments);
      if (result != null) {
        return result;
      }
    }

    MatchedArguments matched = MATCHER.match(arguments);
    SEXP x =  context.evaluate(matched.getActualForFormal(0), rho);

    Vector times;
    int timesIndex = matched.getActualIndex(1);
    if(timesIndex == -1) {
      times = DEFAULT_TIMES;
    } else {
      times = (Vector) context.evaluate(matched.getActualValue(timesIndex), rho);
    }
    int lengthOut;
    int lengthOutIndex = matched.getActualIndex(2);
    if (lengthOutIndex == -1) {
      lengthOut = IntVector.NA;
    } else {
      Vector lengthOutVector = (Vector) context.evaluate(matched.getActualValue(lengthOutIndex), rho);
      lengthOut = lengthOutVector.getElementAsInt(0);
    }
    int each;
    int eachIndex = matched.getActualIndex(3);
    if (eachIndex == -1) {
      each = IntVector.NA;
    } else {
      Vector eachVector = (Vector) context.evaluate(matched.getActualValue(eachIndex), rho);
      each = eachVector.getElementAsInt(0);
    }

    return rep((Vector) x, times, lengthOut, each);
  }


  public static Vector rep(Vector x, Vector times, int lengthOut, int each) {
    int resultLength;

    if(x == Null.INSTANCE) {
      return Null.INSTANCE;
    }
    
    if(x.length() == 0) {
      x = x.getVectorType().newBuilderWithInitialCapacity(1).addNA().build();
    }
    
    if(times.length() == 1) {
      resultLength = x.length() * times.getElementAsInt(0);
    } else {
      resultLength = 0;
      for(int i=0;i!=x.length();++i) {
        resultLength += times.getElementAsInt(i);
      }
    }
    if(!IntVector.isNA(each)) {
      resultLength = resultLength * each;
    } else {
      each = 1;
    }
    if(!IntVector.isNA(lengthOut)) {
      if(lengthOut < 0) {
        throw new EvalException("invalid 'length.out' argument");
      }
      resultLength = lengthOut;
    }

    if(times.length() > 1 && each > 1) {
      throw new EvalException("invalid 'times' argument");
    }

    /*
     * If there is no per-element times parameter,
     * and we have a large vector, then just return
     * a wrapper around this vector and avoid
     * allocating the extra memory.
     */
    if(x instanceof DoubleVector &&
        times.length() == 1 &&
        lengthOut != 0 &&
        (x.isDeferred() || resultLength > RepDoubleVector.LENGTH_THRESHOLD)) {

      return new RepDoubleVector(x, resultLength, each, repeatAttributes(x, resultLength, each));

    } else if(x instanceof IntVector &&
        times.length() == 1 &&
        lengthOut != 0 &&
        (x.isDeferred() || resultLength > RepIntVector.LENGTH_THRESHOLD)) {

      return new RepIntVector(x, resultLength, each, repeatAttributes(x, resultLength, each));
    }

    /*
     * Go ahead and allocate and fill the memory
     */
    Vector.Builder result = x.newBuilderWithInitialCapacity(resultLength);
    AtomicVector names = x.getNames();
    StringArrayVector.Builder resultNames = null;
    if(names != Null.INSTANCE) {
      resultNames = new StringArrayVector.Builder(0, resultLength);
    }
    int result_i = 0;

    if(times.length() == 1) {
      for(int i=0;i!=resultLength;++i) {
        int x_i = (i / each) % x.length();
        result.setFrom(result_i++, x, x_i);
        if(resultNames != null) {
          resultNames.add(names.getElementAsString(x_i));
        }
      }
    } else {
      for(int x_i=0;x_i!=x.length();++x_i) {
        for(int j=0;j<times.getElementAsInt(x_i);++j) {
          result.setFrom(result_i++, x, x_i);
          if(resultNames != null) {
            resultNames.add(names.getElementAsString(x_i));
          }
        }
      }
    }
    if(resultNames != null) {
      result.setAttribute(Symbols.NAMES, resultNames.build());
    }

    return result.build();
  }


  private static AttributeMap repeatAttributes(Vector source, int length, int each) {
    AtomicVector names = source.getNames();
    if(names != Null.INSTANCE) {
      AttributeMap.Builder repeated = AttributeMap.newBuilder();
      repeated.setNames(new RepStringVector(names, length, each, AttributeMap.EMPTY));
      return repeated.build();
      
    } else {
      return AttributeMap.EMPTY;
    }
  }
  
}
