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

package r.base;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math.linear.RealVector;
import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;


/**
 * Creates a sequential vector from an expression like "1:99"
 */
public class Sequences {

  @Primitive(":")
  public static SEXP colon(SEXP n1, SEXP n2) {
    if( n1.inherits("factor") && n2.inherits("factor")) {
      return crossColon(n1, n2);

    } else {
      return colonSequence(n1, n2);
    }
  }

  private static SEXP crossColon(SEXP n1, SEXP n2) {
    throw new UnsupportedOperationException("crossColon not yet implemented");
  }

  public static SEXP colonSequence(SEXP s1, SEXP s2 ) {
    checkArg(s1);
    checkArg(s2);

    double n1 = s1.asReal();
    double n2 = s2.asReal();

    checkValue(n1);
    checkValue(n2);

    return new Range(n1, n2).vector();
  }


  private static void checkValue(double r1) {
    if(DoubleVector.isNaN(r1)) {
      throw new EvalException("NA/NaN argument");
    }
  }

  private static void checkArg(SEXP exp) {
    if(exp.length() == 0) {
      throw new EvalException("argument of length 0");
    } else if(exp.length() > 1) {
      Warning.warning("numerical expression has %d elements: only the first used", exp.length());
    }
  }

  @Primitive("rep.int")
  public static Vector repeatInt(Vector x, int times) {
    EvalException.check(times >= 0, "invalid 'times' value");

    Vector.Builder result = x.newBuilder(x.length() * times);
    int count = 0;
    while(times > 0) {
      for(int i =0; i!=x.length();++i) {
        result.setFrom(count++, x, i);
      }
      times--;
    }
    return result.build();
  }

  @Primitive("rep")
  public static Vector repeat(@ArgumentList ListVector arguments) {
    if(arguments.length() < 1) {
      return Null.INSTANCE;
    }
    Vector x = EvalException.checkedCast(arguments.getElementAsSEXP(0));
    Vector.Builder result = x.newBuilder(0);

    int times = findRepArgument(arguments, 1, "times");
    int lengthOut = findRepArgument(arguments, 1, "length.out");
    int each = findRepArgument(arguments, 1, "each");

    int resultLength = x.length();

    if(!IntVector.isNA(times)) {
      resultLength = x.length() * times;
    }
    if(!IntVector.isNA(each)) {
      resultLength = x.length() * each;
    } else {
      each = 1;
    }
    if(!IntVector.isNA(lengthOut)) {
      resultLength = lengthOut;
    }

    StringVector.Builder names = StringVector.newBuilder();
    int result_i = 0;
    for(int i=0;i!=resultLength;++i) {
      int x_i = (i / each) % x.length();
      result.setFrom(result_i++, x, x_i);
      names.add(x.getName(x_i));
    }
    if(names.haveNonEmpty()) {
      result.setAttribute(Symbol.NAMES, names.build());
    }

    return result.build();
  }

  private static int findRepArgument(ListVector arguments, int position, String name) {
    for(int i=1;i!=arguments.length();++i) {
      if(name.startsWith(arguments.getName(i))) {
        return arguments.getElementAsInt(i);
      }
    }
    if(position < arguments.length() && arguments.getName(position).isEmpty()) {
      return arguments.getElementAsInt(position);
    }
    return IntVector.NA;
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
      count = range + 1 + DoubleVector.EPSILON;

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

    public SEXP vector() {
      return useInteger ? intVector() : realVector();
    }

    private SEXP intVector() {
      int values[] = new int[(int) count];
      int index = 0;

      if(n1 <= n2) {
        for(int n=(int)n1; n<=n2; n++) {
          values[index++] = n;
        }
      } else {
        for(int n=(int)n1; n>=n2; n--) {
          values[index++] = n;
        }
      }
      return new IntVector(values);
    }

    private SEXP realVector() {
      double[] values = new double[(int) count];
      int index = 0;

      if(n1 <= n2) {
        for(double n=n1; n<=n2; n+=1d) {
          values[index++] = n;
        }
      } else {
        for(double n=n1; n>=n2; n-=1d) {
          values[index++] = n;
        }
      }
      return new DoubleVector(values);
    }
  }

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
    SEXP from = matched.findByTag(new Symbol("from"));
    SEXP to = matched.findByTag(new Symbol("to"));
    SEXP by = matched.findByTag(new Symbol("by"));
    SEXP len = matched.findByTag(new Symbol("length.out"));
    SEXP along = matched.findByTag(new Symbol("along.with"));

    if(from!=Symbol.MISSING_ARG) {
      from = from.evalToExp(context, rho);
    }
    if(to!=Symbol.MISSING_ARG) {
      to = to.evalToExp(context, rho);
    }
    if(by!=Symbol.MISSING_ARG) {
      by = by.evalToExp(context, rho);
    }
    if(len!=Symbol.MISSING_ARG) {
      len = len.evalToExp(context, rho);
    }

    return doSeq(from, to, by, len, along, One);
  }

  private static SEXP doSeq(SEXP from, SEXP to, SEXP by, SEXP len, SEXP along, boolean one) {
    if(one && from != Symbol.MISSING_ARG) {
      int lf = from.length();
      if(lf == 1 && ( from instanceof IntVector ||  from instanceof RealVector)) {
        return new Range(1.0, ((AtomicVector) from).getElementAsDouble(0)).vector();
      } else if (lf != 0) {
        return new Range(1.0, lf).vector();
      } else {
        return new IntVector();
      }
    }

    int lout = IntVector.NA;
    if(along != Symbol.MISSING_ARG) {
      //int lout = INTEGER(along)[0];
      if(one) {
        // ans = lout ? seq_colon(1.0, (double)lout, call) : allocVector(INTSXP, 0);
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
      return new IntVector();

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
    if(!DoubleVector.isFinite(rto)) {
      throw new EvalException("'to' must be finite");
    }
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
    return new DoubleVector(ra);
  }

  private static SEXP sequenceFromTo(double from, double to, int length) {
    if(!DoubleVector.isFinite(from)) {
      throw new EvalException("'from' must be finite");
    }
    if(!DoubleVector.isFinite(to)) {
      throw new EvalException("'to' must be finite");
    }
    double sequence[] = new double[length];
    if(length > 0) sequence[0] = from;
    if(length > 1) sequence[length - 1] = to;
    if(length > 2) {
      double by = (to - from)/(double)(length - 1);
      for(int i = 1; i < length-1; i++) {
        sequence[i] = from + i*by;
      }
    }
    return new DoubleVector(sequence);
  }

  private static SEXP sequenceTo(int length, double to, double by) {
    if(!DoubleVector.isFinite(to)) {
      throw new EvalException("'to' must be finite");
    }
    if(!DoubleVector.isFinite(by)) {
      throw new EvalException("'by' must be finite") ;
    }

    double from = to - (length-1)*by;

    if(isIntegerRange(from, to, by)) {
      int sequence[] = new int[length];
      for(int i = 0; i < length; i++) {
        sequence[i] = (int) (to - (length - 1 - i)*by);
      }
      return new IntVector(sequence);

    } else {
      double sequence[] = new double[length];
      for(int i = 0; i < length; i++){
        sequence[i] = to - (length - 1 - i)*by;
      }
      return new DoubleVector(sequence);
    }
  }

  private static SEXP sequenceFrom(int length, double from, double by) {
    if(!DoubleVector.isFinite(from)) {
      throw new EvalException("'from' must be finite");
    }
    if(!DoubleVector.isFinite(by)) {
      throw new EvalException("'by' must be finite");
    }

    double to = from +(length-1)*by;

    if(isIntegerRange(from, to, by)) {
      int sequence[] = new int[length];
      for(int i = 0; i < length; i++) {
        sequence[i] = (int) (from + i*by);
      }
      return new IntVector(sequence);

    } else {
      double sequence[] = new double[length];
      for(int i = 0; i < length; i++) {
        sequence[i] = from + i*by;
      }
      return new DoubleVector(sequence);
    }
  }

  private static boolean isIntegerRange(double from, double to, double by) {
    return by == (int)by && from <= Integer.MAX_VALUE && from >= Integer.MIN_VALUE
        && to <= Integer.MAX_VALUE && to >= Integer.MIN_VALUE;
  }

  @Primitive("seq_along")
  public static int[] seqAlong(SEXP exp) {
    int indexes[] = new int[exp.length()];
    for(int i=0;i!=indexes.length;++i) {
      indexes[i] = i+1;
    }
    return indexes;
  }

  @Primitive("seq_len")
  public static IntVector seqLength(int length) {
    IntVector.Builder result = new IntVector.Builder();
    for(int i=1;i<=length;++i) {
      result.add(i);
    }
    return result.build();
  }
}
