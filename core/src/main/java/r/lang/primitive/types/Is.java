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

package r.lang.primitive.types;

import r.lang.*;
import r.lang.primitive.PrimitiveFunction;

public class Is {

  private static abstract class Base extends PrimitiveFunction {

    private final Class<? extends SEXP> expectedClass;

    protected Base(Class<? extends SEXP> expectedClass) {
      this.expectedClass = expectedClass;
    }

    @Override
    public SEXP apply(LangExp call, NillOrListExp args, EnvExp rho) {
      return new LogicalExp(args.getClass().equals(expectedClass));
    }

  }

  public static class Null extends Base {
    public Null() {
      super(NilExp.class);
    }
  }

  public static class Logical extends Base {
    public Logical() {
      super(LogicalExp.class);
    }
  }

  public static class Integer extends Base {
    public Integer() {
      super(IntExp.class);
    }
  }

  public static class Real extends Base {
    public Real() {
      super(RealExp.class);
    }
  }

  public static class Double extends Base {
    public Double() {
      super(RealExp.class);
    }
  }

  public static class Complex extends Base {
    public Complex() {
      super(ComplexExp.class);
    }
  }

  public static class Character extends Base {
    public Character() {
      super(StringExp.class);
    }
  }

  public static class Symbol extends Base {
    public Symbol() {
      super(SymbolExp.class);
    }
  }

  public static class Environment extends Base {
    public Environment() {
      super(EnvExp.class);
    }
  }

  public class Expression extends Base {
    public Expression() {
      super(ExpExp.class);
    }
  }


}
