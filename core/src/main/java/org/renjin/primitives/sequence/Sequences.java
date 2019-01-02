/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.sexp.*;


/**
 * Creates a sequential vector from an expression like "1:99"
 */
public class Sequences {

  @Builtin(":")
  public static AtomicVector colon(@Current Context context, SEXP n1, SEXP n2) {
    if( n1.inherits("factor") && n2.inherits("factor")) {
      return crossColon(n1, n2);

    } else {
      return colonSequence(context, n1, n2);
    }
  }

  private static AtomicVector crossColon(SEXP n1, SEXP n2) {
    throw new UnsupportedOperationException("crossColon not yet implemented");
  }

  public static AtomicVector colonSequence(Context context, SEXP s1, SEXP s2 ) {
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
      context.warn(String.format("numerical expression has %d elements: only the first used", exp.length()));
    }
  }

  @Internal("rep.int")
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
          if(upperBound <= Integer.MIN_VALUE || upperBound > Integer.MAX_VALUE) {
            useInteger = false;
          }
        }
      }
    }

    public AtomicVector vector() {
      if(useInteger) {
        return IntSequence.fromTo(n1, n2);
      } else {
        return DoubleSequence.fromTo(n1, n2);
      }
    }
  }

  @Builtin
  public static IntVector seq_along(SEXP exp) {
    return new IntSequence(1, 1, exp.length());
  }

  @Builtin
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
