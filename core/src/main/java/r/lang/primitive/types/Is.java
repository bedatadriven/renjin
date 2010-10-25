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
import r.lang.exception.EvalException;
import r.lang.primitive.UnaryFunction;

public class Is {

  private static abstract class UnaryTest extends UnaryFunction {

    @Override
    public EvalResult apply(LangExp call, EnvExp rho, SEXP exp) {
      return new EvalResult(new LogicalExp(apply(exp)));
    }

    protected abstract boolean apply(SEXP exp);

  }

  private static abstract class ClassTest extends UnaryTest {

    private final Class<? extends SEXP> expectedClass;

    protected ClassTest(Class<? extends SEXP> expectedClass) {
      this.expectedClass = expectedClass;
    }

    protected boolean apply(SEXP exp) {
      return exp.getClass().equals(expectedClass);
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

  public class List extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp.getClass() == ListExp.class;
    }
  }

  public class PairList extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp.getClass() == ListExp.class;
    }
  }

  public class Atomic extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp instanceof AtomicExp;
    }
  }

  public class Recursive extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp instanceof RecursiveExp;
    }
  }

  public class Numeric extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {

      return (exp instanceof IntExp && !exp.inherits("factor")) ||
          exp instanceof LogicalExp ||
          exp instanceof RealExp;

    }
  }

  public class Call extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp instanceof LangExp;
    }
  }

  public class Language extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp instanceof SymbolExp ||
          exp instanceof LangExp ||
          exp instanceof ExpExp;
    }
  }

  public class Function extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      return exp instanceof FunExp;
    }
  }

  public class Single extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {
      throw new EvalException("type \"single\" unimplemented in R");
    }
  }

 /* What should is.vector do ?
  * In S, if an object has no attributes it is a vector, otherwise it isn't.
  * It seems to make more sense to check for a dim attribute.
  */
  public class Vector extends UnaryTest {
    @Override
    protected boolean apply(SEXP exp) {

//
//        if (!isString(CADR(args)) || LENGTH(CADR(args)) <= 0)
//          errorcall_return(call, R_MSG_mode);
//
//        PROTECT(ans = allocVector(LGLSXP, 1));
//        if (streql(CHAR(STRING_ELT(CADR(args), 0)), "any")) { /* ASCII */
//          LOGICAL(ans)[0] = isVector(CAR(args));/* from ./util.c */
//        }
//        else if (streql(CHAR(STRING_ELT(CADR(args), 0)), "numeric")) { /* ASCII */
//          LOGICAL(ans)[0] = (isNumeric(CAR(args)) &&
//              !isLogical(CAR(args)));
//        }
//        else if (streql(CHAR(STRING_ELT(CADR(args), 0)), /* ASCII */
//            type2char(TYPEOF(CAR(args))))) {
//          LOGICAL(ans)[0] = 1;
//        }
//        else
//          LOGICAL(ans)[0] = 0;
//
//        /* We allow a "names" attribute on any vector. */
//        if (LOGICAL(ans)[0] && ATTRIB(CAR(args)) != R_NilValue) {
//          a = ATTRIB(CAR(args));
//          while(a != R_NilValue) {
//            if (TAG(a) != R_NamesSymbol) {
//              LOGICAL(ans)[0] = 0;
//              break;
//            }
//            a = CDR(a);
//          }
//        }
//        UNPROTECT(1);
//        return (ans);
//        */
        throw new UnsupportedOperationException();
      }
    }

  }
