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
import r.lang.primitive.UnaryFunction;

public class Is {

  private static abstract class ClassTest extends UnaryFunction {

    private final Class<? extends SEXP> expectedClass;

    protected ClassTest(Class<? extends SEXP> expectedClass) {
      this.expectedClass = expectedClass;
    }

    @Override
    public EvalResult apply(LangExp call, EnvExp rho, SEXP exp) {
      return new EvalResult(new LogicalExp(exp.getClass().equals(expectedClass)));
    }

  }

  public static class Null extends ClassTest {
    public Null() {
      super(NilExp.class);
    }
  }

  public static class Logical extends ClassTest {
    public Logical() {
      super(LogicalExp.class);
    }
  }

  public static class Integer extends ClassTest {
    public Integer() {
      super(IntExp.class);
    }
  }

  public static class Real extends ClassTest {
    public Real() {
      super(RealExp.class);
    }
  }

  public static class Double extends ClassTest {
    public Double() {
      super(RealExp.class);
    }
  }

  public static class Complex extends ClassTest {
    public Complex() {
      super(ComplexExp.class);
    }
  }

  public static class Character extends ClassTest {
    public Character() {
      super(StringExp.class);
    }
  }

  public static class Symbol extends ClassTest {
    public Symbol() {
      super(SymbolExp.class);
    }
  }

  public static class Environment extends ClassTest {
    public Environment() {
      super(EnvExp.class);
    }
  }

  public class Expression extends ClassTest {
    public Expression() {
      super(ExpExp.class);
    }
  }
}
