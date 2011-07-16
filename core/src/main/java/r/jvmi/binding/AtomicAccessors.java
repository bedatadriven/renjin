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

  private AtomicAccessors() {
  }

  public static boolean haveAccessor(SEXP provided, Class elementClass) {
    return create(provided, elementClass) != null;
  }
  
  public static AtomicAccessor create(SEXP sexp, Class clazz) {
    if(sexp instanceof Vector) {
      return create((Vector)sexp, clazz);
    } else {
      return null;
    }
  }

  public static AtomicAccessor create(Vector exp, Class elementType) {
   
    if(exp instanceof LogicalVector) {
      if(elementType == Logical.class) {
        return new LogicalAccessor(exp);
      } else if(elementType == Boolean.TYPE) {
        return new BooleanAccessor(exp);
      } else if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new IntAccessor(exp);
      } else if(elementType == Double.TYPE || elementType == Double.class) {
        return new DoubleAccessor(exp);
      } else if(elementType == String.class) {
        return new StringAccessor(exp);
      } else {
        return null;
      }

    } else if(exp instanceof IntVector) {
      if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new IntAccessor((IntVector) exp);
      } else if(elementType == Double.TYPE || elementType == Double.class) {
        return new DoubleAccessor((IntVector) exp);
      } else if(elementType == String.class) {
        return new StringAccessor((IntVector) exp);
      } else if(elementType == Boolean.TYPE || elementType == Boolean.class) {
        return new BooleanAccessor((IntVector)exp);
      } else {
        return null;
      }

    } else if(exp instanceof DoubleVector) {
      if(elementType == Double.TYPE || elementType == Double.class) {
        return new DoubleAccessor((DoubleVector) exp);
      } else if(elementType == Boolean.TYPE || elementType == Boolean.class) {
        return new BooleanAccessor((DoubleVector)exp);
      } else if(elementType == Integer.TYPE || elementType == Integer.class) {
        return new IntAccessor((DoubleVector)exp);
      } else if(elementType == String.class) {
        return new StringAccessor((DoubleVector) exp);
      }


    } else if(exp instanceof StringVector) {
      if(elementType == String.class) {
        return new StringAccessor((StringVector) exp);
      }

    } else if(exp instanceof Null) {
      return NullAccessor.INSTANCE;
    }

    return null;
  }
  
  private static abstract class BaseAccessor<T> implements AtomicAccessor<T> {

    protected final Vector vector;
    
    public BaseAccessor(Vector vector) {
      super();
      this.vector = vector;
    }

    @Override
    public final int length() {
      return vector.length();
    }

    @Override
    public final boolean isNA(int index) {
      return vector.isElementNA(index);
    }
  }

  private static class IntAccessor extends BaseAccessor<Integer> {
    public IntAccessor(Vector vector) {
      super(vector);
    }
    @Override
    public Integer get(int index) {
      return vector.getElementAsInt(index);
    }
  }
  private static class DoubleAccessor extends BaseAccessor<Double> {
    public DoubleAccessor(Vector vector) {
      super(vector);
    }
    
    @Override
    public Double get(int index) {
      return vector.getElementAsDouble(index);
    }
  }
  
  private static class BooleanAccessor extends BaseAccessor<Boolean> {
    public BooleanAccessor(Vector vector) {
      super(vector);
    }
    
    @Override
    public Boolean get(int index) {
      if(vector.isElementNA(index)) {
        throw new UnsupportedOperationException("an NA value cannot be cast to a Java boolean value");
      }
      return vector.getElementAsLogical(index) == Logical.TRUE;
    }
  }
  
  private static class StringAccessor extends BaseAccessor<String> {
    public StringAccessor(Vector vector) {
      super(vector);
    }
    
    @Override
    public String get(int index) {
      return vector.getElementAsString(index);
    }
  }

  private static class LogicalAccessor extends BaseAccessor<Logical> {
    public LogicalAccessor(Vector vector) {
      super(vector);
    }
    
    @Override
    public Logical get(int index) {
      return vector.getElementAsLogical(index);
    }
  }
  private static class NullAccessor implements AtomicAccessor<Void> {

    static NullAccessor INSTANCE = new NullAccessor();
    
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

  
  

}