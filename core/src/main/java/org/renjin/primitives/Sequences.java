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

package org.renjin.primitives;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math.linear.RealVector;
import org.renjin.eval.Calls;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.PassThrough;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.processor.ArgumentIterator;
import org.renjin.primitives.annotations.processor.WrapperRuntime;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;


/**
 * Creates a sequential vector from an expression like "1:99"
 */
public class Sequences {

  @Primitive(":")
  public static Vector colon(@Current Context context, SEXP n1, SEXP n2) {
    if( n1.inherits("factor") && n2.inherits("factor")) {
      return crossColon(n1, n2);

    } else {
      return colonSequence(context, n1, n2);
    }
  }

  private static Vector crossColon(SEXP n1, SEXP n2) {
    throw new UnsupportedOperationException("crossColon not yet implemented");
  }

  public static Vector colonSequence(Context context, SEXP s1, SEXP s2 ) {
    assertScalar(context, s1);
    assertScalar(context, s2);

    double n1 = s1.asReal();
    double n2 = s2.asReal();

    assertNotNa(n1);
    assertNotNa(n2);

    return new Range(n1, n2).vector();
  }


  private static void assertNotNa(double r1) {
    if(DoubleVector.isNaN(r1)) {
      throw new EvalException("NA/NaN argument");
    }
  }

  private static void assertScalar(Context context, SEXP exp) {
    if(exp.length() == 0) {
      throw new EvalException("argument of length 0");
    } else if(exp.length() > 1) {
      Warning.invokeWarning(context, "numerical expression has %d elements: only the first used", exp.length());
    }
  }

  private static void assertFinite(String name, double to) {
    if(!DoubleVector.isFinite(to)) {
      throw new EvalException("'" + name + "' must be finite");
    }
  }

  @Primitive("rep.int")
  public static Vector repeatInt(Vector x, Vector timesVector) {

    if(timesVector.length() == 1) {
      int times = timesVector.getElementAsInt(0);
      Vector.Builder result = x.newBuilderWithInitialSize(x.length() * times);
      int count = 0;
      while(times > 0) {
        for(int i =0; i!=x.length();++i) {
          result.setFrom(count++, x, i);
        }
        times--;
      }
      return result.build();
    } else {
      if(timesVector.length() != x.length()) {
        throw new EvalException("Invalid 'times' value: times must be the same length as x");
      }
      Vector.Builder result = x.newBuilderWithInitialCapacity(x.length());
      for(int i=0;i!=x.length();++i) {
        int times = timesVector.getElementAsInt(i);
        while(times > 0) {
          result.addFrom(x, i);
          times--;
        }
      }
      return result.build();
    }
  }

  @PassThrough
  @Primitive("rep")
  public static SEXP repeat(Context context, Environment rho, FunctionCall call) {

    // rep is one of the very few primitives that uses argument matching
    // *ALMOST* like that employed for closures.
    //
    // the one gotcha is that generic dispatch is done on the FIRST argument,
    // even if 'x' is provided as named argument elsewhere
    
    // check for zero args -- the result should be null
    PairList arguments = call.getArguments();
    if(arguments == Null.INSTANCE) {
      context.setInvisibleFlag();
      return Null.INSTANCE;
    }
    
    // evaluate the first arg
    ArgumentIterator argIt = new ArgumentIterator(context, rho, arguments);
    PairList.Node firstArgNode = argIt.nextNode();
    SEXP firstArg = context.evaluate( firstArgNode.getValue(), rho);
    if(firstArg.isObject()) {
      SEXP result = WrapperRuntime.tryDispatchFromPrimitive(context, rho, call, "rep", firstArg, arguments);
      if(result != null) {
        return result;
      }
    }
    
    // create a new pair list of evaluated arguments
    PairList.Builder evaled = new PairList.Builder();
    evaled.add(firstArgNode.getRawTag(), firstArg);
    while(argIt.hasNext()) {
      PairList.Node node = argIt.nextNode();
      evaled.add(node.getRawTag(), context.evaluate( node.getValue(), rho));
    }
    
    // declare formals
    PairList.Builder formals = new PairList.Builder();
    formals.add("x", Symbol.MISSING_ARG);
    formals.add("times", Symbol.MISSING_ARG);
    formals.add("length.out", Symbol.MISSING_ARG);
    formals.add("each", Symbol.MISSING_ARG);
    
    PairList matched = Calls.matchArguments(formals.build(), evaled.build());
    
    SEXP x = matched.findByTag(Symbol.get("x"));
    SEXP times = matched.findByTag(Symbol.get("times"));
    SEXP lengthOut = matched.findByTag(Symbol.get("length.out"));
    SEXP each = matched.findByTag(Symbol.get("each"));
    
    return rep(
        (Vector)x,
        times == Symbol.MISSING_ARG ? new IntArrayVector(1) : (Vector)times,
        lengthOut == Symbol.MISSING_ARG ? IntVector.NA : ((Vector)lengthOut).getElementAsInt(0),
        each == Symbol.MISSING_ARG ? IntVector.NA : ((Vector)each).getElementAsInt(0));
  }
  
  private static Vector rep(Vector x, Vector times, int lengthOut, int each) {
    int resultLength;

    if(times.length() == 1) {
      resultLength = x.length() * times.getElementAsInt(0);
    } else {
      resultLength = 0;
      for(int i=0;i!=x.length();++i) {
        resultLength += times.getElementAsInt(i);
      }
    } 
    if(!IntVector.isNA(each)) {
      resultLength = x.length() * each;
    } else {
      each = 1;
    }
    if(!IntVector.isNA(lengthOut)) {
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
       x.length() > RepDoubleVector.LENGTH_THRESHOLD) {

      return new RepDoubleVector((DoubleVector) x, resultLength, each);
    }

    /**
     * Go ahead and allocate and fill the memory
     */
    Vector.Builder result = x.newBuilderWithInitialCapacity(resultLength);
    NamesBuilder names = NamesBuilder.withInitialCapacity(resultLength);
    int result_i = 0;

    if(times.length() == 1) {
      for(int i=0;i!=resultLength;++i) {
        int x_i = (i / each) % x.length();
        result.setFrom(result_i++, x, x_i);
        names.add(x.getName(x_i));
      }
    } else {
      for(int x_i=0;x_i!=x.length();++x_i) {
        for(int j=0;j<times.getElementAsInt(x_i);++j) {
          result.setFrom(result_i++, x, x_i);
          names.add(x.getName(x_i));
        }
      }
    }
    if(names.haveNames()) {
      result.setAttribute(Symbols.NAMES, names.build(resultLength));
    }

    return result.build();
  }

  @VisibleForTesting
  static class Range {
    boolean useInteger;
    private double range;
    double count;
    final double n1;
    final double n2;

    public Range(double n1, double n2) {
      this.n1 = n1;
      this.n2 = n2;
      range = Math.abs(n2 - n1);
      count = range + 1d + DoubleVector.EPSILON;

      determineType();
    }

    private void determineType() {
      int in1 = (int)(n1);
      useInteger = (n1 == in1);
      if(useInteger) {
        if(n1 <= Integer.MIN_VALUE || n1 > Integer.MAX_VALUE) {
          useInteger = false;
        } else {
          /* r := " the effective 'to' "  of  from:to */
          double upperBound = n1 + ((n1 <= n2) ? count -1 : -(count -1));
          if(upperBound <= Integer.MIN_VALUE || upperBound > Integer.MAX_VALUE)
            useInteger = false;
        }
      }
    }

    public Vector vector() {
      if(useInteger) {
        return IntSequence.fromTo(n1, n2);
      } else {
        return DoubleSequence.fromTo(n1, n2);
      }
    }
  }

  @PassThrough
  @Primitive("seq.int")
  public static SEXP seqInt(Context context, Environment rho, FunctionCall call) {

    // TODO: move this argument matching somewhere else,
    // and this is super inefficient...

    // match arguments
    PairList.Builder formals = new PairList.Builder();
    formals.add("from", Symbol.MISSING_ARG);
    formals.add("to", Symbol.MISSING_ARG);
    formals.add("by", Symbol.MISSING_ARG);
    formals.add("length.out", Symbol.MISSING_ARG);
    formals.add("along.with", Symbol.MISSING_ARG);

    boolean One = (call.getArguments().length() == 1);
    PairList matched = Calls.matchArguments(formals.build(), call.getArguments());
    SEXP from = matched.findByTag(Symbol.get("from"));
    SEXP to = matched.findByTag(Symbol.get("to"));
    SEXP by = matched.findByTag(Symbol.get("by"));
    SEXP len = matched.findByTag(Symbol.get("length.out"));
    SEXP along = matched.findByTag(Symbol.get("along.with"));

    if(from!=Symbol.MISSING_ARG) {
      from = context.evaluate( from, rho);
    }
    if(to!=Symbol.MISSING_ARG) {
      to = context.evaluate(to, rho);
    }
    if(by!=Symbol.MISSING_ARG) {
      by = context.evaluate( by, rho );
    }
    if(len!=Symbol.MISSING_ARG) {
      len = context.evaluate( len, rho);
    }

    return doSeqInt(from, to, by, len, along, One);
  }

  private static SEXP doSeqInt(SEXP from, SEXP to, SEXP by, SEXP len, SEXP along, boolean one) {
    if(one && from != Symbol.MISSING_ARG) {
      int lf = from.length();
      if(lf == 1 && ( from instanceof IntVector || from instanceof RealVector)) {
        return new Range(1.0, ((AtomicVector) from).getElementAsDouble(0)).vector();
      } else if (lf != 0) {
        return new Range(1.0, lf).vector();
      } else {
        return new IntArrayVector();
      }
    }

    int lout = IntVector.NA;
    if(along != Symbol.MISSING_ARG) {
      if(one) {
        throw new UnsupportedOperationException("implement me!");
      }
    } else if(len != Symbol.MISSING_ARG && len != Symbol.MISSING_ARG) {
      double rout = len.asReal();
      if(Double.isNaN(rout) || rout <= -0.5) {
        throw new EvalException("'length.out' must be a non-negative number");
      }
      lout = (int) Math.ceil(rout);
    }

    if(IntVector.isNA(lout)) {
      double rfrom = (from == Symbol.MISSING_ARG) ? 1.0 : from.asReal();
      double rto = (to == Symbol.MISSING_ARG) ? 1.0 : to.asReal();
      double rby = by.asReal();

      if(by == Symbol.MISSING_ARG) {
        return new Range(rfrom, rto).vector();
      } else {
        return sequenceBy(from, to, rfrom, rto, rby);
      }
    } else if (lout == 0) {
      return new IntArrayVector();

    } else if (one) {
      return new Range(1.0, lout).vector();

    } else if (by == Symbol.MISSING_ARG) {
      double rfrom = from.asReal();
      double rto = to.asReal();
      if(to == Symbol.MISSING_ARG) {
        rto = rfrom + lout - 1;
      }
      if(from == Symbol.MISSING_ARG) {
        rfrom = rto - lout + 1;
      }
      return sequenceFromTo(rfrom, rto, lout);

    } else if (to == Symbol.MISSING_ARG) {
      double rfrom = (from == Symbol.MISSING_ARG) ? 1.0 : from.asReal();
      return sequenceFrom(lout, rfrom, by.asReal());

    } else if (from == Symbol.MISSING_ARG) {
      return sequenceTo(lout, to.asReal(), by.asReal());

    } else {
      throw new EvalException("too many arguments");
    }
  }

  private static SEXP sequenceBy(SEXP from, SEXP to, double rfrom, double rto, double by) {
    double del = rto - rfrom, n, dd;
    int nn;
    if(!DoubleVector.isFinite(rfrom)) {
      throw new EvalException("'from' must be finite");
    }
    assertFinite("to", rto);
    if(del == 0.0 && rto == 0.0) {
      return to;
    }
    n = del/by;
    if(!DoubleVector.isFinite(n)) {
      if(del == 0.0 && by == 0.0) {
        return from;
      } else {
        throw new EvalException("invalid '(to - from)/by' in 'seq'");
      }
    }
    dd = Math.abs(del)/Math.max(Math.abs(rto), Math.abs(rfrom));
    if(dd < 100 * DoubleVector.EPSILON) {
      return from;
    }
    if(n > (double) Integer.MAX_VALUE) {
      throw new EvalException("'by' argument is much too small");
    }
    if(n < -DoubleVector.EPSILON) {
      throw new EvalException("wrong sign in 'by' argument");
    }
    nn = (int)(n + DoubleVector.EPSILON);
    double ra[] = new double[nn+1];
    for(int i = 0; i <= nn; i++) {
      ra[i] = rfrom + (i * by);
    }
    /* Added in 2.9.0 */
    if (nn > 0) {
      if((by > 0 && ra[nn] > rto) || (by < 0 && ra[nn] < rto)) {
        ra[nn] = rto;
      }
    }
    return new DoubleArrayVector(ra);
  }

  private static SEXP sequenceFromTo(double from, double to, int length) {
    assertFinite("from", from);
    assertFinite("to", to);

    double by = (to - from)/(double)(length - 1);

    return newSequence(from, by, to, length);
  }

  private static SEXP sequenceTo(int length, double to, double by) {
    assertFinite("to", to);
    assertFinite("by", by);

    double from = to - (length-1)*by;

    return newSequence(from, by, to, length);
  }

  private static SEXP sequenceFrom(int length, double from, double by) {
    assertFinite("from", from);
    assertFinite("by", by);

    double to = from +(length-1)*by;

    return newSequence(from, by, to, length);
  }


  @Primitive
  public static IntVector seq_along(SEXP exp) {
    return new IntSequence(1, 1, exp.length());
  }

  @Primitive
  public static IntVector seq_len(int length) {
    return new IntSequence(1, 1, length);
  }

  private static SEXP newSequence(double from, double by, double to, int length) {
    if(isIntegerRange(from, to, by)) {
      return new IntSequence((int)from, (int)by, length);
    } else {
      return new DoubleSequence(from, by, length);
    }
  }

  private static boolean isIntegerRange(double from, double to, double by) {
    return by == (int)by &&
           from <= Integer.MAX_VALUE &&
           from >= Integer.MIN_VALUE &&
           to <= Integer.MAX_VALUE &&
           to >= Integer.MIN_VALUE;
  }

}
