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
package org.renjin.primitives;

import com.google.common.base.Strings;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Options;
import org.renjin.invoke.annotations.*;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.IsNaVector;
import org.renjin.sexp.*;

/**
 * Builtin type inspection and coercion functions
 */
public class Types {

  @Builtin("is.null")
  public static boolean isNull(SEXP exp) {
    return exp == Null.INSTANCE;
  }

  @Builtin("is.logical")
  public static boolean isLogical(SEXP exp) {
    return exp instanceof LogicalVector;
  }

  @Builtin("is.integer")
  public static boolean isInteger(SEXP exp) {
    return exp instanceof IntVector;
  }

  @Builtin("is.real")
  public static boolean isReal(SEXP exp) {
    return exp instanceof DoubleVector;
  }

  @Builtin("is.double")
  public static boolean isDouble(SEXP exp) {
    return exp instanceof DoubleVector;
  }

  @Builtin("is.complex")
  public static boolean isComplex(SEXP exp) {
    return exp instanceof ComplexVector;
  }

  @Generic
  @Builtin("is.character")
  public static boolean isCharacter(SEXP exp) {
    return exp instanceof StringVector;
  }

  @Builtin("is.symbol")
  public static boolean isSymbol(SEXP exp) {
    return exp instanceof Symbol;
  } 


  @Builtin("is.environment")
  public static boolean isEnvironment(SEXP exp) {
    return exp instanceof Environment;
  }

  @Builtin("is.expression")
  public static boolean isExpression(SEXP exp) {
    return exp instanceof Environment;
  }

  @Builtin("is.list")
  public static boolean isList(SEXP exp) {
    return exp instanceof ListVector || exp.getClass() == PairList.Node.class;
  }

  @Builtin("is.pairlist")
  public static boolean isPairList(SEXP exp) {
    // strange, but true: 
    return exp instanceof PairList &&
        !(exp instanceof FunctionCall);
  }

  @Builtin("is.atomic")
  public static boolean isAtomic(SEXP exp) {
    return exp instanceof AtomicVector;
  }

  @Builtin("is.recursive")
  public static boolean isRecursive(SEXP exp) {
    return exp instanceof Recursive;
  }

  @Generic
  @Builtin("is.numeric")
  public static boolean isNumeric(SEXP exp) {
    return (exp instanceof IntVector && !exp.inherits("factor")) || exp instanceof DoubleVector;
  }

  @Generic
  @Builtin("is.matrix")
  public static boolean isMatrix(SEXP exp) {
    return exp.getAttribute(Symbols.DIM).length() == 2;
  }

  @Generic
  @Builtin("is.array")
  public static boolean isArray(SEXP exp) {
    return exp.getAttribute(Symbols.DIM).length() > 0;
  }

  @Internal("is.vector")
  public static boolean isVector(SEXP exp, String mode) {
    // first check for any attribute besides names
    if(exp.getAttributes().hasAnyBesidesName()) {
      return false;
    }

    // otherwise check
    if ("logical".equals(mode)) {
      return exp instanceof LogicalVector;
    } else if ("integer".equals(mode)) {
      return exp instanceof IntVector;
    } else if ("numeric".equals(mode)) {
      return exp instanceof DoubleVector;
    } else if ("complex".equals(mode)) {
      return exp instanceof ComplexVector;
    } else if ("character".equals(mode)) {
      return exp instanceof StringVector;
    } else if ("any".equals(mode)) {
      return exp instanceof AtomicVector || exp instanceof ListVector;
    } else if ("list".equals(mode)) {
      return exp instanceof ListVector;
    } else {
      return false;
    }
  }

  @Builtin("is.object")
  public static boolean isObject(SEXP exp) {
    return exp.isObject();
  }

  @Builtin("is.call")
  public static boolean isCall(SEXP exp) {
    return exp instanceof FunctionCall;
  }

  @Builtin("is.language")
  public static boolean isLanguage(SEXP exp) {
    return exp instanceof Symbol || exp instanceof FunctionCall
        || exp instanceof ExpressionVector;

  }

  @Builtin("is.function")
  public static boolean isFunction(SEXP exp) {
    return exp instanceof Function;
  }

  @Builtin("is.single")
  public static boolean isSingle(SEXP exp) {
    throw new EvalException("type \"single\" unimplemented in R");
  }

  @Generic
  @Builtin("is.na")
  public static LogicalVector isNA(final ListVector vector) {
    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder(vector.length());
    for (int i = 0; i != vector.length(); ++i) {
      SEXP element = vector.getElementAsSEXP(i);
      if(element instanceof AtomicVector && element.length()==1) {
        result.set(i, ((AtomicVector)element).isElementNaN(0));
      } else {
        result.set(i, false);
      }
    }
    result.setAttribute(Symbols.DIM, vector.getAttribute(Symbols.DIM));
    result.setAttribute(Symbols.NAMES, vector.getAttribute(Symbols.NAMES));
    result.setAttribute(Symbols.DIMNAMES, vector.getAttribute(Symbols.DIMNAMES));

    return result.build();
  }
  
  @Generic
  @Builtin("is.na")
  public static LogicalVector isNA(final AtomicVector vector) {
    if(vector.length() > 100 || vector instanceof DeferredComputation) {
      return new IsNaVector(vector);

    } else {
      LogicalArrayVector.Builder result = new LogicalArrayVector.Builder(vector.length());
      for (int i = 0; i != vector.length(); ++i) {
        result.set(i, vector.isElementNaN(i));
      }
      result.setAttribute(Symbols.DIM, vector.getAttribute(Symbols.DIM));
      result.setAttribute(Symbols.NAMES, vector.getAttribute(Symbols.NAMES));
      result.setAttribute(Symbols.DIMNAMES, vector.getAttribute(Symbols.DIMNAMES));

      return result.build();
    }
  }

  @Generic
  @Builtin("is.nan")
  @DataParallel(passNA = true)
  @Deferrable
  public static boolean isNaN(double value) {
    return !DoubleVector.isNA(value) && DoubleVector.isNaN(value);
  }

  @Generic
  @Builtin("is.finite")
  @DataParallel(passNA = true)
  @Deferrable
  public static boolean isFinite(@Recycle double value) {
    return !Double.isNaN(value) && !Double.isInfinite(value);
  }

  @Generic
  @Builtin("is.finite")
  @DataParallel(passNA = true)
  @Deferrable
  public static boolean isFinite(@Recycle String value) {
    return false;
  }

  @Generic
  @Builtin("is.infinite")
  @DataParallel(passNA = true)
  @Deferrable
  public static boolean isInfinite(@Recycle double value) {
    return Double.isInfinite(value);
  }

  @Generic
  @Builtin("is.infinite")
  @DataParallel(passNA = true)
  @Deferrable
  public static boolean isInfinite(@Recycle String value) {
    return false;
  }
  
  @Builtin
  public static boolean isS4(SEXP object) {
    if(object instanceof S4Object) {
      return true;
    }
    SEXP bit = object.getAttribute(Symbols.S4_BIT);
    if(bit instanceof LogicalVector && bit.length() == 1 && ((LogicalVector) bit).getElementAsLogical(0) == Logical.TRUE) {
      return true;
    } else {
      return false;
    }
  }

  @Internal
  public static SEXP setS4Object(SEXP object, boolean bool, boolean complete) {
    if(object instanceof S4Object) {
      return object;
    } else {
      return object.setAttribute(Symbols.S4_BIT, LogicalVector.TRUE);
    }
  }

  /**
   * Default implementation of as.function. Note that this is an
   * internal primitive called by the closure "as.function.default" in the
   * base package, so it is not itself generic.
   * 
   * @param list a ListVector containing the formal argument and the last element as the function body
   * @param envir the environment overwhich to close
   * @return a new Closure
   */
  @Internal("as.function.default")
  public static Closure asFunctionDefault(ListVector list, Environment envir) {
  
    PairList.Builder formals = new PairList.Builder();
    for(int i=0;(i+1)<list.length();++i) {
      String name = list.getName(i);
      if(Strings.isNullOrEmpty(name)) {
        throw new EvalException("formal arguments to a closure must be named");
      }
      formals.add(name, list.getElementAsSEXP(i));
    }
    SEXP body = list.getElementAsSEXP(list.length() - 1);
    
    return new Closure(envir, formals.build(), body);
  }

  @Builtin("is.raw")
  public static SEXP isRaw(Vector v) {
    return (new LogicalArrayVector(v.getVectorType() == RawVector.VECTOR_TYPE));
  }


  /**
   * Creates a new, unevaluated FunctionCall expression from a list vector.
   * 
   * @param list
   *          a list containing the function as the first element, followed by
   *          arguments
   * @return an unevaluated FunctionCall expression
   */
  @Builtin("as.call")
  public static FunctionCall asCall(ListVector list) {
    EvalException.check(list.length() > 0, "invalid length 0 argument");

    PairList.Builder arguments = new PairList.Builder();
    for (int i = 1; i != list.length(); ++i) {
      arguments.add(list.getName(i), list.getElementAsSEXP(i));
    }
    return new FunctionCall(list.getElementAsSEXP(0), arguments.build());
  }

  @Builtin
  public static ListVector list(@ArgumentList ListVector arguments) {
    return arguments;
  }

  @Internal
  public static PairList formals(Closure closure) {
    return closure.getFormals();
  }
  
  @Internal
  public static Null formals(PrimitiveFunction function) {
    return Null.INSTANCE;
  } 

  @Internal
  public static SEXP body(Closure closure) {
    return closure.getBody();
  }


  @Internal
  public static String typeof(SEXP exp) {
    return exp.getTypeName();
  }

  @Builtin
  public static SEXP invisible(@Current Context context, SEXP value) {
    context.setInvisibleFlag();
    return value;
  }

  @Builtin
  public static SEXP invisible(@Current Context context) {
    context.setInvisibleFlag();
    return Null.INSTANCE;
  }

  @Internal
  public static String Encoding(StringVector vector) {
    return "UTF-8";
  }

  @Internal
  public static StringVector setEncoding(StringVector vector,
      String encodingName) {
    if (encodingName.equals("UTF-8") || encodingName.equals("unknown")) {
      return vector;
    } else {
      throw new EvalException(
          "Only UTF-8 and unknown encoding are supported at this point");
    }
  }
  
  @Builtin
  public static boolean isFactor(SEXP exp) {
    return exp instanceof IntVector && exp.inherits("factor");
  }

  private static boolean isListFactor(ListVector list) {
    for (SEXP element : list) {
      if (element instanceof ListVector && !isListFactor((ListVector) element)) {
        return false;
      } else if (!isFactor(element)) {
        return false;
      }
    }
    return true;
  }

  @Internal
  public static boolean islistfactor(SEXP exp, boolean recursive) {

    if (!(exp instanceof ListVector)) {
      return false;
    }
    if (exp.length() == 0) {
      return false;
    }

    ListVector vector = (ListVector) exp;
    for (SEXP element : vector) {
      if (element instanceof ListVector) {
        if (!recursive || !isListFactor((ListVector) element)) {
          return false;
        }
      } else if (!isFactor(exp)) {
        return false;
      }
    }
    return true;
  }

  @Internal
  public static ListVector options(@Current Context context, @ArgumentList ListVector arguments) {
    Options options = context.getSession().getSingleton(Options.class);
    
    ListVector.NamedBuilder results = ListVector.newNamedBuilder();

    if (arguments.length() == 0) {
      // return all options as a list
      for (String name : options.names()) {
        results.add(name, options.get(name));
      }

    } else if (arguments.length() == 1
        && arguments.getElementAsSEXP(0) instanceof ListVector
        && StringVector.isNA(arguments.getName(0))) {
      ListVector list = (ListVector) arguments.getElementAsSEXP(0);
      if (list.getAttribute(Symbols.NAMES) == Null.INSTANCE) {
        throw new EvalException("list argument has no valid names");
      }
      for (NamedValue argument : list.namedValues()) {
        if (!argument.hasName()) {
          throw new EvalException("invalid argument");
        }
        String name = argument.getName();
        results.add(name, options.set(name, argument.getValue()));
      }

    } else {
      for (NamedValue argument : arguments.namedValues()) {
        if (argument.hasName()) {
          String name = argument.getName();
          results.add(name, options.set(name, argument.getValue()));

        } else if (argument.getValue() instanceof StringVector) {
          String name = ((StringVector) argument.getValue())
              .getElementAsString(0);
          results.add(name, options.get(name));

        } else {
          throw new EvalException("invalid argument");
        }
      }
      context.setInvisibleFlag();
    }
    return results.build();
  }
}
