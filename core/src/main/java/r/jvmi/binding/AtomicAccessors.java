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

package r.jvmi.binding;

import r.lang.*;
import r.parser.ParseUtil;

public class AtomicAccessors {

  private static final AtomicAccessor<Void> NULL_ACCESSOR = new NullAccessor();

  private AtomicAccessors() {
  }

  public static boolean haveAccessor(SEXP provided, Class elementClass) {
    return create(provided, elementClass) != null;
  }

  public static AtomicAccessor create(SEXP exp, Class elementType) {

    if(exp instanceof LogicalVector) {
      if(elementType == Logical.class) {
        return new LogicalExpToLogical((LogicalVector) exp);
      } else if(elementType == Boolean.TYPE) {
        return new LogicalExpToBoolean((LogicalVector) exp);
      } else if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new LogicalExpToInt((LogicalVector) exp);
      } else if(elementType == Double.TYPE || elementType == Double.class) {
        return new LogicalExpToDouble((LogicalVector) exp);
      } else if(elementType == String.class) {
        return new LogicalExpToString((LogicalVector) exp);
      } else {
        return null;
      }

    } else if(exp instanceof IntVector) {
      if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new IntExpAccessor((IntVector) exp);
      } else if(elementType == Double.TYPE || elementType == Double.class) {
        return new IntExpToDouble((IntVector) exp);
      } else if(elementType == String.class) {
        return new IntExpToString((IntVector) exp);
      } else if(elementType == Boolean.TYPE || elementType == Boolean.class) {
        return new IntExpToLogical((IntVector)exp);
      } else {
        return null;
      }


    } else if(exp instanceof DoubleVector) {
      if(elementType == Double.TYPE || elementType == Double.class) {
        return new DoubleExpAccessor((DoubleVector) exp);
      } else if(elementType == Boolean.TYPE || elementType == Boolean.class) {
        return new DoubleExpAsLogical((DoubleVector)exp);
      } else if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new DoubleExpAsInteger((DoubleVector)exp);
      } else if(elementType == String.class) {
        return new DoubleExpToString((DoubleVector) exp);
      }


    } else if(exp instanceof StringVector) {
      if(elementType == String.class) {
        return new StringExpAccessor((StringVector) exp);
      }

    } else if(exp instanceof Null) {
      return NULL_ACCESSOR;
    }

    return null;
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
    private LogicalVector exp;

    private LogicalExpToInt(LogicalVector vector) {
      this.exp = vector;
    }

    @Override
    public boolean isNA(int index) {
      return IntVector.isNA(exp.getElementAsInt(index));
    }

    @Override
    public Integer get(int index) {
      return exp.getElementAsInt(index);
    }

    @Override
    public int length() {
      return exp.length();
    }

  }

  private static class IntExpAccessor implements AtomicAccessor<Integer> {
    private IntVector exp;

    private IntExpAccessor(IntVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return exp.getElementAsInt(index) == IntVector.NA;
    }

    @Override
    public Integer get(int index) {
      return exp.getElementAsInt(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class DoubleExpAccessor implements AtomicAccessor<Double> {
    private DoubleVector exp;

    private DoubleExpAccessor(DoubleVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return DoubleVector.isNA(exp.get(index));
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

  private static class DoubleExpAsLogical implements AtomicAccessor<Boolean> {
    private DoubleVector exp;

    private DoubleExpAsLogical(DoubleVector exp) {
      this.exp = exp;
    }

    @Override
    public int length() {
      return exp.length();
    }

    @Override
    public boolean isNA(int index) {
      return DoubleVector.isNA( exp.get(index) );
    }

    @Override
    public Boolean get(int index) {
      return exp.get(index) != 0;
    }
  }

  private static class DoubleExpAsInteger implements AtomicAccessor<Integer> {
    private DoubleVector exp;

    private DoubleExpAsInteger(DoubleVector exp) {
      this.exp = exp;
    }

    @Override
    public int length() {
      return exp.length();
    }

    @Override
    public boolean isNA(int index) {
      return DoubleVector.isNA( exp.get(index) );
    }

    @Override
    public Integer get(int index) {
      return exp.getElementAsInt(index);
    }
  }

  private static class IntExpToDouble implements AtomicAccessor<Double> {
    private IntVector exp;

    private IntExpToDouble(IntVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return IntVector.isNA(exp.getElementAsInt(index));
    }

    @Override
    public Double get(int index) {
      return (double) exp.getElementAsInt(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class IntExpToLogical implements AtomicAccessor<Boolean> {
    private IntVector exp;

    private IntExpToLogical(IntVector exp) {
      this.exp = exp;
    }

    @Override
    public int length() {
      return exp.length();
    }

    @Override
    public boolean isNA(int index) {
      return IntVector.isNA( exp.getElementAsInt(index) );
    }

    @Override
    public Boolean get(int index) {
      return exp.getElementAsInt( index) != 0;
    }
  }


  private static class LogicalExpToDouble implements AtomicAccessor<Double> {
    private LogicalVector exp;

    private LogicalExpToDouble(LogicalVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return exp.getElementAsInt(index) == IntVector.NA;
    }

    @Override
    public Double get(int index) {
      return (double)exp.getElementAsDouble(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class LogicalExpToBoolean implements AtomicAccessor<Boolean> {
    private LogicalVector exp;

    private LogicalExpToBoolean(LogicalVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return exp.getElementAsInt(index) == IntVector.NA;
    }

    @Override
    public Boolean get(int index) {
      return exp.getElementAsInt(index) == 1;
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class StringExpAccessor implements AtomicAccessor<String> {
    private StringVector exp;

    private StringExpAccessor(StringVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return StringVector.isNA(exp.getElement(index));
    }

    @Override
    public String get(int index) {
      return exp.getElement(index);
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class DoubleExpToString implements AtomicAccessor<String> {
    private DoubleVector exp;

    private DoubleExpToString(DoubleVector exp) {
      this.exp = exp;
    }

    public boolean isNA(int index) {
      return DoubleVector.isNA(exp.get(index));
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
    private IntVector exp;

    private IntExpToString(IntVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return IntVector.isNA(exp.getElementAsInt(index));
    }

    @Override
    public String get(int index) {
      return ParseUtil.toString(exp.getElementAsInt(index));
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class LogicalExpToString implements AtomicAccessor<String> {
    private LogicalVector exp;

    private LogicalExpToString(LogicalVector exp) {
      this.exp = exp;
    }

    @Override
    public boolean isNA(int index) {
      return IntVector.isNA(exp.getElementAsInt(index));
    }

    @Override
    public String get(int index) {
      return exp.getElementAsInt(index) == 1 ? "TRUE" : "FALSE";
    }

    @Override
    public int length() {
      return exp.length();
    }
  }

  private static class LogicalExpToLogical implements AtomicAccessor<Logical> {
    private final LogicalVector exp;

    public LogicalExpToLogical(LogicalVector exp) {
      this.exp = exp;
    }

    @Override
    public int length() {
      return exp.length();
    }

    @Override
    public boolean isNA(int index) {
      return IntVector.isNA(exp.getElementAsInt(index));
    }

    @Override
    public Logical get(int index) {
      return Logical.valueOf(exp.getElementAsInt(index));
    }
  }
}