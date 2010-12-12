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

package r.lang.primitive;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.*;
import r.parser.ParseUtil;

import java.util.Arrays;
import java.util.List;

import static r.lang.CollectionUtils.modePredicate;

/**
 * Primitive type inspection and coercion functions
 */
public class Types {

  public static boolean isNull(SEXP exp) {
    return exp == Null.INSTANCE;
  }

  public static boolean isLogical(SEXP exp) {
    return exp instanceof LogicalVector;
  }

  public static boolean isInteger(SEXP exp) {
    return exp instanceof IntVector;
  }

  public static boolean isReal(SEXP exp) {
    return exp instanceof DoubleVector;
  }

  public static boolean isDouble(SEXP exp) {
    return exp instanceof DoubleVector;
  }

  public static boolean isComplex(SEXP exp) {
    return exp instanceof ComplexVector;
  }

  public static boolean isCharacter(SEXP exp) {
    return exp instanceof StringVector;
  }

  public static boolean isSymbol(SEXP exp) {
    return exp instanceof SymbolExp;
  }

  public static boolean isEnvironment(SEXP exp) {
    return exp instanceof Environment;
  }

  public static boolean isExpression(SEXP exp) {
    return exp instanceof Environment;
  }

  public static boolean isList(SEXP exp) {
    return exp instanceof ListVector ||
           exp.getClass() == PairList.Node.class;
  }

  public static boolean isPairList(SEXP exp) {
    return exp instanceof PairList;
  }

  public static boolean isAtomic(SEXP exp) {
    return exp instanceof AtomicVector;
  }

  public static boolean isRecursive(SEXP exp) {
    return exp instanceof Recursive;
  }

  public static boolean isNumeric(SEXP exp) {
    return (exp instanceof IntVector && !exp.inherits("factor")) ||
            exp instanceof LogicalVector ||
            exp instanceof DoubleVector;
  }

  @Primitive("is.vector")
  public static boolean isVector(SEXP exp, String mode) {
    // first check for any attribute besides names
    for(PairList.Node node : exp.getAttributes().nodes()) {
      if(!node.getTag().equals(SymbolExp.NAMES)) {
        return false;
      }
    }

    // otherwise check
    if("logical".equals(mode)) {
      return exp instanceof LogicalVector;
    } else if("integer".equals(mode)) {
      return exp instanceof IntVector;
    } else if("numeric".equals(mode)) {
      return exp instanceof DoubleVector;
    } else if("complex".equals(mode)) {
      return exp instanceof ComplexVector;
    } else if("character".equals(mode)) {
      return exp instanceof StringVector;
    } else if("any".equals(mode)) {
      return exp instanceof AtomicVector || exp instanceof ListVector;
    } else if("list".equals(mode)) {
      return exp instanceof ListVector;
    } else {
      return false;
    }
  }

  @Primitive("is.object")
  public static boolean isObject(SEXP exp) {
    return exp.getAttribute(SymbolExp.CLASS) != Null.INSTANCE;
  }

  public static boolean isCall(SEXP exp) {
    return exp instanceof FunctionCall;
  }

  public static boolean isLanguage(SEXP exp) {
    return exp instanceof SymbolExp ||
            exp instanceof FunctionCall ||
            exp instanceof ExpressionVector;

  }

  public static boolean isFunction(SEXP exp) {
    return exp instanceof Function;
  }

  public static boolean isSingle(SEXP exp) {
    throw new EvalException("type \"single\" unimplemented in R");
  }

  public static boolean isNA(@AllowNA double value) {
    return DoubleVector.isNA(value);
  }

  public static boolean isNA(@AllowNA String value) {
    return StringVector.isNA(value);
  }

  public static boolean isNA(@AllowNA int value) {
    return IntVector.isNA(value);
  }

  public static boolean[] isNA(ListVector list) {
    boolean[] result = new boolean[list.length()];
    int i = 0;
    for(SEXP element : list) {
      result[i++] = element instanceof AtomicVector &&
                    element.length() == 1 &&
                    ((AtomicVector) element).isElementNA(0);
    }
    return result;
  }

  public static boolean isNaN(double value) {
    return DoubleVector.isNaN(value);
  }

  public static boolean isFinite(double value) {
    return !Double.isInfinite(value);
  }

  public static boolean isInfinite(double value) {
    return Double.isInfinite(value);
  }

  public static String asCharacter(String value) {
    return value;
  }

  public static StringVector asCharacter(ListVector list) {
    StringVector.Builder result = new StringVector.Builder();
    for(SEXP element : list) {
      if(element.length() == 1 && element instanceof AtomicVector) {
        result.add( ((AtomicVector) element).getElementAsString(0) );
      } else {
        result.add( Parse.deparse(element) );
      }
    }
    return result.build();
  }

  public static LogicalVector asLogical(Vector vector) {
    int result[] = new int[vector.length()];
    for(int i=0;i!=result.length; ++i) {
      result[i] = vector.getElementAsLogical(i).getInternalValue();
    }
    return new LogicalVector(result);
  }

  public static StringVector asCharacter(SymbolExp symbol) {
    return new StringVector( symbol.getPrintName() );
  }

  public static DoubleVector asDouble(DoubleVector exp) {
    return exp;
  }

  public static double asDouble(int value) {
    return (double)value;
  }

  public static double asDouble(String value) {
    return ParseUtil.parseDouble(value);
  }

  public static int asInteger(double x) {
    return (int) x;
  }

  public static IntVector asInteger(IntVector exp) {
    return exp;
  }

  public static int asInteger(String x) {
    return (int)ParseUtil.parseDouble(x);
  }

  public static Environment asEnvironment(Environment arg) {
    return arg;
  }

  public static Environment asEnvironment(@Current Environment rho, double index) {
    Environment result = rho.getGlobalEnvironment();
    for(int i=2;i<index;++i) {
      if(result == Environment.EMPTY) {
        throw new EvalException("invalid 'pos' argument");
      }
      result = result.getParent();
    }
    return result;
  }

  public static Environment getParentEnv(Environment environment) {
    return environment.getParent();
  }

  public static Environment setParentEnv(Environment environment, Environment newParent) {
    environment.setParent(newParent);
    return environment;
  }

  public static ListVector list(@ArgumentList PairList arguments) {

    int n = arguments.length();
    SEXP values[] = new SEXP[n];
    String names[] = new String[n];

    int index=0;
    boolean haveNames = false;

    for(PairList.Node arg : arguments.nodes()) {
      values[index] = arg.getValue();
      if(arg.hasTag()) {
        names[index] = arg.getTag().getPrintName();
        haveNames = true;
      } else {
        names[index] = "";
      }
      index++;
    }

    if(haveNames) {
      return new ListVector(values, PairList.Node.buildList(SymbolExp.NAMES, new StringVector(names)).build());
    } else {
      return new ListVector(values);
    }
  }

  public static Environment environment(@Current Environment rho) {
    return rho.getGlobalEnvironment();
  }

  public static SEXP environment(@Current Environment rho, SEXP exp) {
    if(exp == Null.INSTANCE) {
      // if the user passes null, we return the current exp
      return rho;
    } else if(exp instanceof Closure)  {
      return ((Closure) exp).getEnclosingEnvironment();
    } else {
      return Null.INSTANCE;
    }
  }

  public static Environment parentFrame(@Current Environment rho, int n) {
    Environment parent = rho;
    while(n > 0 && rho != rho.getGlobalEnvironment()) {
      parent = rho.getParent();
      --n;
    }
    return parent;
  }

  public static Environment newEnv(boolean hash, Environment parent, int size) {
    return Environment.createChildEnvironment(parent);
  }

  public static Environment baseEnv(@Current Environment rho) {
    return rho.getBaseEnvironment();
  }

  public static Environment globalEnv(@Current Environment rho) {
    return rho.getGlobalEnvironment();
  }

  public static boolean exists(String x, Environment environment, String mode, boolean inherits) {
    return environment.findVariable(new SymbolExp(x), modePredicate(mode), inherits)
        != SymbolExp.UNBOUND_VALUE;
  }

  public static SEXP get(String x, Environment environment, String mode, boolean inherits) {
    return environment.findVariable(new SymbolExp(x), modePredicate(mode), inherits);
  }

  public static int length(SEXP exp) {
    return exp.length();
  }

  public static SEXP vector(String mode, @Indices int length) {
    if("logical".equals(mode)) {
      return new LogicalVector(new int[length]);

    } else if("integer".equals(mode)) {
      return new IntVector(new int[length]);

    } else if("numeric".equals(mode)) {
      return new DoubleVector(new double[length]);

    } else if("complex".equals(mode)) {
      throw new UnsupportedOperationException("implement me!");

    } else if("character".equals(mode)) {
      String values[] = new String[length];
      Arrays.fill(values, "");
      return new StringVector(values);

    } else if("list".equals(mode)) {
      SEXP values[] = new SEXP[length];
      Arrays.fill(values, Null.INSTANCE);
      return new ListVector(values);

    } else if("pairlist".equals(mode)) {
      SEXP values[] = new SEXP[length];
      Arrays.fill(values, Null.INSTANCE);
      return PairList.Node.fromArray(values);

    } else {
      throw new EvalException(String.format("vector: cannot make a vector of mode '%s'.", mode));
    }
  }

  public static String typeof(SEXP exp) {
    return exp.getTypeName();
  }

  public static SEXP getNames(SEXP exp) {
    return exp.getNames();
  }

  public static SEXP setNames(SEXP exp, StringVector names) {
    return exp.setNames(names);
  }

  @Primitive("class")
  public static StringVector getClass(SEXP exp) {
    return exp.getClassAttribute();
  }

  @Primitive("class<-")
  public static SEXP setClass(SEXP exp, StringVector classes) {
    return exp.setClass(classes);
  }

  public static SEXP setClass(SEXP exp, ListVector list) {
    return exp.setClass(Types.asCharacter(list));
  }

  @Primitive("attr<-")
  public static SEXP setAttribute(SEXP exp, String which, SEXP value) {
    return exp.setAttribute(which, value);
  }

  public static SEXP oldClass(SEXP exp) {
    if(!exp.hasAttributes()) {
      return Null.INSTANCE;
    }
    return exp.getAttribute(SymbolExp.CLASS);
  }

  public static boolean inherits(SEXP exp, StringVector what) {
    StringVector classes = getClass(exp);
    for(String whatClass : what) {
      if(Iterables.contains(classes, whatClass)) {
        return true;
      }
    }
    return false;
  }

  public static SEXP inherits(SEXP exp, StringVector what, boolean which) {
    if(!which) {
      return new LogicalVector( inherits(exp, what) );
    }
    StringVector classes = getClass(exp);
    int result[] = new int[what.length()];

    for(int i=0; i!=what.length();++i) {
      result[i] = Iterables.indexOf(classes, Predicates.equalTo( what.getElement(i)) ) + 1;
    }
    return new IntVector( result );
  }

  public static StringVector search(@Current Environment rho) {
    List<String> names = Lists.newArrayList();
    Environment env = rho.getGlobalEnvironment();
    while(env != Environment.EMPTY) {
      names.add(env.getName());
      env = env.getParent();
    }
    return new StringVector(names);
  }

  public static boolean isFactor(SEXP exp) {
    return exp instanceof IntVector && exp.inherits("factor");
  }

  private static boolean isListFactor(ListVector list) {
    for(SEXP element : list) {
      if(element instanceof ListVector && !isListFactor((ListVector) element)) {
        return false;
      } else if(!isFactor(element)) {
        return false;
      }
    }
    return true;
  }

  @Primitive("islistfactor")
  public static boolean isListFactor(SEXP exp, boolean recursive) {

    if(!(exp instanceof ListVector)) {
      return false;    
    }

    ListVector vector = (ListVector) exp;
    for(SEXP element : vector) {
      if(element instanceof ListVector ) {
        if(!recursive || !isListFactor((ListVector) element)) {
          return false;
        }
      } else if(!isFactor(exp)) {
        return false;
      }
    }
    return true;
  }

}
