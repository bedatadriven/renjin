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

import com.google.common.base.Preconditions;
import r.lang.*;

import java.util.Arrays;

public class AtomicBuilders {

  private static final String LENGTH_ERROR = "length must be less than or equal to max length";


  public static boolean haveFor(PrimitiveMethod method) {
    Class type = method.getReturnType();
    return canCreateFor(type);
  }

  public static boolean canCreateFor(Class type) {
    return
        type == Double.TYPE ||
            type == Integer.TYPE ||
            type == Logical.class ||
            type == Boolean.TYPE ||
            type == String.class;
  }

  public static AtomicBuilder createFor(Class type, int length) {
    if(type == Integer.TYPE) {
      return new IntResultBuilder(length);

    } else if(type == Double.TYPE) {
      return new DoubleResultBuilder(length);

    } else if(type == Logical.class) {
      return new LogicalResultBuilder(length);

    } else if(type == Boolean.TYPE) {
      return new BooleanResultBuilder(length);

    } else if(type == String.class) {
      return new StringResultBuilder(length);

    } else {
      throw new UnsupportedOperationException("No AtomicBuilder for " + type.getName() );
    }
  }



  private static class IntResultBuilder implements AtomicBuilder<Integer> {
    private int values[];

    private IntResultBuilder(int length) {
      values = new int[length];
    }

    @Override
    public void set(int index, Integer value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = IntExp.NA;
    }

    @Override
    public IntExp build() {
      return new IntExp(values);
    }

    @Override
    public IntExp build(int length) {
      Preconditions.checkArgument(length <= values.length, LENGTH_ERROR);

      return new IntExp(Arrays.copyOf(values, length));
    }
  }

  private static class DoubleResultBuilder implements AtomicBuilder<Double> {
    private double values[];

    private DoubleResultBuilder(int length) {
      values = new double[length];
    }

    @Override
    public void set(int index, Double value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = DoubleExp.NA;
    }

    @Override
    public DoubleExp build() {
      return new DoubleExp( values );
    }

    @Override
    public DoubleExp build(int length) {
      Preconditions.checkArgument(length <= values.length, LENGTH_ERROR);

      return new DoubleExp( Arrays.copyOf(values, length));
    }
  }

  private static class BooleanResultBuilder implements AtomicBuilder<Boolean> {
    private int values[];

    private BooleanResultBuilder(int length) {
      values = new int[length];
    }

    @Override
    public void set(int index, Boolean value) {
      values[index] = value ? 1 : 0;
    }

    @Override
    public void setNA(int index) {
      values[index] = Logical.NA.getInternalValue();
    }

    @Override
    public LogicalExp build() {
      return new LogicalExp( values );
    }

    @Override
    public LogicalExp build(int length) {
      Preconditions.checkArgument(length <= values.length, LENGTH_ERROR);
      return new LogicalExp( Arrays.copyOf(values, length ));
    }
  }

  private static class LogicalResultBuilder implements AtomicBuilder<Logical> {
    private Logical[] values;

    public LogicalResultBuilder(int maxSize) {
      values = new Logical[maxSize];
    }


    @Override
    public void set(int index, Logical value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = Logical.NA;
    }

    @Override
    public AtomicExp build() {
      return new LogicalExp(values);
    }

    @Override
    public AtomicExp build(int length) {
      Preconditions.checkArgument(length <= values.length, LENGTH_ERROR);
      return new LogicalExp( Arrays.copyOf(values, length ));
    }
  }

  private static class StringResultBuilder implements AtomicBuilder<String> {

    private String values[];

    public StringResultBuilder(int length) {
      values = new String[length];
    }

    @Override
    public void set(int index, String value) {
      values[index] = value;
    }

    @Override
    public void setNA(int index) {
      values[index] = StringExp.NA;
    }

    @Override
    public StringExp build() {
      return new StringExp(values);
    }

    @Override
    public StringExp build(int length) {
      Preconditions.checkArgument(length <= values.length, LENGTH_ERROR);

      return new StringExp( Arrays.copyOf(values, length));
    }
  }

}
