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
    return exp instanceof Symbol;
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
      if(!node.getTag().equals(Symbol.NAMES)) {
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
    return exp.getAttribute(Symbol.CLASS) != Null.INSTANCE;
  }

  public static boolean isCall(SEXP exp) {
    return exp instanceof FunctionCall;
  }

  public static boolean isLanguage(SEXP exp) {
    return exp instanceof Symbol ||
            exp instanceof FunctionCall ||
            exp instanceof ExpressionVector;

  }

  public static boolean isFunction(SEXP exp) {
    return exp instanceof Function;
  }

  public static boolean isSingle(SEXP exp) {
    throw new EvalException("type \"single\" unimplemented in R");
  }

  public static LogicalVector isNA(Vector list) {
    LogicalVector.Builder result = new LogicalVector.Builder(list.length());
    for(int i=0;i!=list.length();++i) {
      result.set(i, list.isElementNA(i));
    }
    result.setAttribute(Symbol.DIM, list.getAttribute(Symbol.DIM));
    result.setAttribute(Symbol.NAMES, list.getAttribute(Symbol.NAMES));

    return result.build();
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

  public static StringVector asCharacter(Vector source) {
    return (StringVector) convertVector(new StringVector.Builder(), source);
  }

  public static StringVector asCharacter(Symbol symbol) {
    return new StringVector( symbol.getPrintName() );
  }

  public static LogicalVector asLogical(Vector vector) {
    return (LogicalVector) convertVector(new LogicalVector.Builder(), vector);
  }

  public static IntVector asInteger(Vector source) {
      return (IntVector) convertVector(new IntVector.Builder(), source);
  }

  public static DoubleVector asDouble(Vector source) {
    return (DoubleVector) convertVector(new DoubleVector.Builder(), source);
  }

  private static Vector convertVector(Vector.Builder builder, Vector source) {
    for(int i=0;i!=source.length();++i) {
      builder.addFrom(source, i);
    }
    return builder.build();
  }

  @Primitive("as.vector")
  public static SEXP asVector(Vector x, String mode) {
    Vector.Builder result;
    if("any".equals(mode)) {
      result = x.getVectorType().newBuilder();
    } else if("character".equals(mode)) {
      result = new StringVector.Builder();
    } else if("logical".equals(mode)) {
      result = new LogicalVector.Builder(x.length());
    } else if("numeric".equals(mode)) {
      result = new DoubleVector.Builder(x.length());
    } else if("list".equals(mode)) {
      result = new ListVector.Builder();
    } else if("pairlist".equals(mode)) {
      // a pairlist is actually not a vector, so bail from here
      // as a special case
      return asPairList(x);
    } else {
      throw new EvalException("invalid 'mode' argument");
    }

    for(int i=0;i!=x.length();++i) {
      result.setFrom(i, x, i);
    }
    SEXP names = x.getNames();
    if(names.length() > 0) {
      result.setAttribute(Attributes.NAMES, names);
    }
    return result.build();
  }

  private static PairList asPairList(Vector x) {
    PairList.Builder builder = new PairList.Builder();
    for(int i=0;i!=x.length();++i) {
      builder.add(x.getName(i), x.getElementAsSEXP(i));
    }
    return builder.build();
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

  public static StringVector ls(Environment environment, boolean allNames) {
    StringVector.Builder names = new StringVector.Builder();
    for(Symbol name : environment.getSymbolNames()) {
      if(allNames || ! name.getPrintName().startsWith(".")) {
        names.add(name.getPrintName());
      }
    }
    return names.build();
  }

  public static boolean identical(SEXP x, SEXP y, boolean numericallyEqual, boolean singleNA, boolean attributesAsSet) {
    if(!numericallyEqual || !singleNA || !attributesAsSet) {
      throw new EvalException("identical implementation only supports num.eq = TRUE, single.NA = TRUE, attrib.as.set = TRUE");
    }
    return x.equals(y);
  }

  @Primitive("dim")
  public static SEXP getDimensions(SEXP sexp) {
    return sexp.getAttribute(Symbol.DIM);
  }

  @Primitive("dim<-")
  public static SEXP setDimensions(SEXP exp, AtomicVector vector) {
    int dim[] = new int[vector.length()];
    int prod = 1;
    for(int i=0;i!=vector.length();++i) {
      dim[i] = vector.getElementAsInt(i);
      prod *= dim[i];
    }

    if(prod != exp.length()) {
      throw new EvalException("dims [product %d] do not match the length of object [%d]", prod, exp.length());
    }

    return exp.setAttribute(Attributes.DIM, new IntVector(dim));
  }

  @Primitive("dimnames")
  public static SEXP getDimensionNames(SEXP exp) {
    return exp.getAttribute(Symbol.DIMNAMES);
  }

  public static PairList attributes(SEXP sexp) {
    return sexp.getAttributes();
  }

  @Primitive("attr")
  public static SEXP getAttribute(SEXP exp, String which) {
    SEXP partialMatch = Null.INSTANCE;
    int partialMatchCount = 0;

    for(PairList.Node node : exp.getAttributes().nodes()) {
      String name = node.getTag().getPrintName();
      if(name.equals(which)) {
        return node.getValue();
      } else if(name.startsWith(which)) {
        partialMatch = node.getValue();
        partialMatchCount ++;
      }
    }
    return partialMatchCount == 1 ? partialMatch : Null.INSTANCE;
  }

  @Primitive("attributes<-")
  public static SEXP setAttributes(SEXP exp, ListVector attributes) {
    return exp.setAttributes(attributes);
  }

  public static ListVector list(@ArgumentList ListVector arguments) {
    return arguments;
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
    return environment.findVariable(new Symbol(x), modePredicate(mode), inherits)
        != Symbol.UNBOUND_VALUE;
  }

  public static SEXP get(String x, Environment environment, String mode, boolean inherits) {
    return environment.findVariable(new Symbol(x), modePredicate(mode), inherits);
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

  public static SEXP setNames(SEXP exp, SEXP names) {
    return exp.setAttribute("names", names);
  }

  @Primitive("class")
  public static StringVector getClass(SEXP exp) {
    return exp.getClassAttribute();
  }

  @Primitive("class<-")
  public static SEXP setClass(SEXP exp, Vector classes) {
    return exp.setAttribute("class", classes);
  }

  @Primitive("attr<-")
  public static SEXP setAttribute(SEXP exp, String which, SEXP value) {
    return exp.setAttribute(which, value);
  }

  public static SEXP oldClass(SEXP exp) {
    if(!exp.hasAttributes()) {
      return Null.INSTANCE;
    }
    return exp.getAttribute(Symbol.CLASS);
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

  public static EvalResult invisible(SEXP value) {
    return EvalResult.invisible(value);
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

  @Visible(false)
  public static Environment attach(@Current Environment rho, SEXP what, @Indices int pos, String name) {

    //By default the database is attached in position 2 in the search path,
    // immediately after the user's workspace and before all previously loaded packages and
    // previously attached databases. This can be altered to attach later in the search
    // path with the pos option, but you cannot attach at pos=1.

    if(pos < 2) {
      throw new EvalException("Attachment position must be 2 or greater");
    }

    Environment child = rho.getGlobalEnvironment();
    for(int i=2;i!=pos;++i) {
      child = child.getParent();
    }

    Environment newEnv = Environment.createChildEnvironment(child.getParent());
    child.setParent(newEnv);

    // copy all values from the provided environment into the
    // new environment
    if(what instanceof Environment) {
      Environment source = (Environment) what;
      for(Symbol symbol : source.getSymbolNames()) {
        newEnv.setVariable(symbol, source.getVariable(symbol));
      }
    }

    return newEnv;
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
    if(exp.length() == 0) {
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

  public static ListVector options(@Current Context context, @ArgumentList ListVector arguments) {
    Context.Options options = context.getGlobals().options;
    ListVector.Builder results = ListVector.newBuilder();

    for(NamedValue argument : arguments.namedValues()) {
      if(argument.hasName()) {
        String name = argument.getName();
        results.add(name, options.set(name, argument.getValue()));

      } else if(argument.getValue() instanceof StringVector) {
        String name = ((StringVector) argument.getValue()).getElementAsString(0);
        results.add(name, options.get(name));

      } else {
        throw new EvalException("invalid argument");
      }
    }

    return results.build();
  }

}
