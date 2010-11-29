/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.lang;

import java.util.Arrays;
import java.util.Iterator;

public class LogicalExp extends AtomicExp implements Iterable<Logical>, HasElements,
    WidensToInt, WidensToDouble, WidensToString {
  public static final String TYPE_NAME = "logical";
  public static final int TYPE_CODE = 10;

  public static int NA = IntExp.NA;

  public static LogicalExp TRUE = new LogicalExp(1);
  public static LogicalExp FALSE = new LogicalExp(0);

  private int[] values;

  /**
   * Constructs a Logical vector from a list of boolean values
   */
  public LogicalExp(boolean... values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i] ? 1 : 0;
    }
  }

  public LogicalExp(int[] values, PairList attributes) {
     super(attributes);
     this.values = Arrays.copyOf(values, values.length);
  }

  public LogicalExp(int... values) {
    this(values, NullExp.INSTANCE);
  }

  public LogicalExp(Logical... values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i].getInternalValue();
    }
  }


  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public int getInt(int index) {
    return values[index];
  }

  @Override
  public double getDouble(int index) {
    int value = values[index];
    return value == IntExp.NA ? DoubleExp.NA : (double) value;
  }

  @Override
  public SEXP getExp(int index) {
    return new LogicalExp(values[index]);
  }



  @Override
  public String getString(int index) {
    int value = values[index];
    if(value == IntExp.NA) {
      return StringExp.NA;
    } else if(value == 0) {
      return "FALSE";
    } else {
      return "TRUE";
    }
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Logical asLogical() {
    if (values[0] == IntExp.NA) {
      return Logical.NA;
    } else {
      return values[0] == 0 ? Logical.FALSE : Logical.TRUE;
    }
  }

  @Override
  public double asReal() {
    if(values.length == 0) {
      return DoubleExp.NA;
    } else {
      return values[0];
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Logical> iterator() {
    return new Iterator<Logical>() {
      private int i=0;

      @Override
      public boolean hasNext() {
        return i<values.length;
      }

      @Override
      public Logical next() {
        return Logical.valueOf(values[i++]);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LogicalExp that = (LogicalExp) o;

    if (!Arrays.equals(values, that.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    if (length() == 1) {
      return toString(values[0]);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i != values.length; ++i) {
        if(i > 0) {
          sb.append(", ");
        }
        sb.append(toString(values[i]));
      }
      sb.append("]");
      return sb.toString();
    }
  }

  @Override
  public boolean isWiderThan(Object vector) {
    return vector instanceof LogicalExp;
  }

  @Override
  public Builder newCopyBuilder() {
    return null;
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder(this);
  }

  private String toString(int x) {
    if (x == 1) {
      return "TRUE";
    } else if (x == 0) {
      return "FALSE";
    } else {
      return "NA";
    }
  }

  @Override
  public Class getElementClass() {
    return Integer.TYPE;
  }

  private static class Builder implements HasElements.Builder<IntExp, WidensToInt> {
    private PairList attributes;
    private int values[];

    private Builder(int initialSize) {
      values = new int[initialSize];
      Arrays.fill(values, NA);
    }

    private Builder(LogicalExp exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      this.attributes = exp.attributes;
    }

    public Builder set(int index, int value) {
      if(values.length <= index) {
        int copy[] = Arrays.copyOf(values, index+1);
        Arrays.fill(copy, values.length, copy.length, NA);
        values = copy;
      }
      values[index] = value;
      return this;
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, WidensToInt source, int sourceIndex) {
      return set(destinationIndex, source.getInt(sourceIndex));
    }

    @Override
    public IntExp build() {
      return new IntExp(values, attributes);
    }
  }
}
