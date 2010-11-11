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

package r.lang.primitive.binding;

import r.lang.*;
import r.parser.ParseUtil;

public class AtomicAccessors {

  private static final AtomicAccessor<Void> NULL_ACCESSOR = new NullAccessor();

  private AtomicAccessors() {
  }

  public static boolean haveAccessor(SEXP provided, Class elementClass) {
    return create(provided, elementClass) != null;
  }

  public static AtomicAccessor<?> create(SEXP exp) {
    if(exp instanceof LogicalExp) {
      return new LogicalExpToBoolean((LogicalExp) exp);
    } else if(exp instanceof IntExp) {
      return new IntExpAccessor((IntExp) exp);
    } else if(exp instanceof DoubleExp) {
      return new DoubleExpAccessor((DoubleExp) exp);
    } else if(exp instanceof StringExp) {
      return new StringExpAccessor((StringExp)exp);
    } else if(exp instanceof NullExp) {
      return NULL_ACCESSOR;
    }
    return null;
  }

  public static AtomicAccessor create(SEXP exp, Class elementType) {

    if(exp instanceof LogicalExp) {
      if(elementType == Logical.class) {
        return new LogicalExpToLogical((LogicalExp) exp);
      } else if(elementType == Boolean.TYPE) {
        return new LogicalExpToBoolean((LogicalExp) exp);
      } else if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new LogicalExpToInt((LogicalExp) exp);
      } else if(elementType == Double.TYPE || elementType == Double.class) {
        return new LogicalExpToDouble((LogicalExp) exp);
      } else if(elementType == String.class) {
        return new LogicalExpToString((LogicalExp) exp);
      } else {
        return null;
      }

    } else if(exp instanceof IntExp) {
      if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new IntExpAccessor((IntExp) exp);
      } else if(elementType == Double.TYPE || elementType == Double.class) {
        return new IntExpToDouble((IntExp) exp);
      } else if(elementType == String.class) {
        return new IntExpToString((IntExp) exp);
      } else {
        return null;
      }


    } else if(exp instanceof DoubleExp) {
      if(elementType == Double.TYPE || elementType == Double.class) {
        return new DoubleExpAccessor((DoubleExp) exp);
      } else if(elementType == String.class) {
        return new DoubleExpToString((DoubleExp) exp);
      }


    } else if(exp instanceof StringExp) {
      if(elementType == String.class) {
        return new StringExpAccessor((StringExp) exp);
      }

    } else if(exp instanceof NullExp) {
      return NULL_ACCESSOR;
    }

    return null;
  }


  public static Class elementClassOf(Class<? extends AtomicExp> atomicClass) {
    if(atomicClass == LogicalExp.class) {
      return Logical.class;
    } else if(atomicClass == IntExp.class) {
      return Integer.TYPE;
    } else if(atomicClass == DoubleExp.class) {
      return Double.TYPE;
    } else if(atomicClass == StringExp.class) {
      return String.class;
    } else {
      throw new IllegalArgumentException();
    }
  }

  private static class NullAccessor implements AtomicAccessor<Void> {

    @Override
    public int length() {
      return 0;
    }

    @Override
    public boolean isNA(int index) {
      throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public Void get(int index) {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }

  private static class LogicalExpToInt implements AtomicAccessor<Integer> {
    private LogicalExp exp;

    private LogicalExpToInt(LogicalExp vector) {
      this.exp = vector;
    }

    @Override
    public boolean isNA(int index) {
      return IntExp.isNA(exp.get(index));
    }

    @Override
    public Integer get(int index) {
      return exp.get(index);
    }

    @Override
    public int length() {
      return exp.length();
    }

  }

  private static class IntExpAccessor implements AtomicAccessor<Integer> {
    private IntExp exp;

    private IntExpAccessor(IntExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return exp.get(index) == IntExp.NA;
    }

    @Override
    public Integer get(int index) {
      return exp.get(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class DoubleExpAccessor implements AtomicAccessor<Double> {
    private DoubleExp exp;

    private DoubleExpAccessor(DoubleExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return DoubleExp.isNA(exp.get(index));
    }

    @Override
    public Double get(int index) {
      return exp.get(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class IntExpToDouble implements AtomicAccessor<Double> {
    private IntExp exp;

    private IntExpToDouble(IntExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return IntExp.isNA(exp.get(index));
    }

    @Override
    public Double get(int index) {
      return (double) exp.get(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }



  private static class LogicalExpToDouble implements AtomicAccessor<Double> {
    private LogicalExp exp;

    private LogicalExpToDouble(LogicalExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return exp.get(index) == IntExp.NA;
    }

    @Override
    public Double get(int index) {
      return (double)exp.get(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class LogicalExpToBoolean implements AtomicAccessor<Boolean> {
    private LogicalExp exp;

    private LogicalExpToBoolean(LogicalExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return exp.get(index) == IntExp.NA;
    }

    @Override
    public Boolean get(int index) {
      return exp.get(index) == 1;
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class StringExpAccessor implements AtomicAccessor<String> {
    private StringExp exp;

    private StringExpAccessor(StringExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return StringExp.isNA(exp.get(index));
    }

    @Override
    public String get(int index) {
      return exp.get(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class DoubleExpToString implements AtomicAccessor<String> {
    private DoubleExp exp;

    private DoubleExpToString(DoubleExp exp) {
      this.exp = exp;
    }

    public boolean isNA(int index) {
      return DoubleExp.isNA(exp.get(index));
    }

    public String get(int index) {
      return ParseUtil.toString(exp.get(index));
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class IntExpToString implements AtomicAccessor<String> {
    private IntExp exp;

    private IntExpToString(IntExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return IntExp.isNA(exp.get(index));
    }

    @Override
    public String get(int index) {
      return ParseUtil.toString(exp.get(index));
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class LogicalExpToString implements AtomicAccessor<String> {
    private LogicalExp exp;

    private LogicalExpToString(LogicalExp exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return IntExp.isNA(exp.get(index));
    }

    @Override
    public String get(int index) {
      return exp.get(index) == 1 ? "TRUE" : "FALSE";
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class LogicalExpToLogical implements AtomicAccessor<Logical> {
    private final LogicalExp exp;

    public LogicalExpToLogical(LogicalExp exp) {
      this.exp = exp;
    }

    @Override
    public int length() {
      return exp.length();
    }

    @Override
    public boolean isNA(int index) {
      return IntExp.isNA(exp.get(index));
    }

    @Override
    public Logical get(int index) {
      return Logical.valueOf(exp.get(index));
    }
  }
}