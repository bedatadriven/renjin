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

import com.google.common.base.*;
import com.google.common.collect.Lists;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.eval.Context.Type;
import org.renjin.eval.EvalException;
import org.renjin.eval.Options;
import org.renjin.invoke.annotations.*;
import org.renjin.invoke.reflection.converters.*;
import org.renjin.primitives.vector.ConstantDoubleVector;
import org.renjin.primitives.vector.ConvertingDoubleVector;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;
import org.renjin.sexp.Function;
import org.renjin.sexp.Vector.Builder;
import org.renjin.util.NamesBuilder;

import java.util.Arrays;
import java.util.List;

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
    return (exp instanceof IntVector && !exp.inherits("factor"))
        || exp instanceof LogicalVector || exp instanceof DoubleVector;
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

  private static class IsNaVector extends LogicalVector {
    private final Vector vector;

    private IsNaVector(Vector vector) {
      super(buildAttributes(vector));
      this.vector = vector;
    }

    private static AttributeMap buildAttributes(Vector vector) {
      AttributeMap sourceAttributes = vector.getAttributes();
      return AttributeMap.builder()
              .addIfNotNull(sourceAttributes, Symbols.DIM)
              .addIfNotNull(sourceAttributes, Symbols.NAMES)
              .addIfNotNull(sourceAttributes, Symbols.DIMNAMES)
              .build();
    }

    private IsNaVector(AttributeMap attributes, Vector vector) {
      super(attributes);
      this.vector = vector;
    }

    @Override
    public int length() {
      return vector.length();
    }

    @Override
    public int getElementAsRawLogical(int index) {
      return vector.isElementNA(index) ? 1 : 0;
    }

    @Override
    public boolean isConstantAccessTime() {
      return vector.isConstantAccessTime();
    }

    @Override
    protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
      return new IsNaVector(attributes, vector);
    }
  }

  @Generic
  @Builtin("is.na")
  public static LogicalVector isNA(final ListVector vector) {
    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder(vector.length());
    for (int i = 0; i != vector.length(); ++i) {
      SEXP element = vector.getElementAsSEXP(i);
      if(element instanceof AtomicVector && element.length()==1) {
        result.set(i, ((AtomicVector)element).isElementNA(0));
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
    if(vector.length() > 100) {
      return new IsNaVector(vector);

    } else {
      LogicalArrayVector.Builder result = new LogicalArrayVector.Builder(vector.length());
      for (int i = 0; i != vector.length(); ++i) {
        result.set(i, vector.isElementNA(i));
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
  public static boolean isFinite(double value) {
    return !Double.isNaN(value) && !Double.isInfinite(value);
  }

  @Generic
  @Builtin("is.infinite")
  @DataParallel(passNA = true)
  @Deferrable
  public static boolean isInfinite(@Recycle double value) {
    return Double.isInfinite(value);
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

  @Generic
  @Builtin("as.raw")
  public static RawVector asRaw(Vector source) {
    /*
     * Vector iv = (Vector) values; RawVector.Builder b = new
     * RawVector.Builder(); int value; Raw raw; for (int i = 0; i <
     * values.length(); i++) { value = iv.getElementAsInt(i); if (value < 0 ||
     * value > 255) { throw new
     * EvalException("out-of-range values treated as 0 in coercion to raw"); }
     * raw = new Raw(iv.getElementAsInt(i)); b.add(raw); } return (b.build());
     */
    return (RawVector) convertToAtomicVector(new RawVector.Builder(), source);
  }

  @Builtin("is.raw")
  public static SEXP isRaw(Vector v) {
    return (new LogicalArrayVector(v.getVectorType() == RawVector.VECTOR_TYPE));
  }

  @Internal
  public static RawVector rawToBits(RawVector vector) {
    RawVector.Builder bits = new RawVector.Builder();
    for(int i=0;i!=vector.length();++i) {
      int intValue = vector.getElementAsInt(i);
      for(int bit=0;bit!=8;++bit) {
        int mask = 1 << bit;
        if( (intValue & mask) != 0) {
          bits.add(1);
        } else {
          bits.add(0);
        }
      }
    }
    return bits.build();
  }
  
  @Internal
  public static StringVector rawToChar(RawVector vector, boolean multiple) {
    byte[] bytes = vector.toByteArray();
    if(multiple) {
      StringVector.Builder result = new StringVector.Builder(0, vector.length());
      for(int i=0;i!=vector.length();++i) {
        result.add(StringVector.valueOf(new String(bytes, i, 1)));
      }
      return result.build();
      
    } else {
      return StringVector.valueOf(new String(bytes));
    }
  }
  
  @Internal
  public static RawVector charToRaw(StringVector sv) {
    // the R lang docs inexplicably say that
    // this method converts a length-one character vector
    // to raw bytes 'without taking into account any declared encoding'
    
    // (AB) I think this means that we just dump out the 
    // string how ever it was stored internally. In R, the 
    // storage of a string depends on its encoding; in the JVM,
    // its always UTF-16. 
    
    // this implementation splits the difference and dumps
    // out the string as UTF-8. 
    
    if (sv.length() != 1) {
      throw new EvalException(
          "argument should be a character vector of length 1");
    }
    return new RawVector(sv.getElementAsString(0).getBytes(Charsets.UTF_8));
  }

  @Internal
  public static RawVector rawShift(RawVector rv, int n) {
    if (n > RawVector.NUM_BITS || n < (-1 * RawVector.NUM_BITS)) {
      throw new EvalException("argument 'shift' must be a small integer");
    }
    RawVector.Builder b = new RawVector.Builder();
    int r;
    for (int i = 0; i < rv.length(); i++) {
      if (n >= 0) {
        r = rv.getElementAsByte(i) << Math.abs(n);
      } else {
        r = rv.getElementAsByte(i) >> Math.abs(n);
      }
      b.add(r);
    }
    return (b.build());
  }

  @Internal
  public static RawVector intToBits(Vector vector) {
    RawVector.Builder bits = new RawVector.Builder();
    for(int i=0;i!=vector.length();++i) {

      int intValue = vector.getElementAsInt(i);
      for(int bit=0;bit!=Integer.SIZE;++bit) {
        int mask = 1 << bit;
        if( (intValue & mask) != 0) {
          bits.add(1);
        } else {
          bits.add(0);
        }
      }
    }
    return bits.build();
  }

  @Generic
  @Builtin("as.character")
  public static StringVector asCharacter(PairList.Node source) {
    return (StringVector) convertToStringVector(null, new StringVector.Builder(), source.toVector());
  }


  @Generic
  @Builtin("as.character")
  public static StringVector asCharacter(@Current Context context, Vector source) {
    if(source instanceof StringVector) {
      return (StringVector) source.setAttributes(AttributeMap.EMPTY);
    } else if(source.length() < 100) {
      return convertToStringVector(context, new StringVector.Builder(), source);
    } else {
      return new ConvertingStringVector(source);
    }
  }


  private static StringVector convertToStringVector(Context context, StringVector.Builder builder, Vector source) {
    if(source instanceof ListVector) {
      for (int i = 0; i != source.length(); ++i) {
        SEXP value = ((ListVector) source).getElementAsSEXP(i);
        if(value instanceof AtomicVector && value.length() == 1) {
          builder.addFrom((AtomicVector)value, 0);
        } else {
          builder.add(Deparse.deparseExp(context, value));
        }
      }
    } else {
      for (int i = 0; i != source.length(); ++i) {
        builder.addFrom(source, i);
      }
    }
    return builder.build();
  }

  @Generic
  @Builtin("as.character")
  public static StringVector asCharacter(Symbol symbol) {
    return StringVector.valueOf(symbol.getPrintName());
  }

  @Generic
  @Builtin("as.character")
  public static StringVector asCharacter(ExternalPtr<?> ptr) {
    Object instance = ptr.getInstance();
    if (StringConverter.accept(instance.getClass())) {
      return (StringVector) StringConverter.INSTANCE
          .convertToR((String) instance);
    } else if (StringArrayConverter.accept(instance.getClass())) {
      return (StringVector) StringArrayConverter.INSTANCE
          .convertToR(instance);
    } else {
      return StringArrayVector.valueOf(ptr.getInstance().toString());
    }
  }

  @Generic
  @Builtin("as.logical")
  public static LogicalVector asLogical(ExternalPtr ptr) {
    Object instance = ptr.getInstance();
    Class clazz = instance.getClass();
    if (BooleanConverter.accept(clazz)) {
      return BooleanConverter.INSTANCE
          .convertToR((Boolean) instance);
    } else if (BooleanArrayConverter.accept(clazz)) {
      return BooleanArrayConverter.INSTANCE
          .convertToR((Boolean[]) instance);
    } else {
      return new LogicalArrayVector(Logical.NA);
    }
  }

  @Generic
  @DataParallel
  @Builtin("as.logical")
  public static boolean asLogical(@DownCastComplex boolean x) {
    return x;
  }

  @Generic
  @DataParallel
  @Builtin("as.logical")
  public static Logical asLogical(String x) {
    String xLower = x.toLowerCase();
    if(xLower.equals("true")) {
      return Logical.TRUE;
    } else if(xLower.equals("false")) {
      return Logical.FALSE;
    } else {
      return Logical.NA;
    }
  }

  @Generic
  @Builtin("as.logical")
  public static LogicalVector asLogical(ListVector vector) {
    LogicalVector.Builder result = new LogicalArrayVector.Builder(0, vector.length());
    for(int i=0;i!=vector.length();++i) {
      SEXP element = vector.getElementAsSEXP(i);
      if(element)
    }

    return (LogicalVector) convertToAtomicVector(new LogicalArrayVector.Builder(), vector);
  }

  @Generic
  @Builtin("as.integer")
  public static IntVector asInteger(ExternalPtr ptr) {
    Object instance = ptr.getInstance();
    Class clazz = instance.getClass();
    if (IntegerConverter.accept(clazz)) {
      return (IntVector) IntegerConverter.INSTANCE
          .convertToR((Number) instance);
    } else if (IntegerArrayConverter.accept(clazz)) {
      return (IntVector) IntegerArrayConverter.INSTANCE
          .convertToR((Number[]) instance);
    } else {
      return IntVector.valueOf(IntVector.NA);
    }
  }

  @Generic
  @Builtin("as.integer")
  public static IntVector asInteger(Vector source) {
    return (IntVector) convertToAtomicVector(new IntArrayVector.Builder(), source);
  }

  @Generic
  @Builtin("as.double")
  public static DoubleVector asDouble(ExternalPtr ptr) {
    Object instance = ptr.getInstance();
    Class clazz = instance.getClass();
    if (DoubleConverter.accept(clazz)) {
      return (DoubleVector) DoubleConverter.INSTANCE
          .convertToR(instance);
    } else if (DoubleArrayConverter.accept(clazz)) {
      return (DoubleVector)new DoubleArrayConverter(clazz)
          .convertToR(instance);
    } else {
      return new DoubleArrayVector(DoubleVector.NA);
    }
  }

  @Generic
  @Builtin("as.double")
  public static DoubleVector asDouble(Vector source) {
    if(source instanceof DoubleVector) {
      return (DoubleVector) source.setAttributes(AttributeMap.EMPTY);
    } else if(source instanceof DeferredComputation || source.length() > 100) {
      return new ConvertingDoubleVector(source);
    } else {
      return (DoubleVector) convertToAtomicVector(new DoubleArrayVector.Builder(), source);
    }
  }

  private static Vector convertToAtomicVector(Vector.Builder builder, Vector source) {
    if(source instanceof ListVector) {
      for (int i = 0; i != source.length(); ++i) {
        SEXP value = ((ListVector) source).getElementAsSEXP(i);
        if(value instanceof AtomicVector && value.length() == 1) {
          builder.addFrom(value, 0);
        } else {
          builder.addNA();
        }
      }
    } else {
      for (int i = 0; i != source.length(); ++i) {
        builder.addFrom(source, i);
      }
    }
    return builder.build();
  }

  @Generic
  @Builtin("as.complex")
  @DataParallel
  public static Complex asComplex(@Recycle double x) {
    return new Complex(x,0);
  }

  @Generic
  @Builtin("as.complex")
  @DataParallel
  public static Complex asComplex(@Recycle Complex x) {
    return x;
  }
  
  @Generic
  @Internal("as.vector")
  public static SEXP asVector(Vector x, String mode) {

    if(mode.equals("any")) {
      return x.setAttributes(x.getAttributes().copyNames());
    }

    Vector.Builder result;
    if ("character".equals(mode)) {
      result = new StringVector.Builder();
    } else if ("logical".equals(mode)) {
      result = new LogicalArrayVector.Builder(x.length());
    } else if ("integer".equals(mode)) {
      result = new IntArrayVector.Builder(x.length());
    } else if ("numeric".equals(mode) || "double".equals(mode)) {
      result = new DoubleArrayVector.Builder(x.length());
    } else if ("complex".equals(mode)) {
      result = new ComplexArrayVector.Builder(x.length());
    } else if ("list".equals(mode)) {
      result = new ListVector.Builder();
    } else if ("pairlist".equals(mode)) {
      // a pairlist is actually not a vector, so bail from here
      // as a special case
      return asPairList(x);
    } else if ("symbol".equals(mode)) {
      // weird but seen in the base package
      if (x.length() == 0) {
        throw new EvalException(
            "invalid type/length (symbol/0) in vector allocation");
      }
      return Symbol.get(x.getElementAsString(0));
    } else if ("raw".equals(mode)) {
      result = new RawVector.Builder();
    } else {
      throw new EvalException("invalid 'mode' argument: " + mode);
    }

    for (int i = 0; i != x.length(); ++i) {
      result.setFrom(i, x, i);
    }
    SEXP names = x.getNames();
    if (names.length() > 0) {
      result.setAttribute(Symbols.NAMES, names);
    }
    return result.build();
  }

  @Generic
  @Internal("as.vector")
  public static SEXP asVector(PairList x, String mode) {
    Vector.Builder result;
    NamesBuilder names = NamesBuilder.withInitialCapacity(x.length());
    if ("character".equals(mode)) {
      result = new StringVector.Builder();
    } else if ("logical".equals(mode)) {
      result = new LogicalArrayVector.Builder(x.length());
    } else if ("numeric".equals(mode)) {
      result = new DoubleArrayVector.Builder(x.length());
    } else if ("list".equals(mode)) {
      result = new ListVector.Builder();
    } else if ("raw".equals(mode)) {
      result = new RawVector.Builder();
    } else {
      throw new EvalException("invalid 'mode' argument");
    }

    for (PairList.Node node : x.nodes()) {
      if (node.hasTag()) {
        names.add(node.getTag().getPrintName());
      } else {
        names.addNA();
      }
      result.add(node.getValue());
    }
    result.setAttribute(Symbols.NAMES.getPrintName(),
        names.build(result.length()));
    return result.build();
  }

  private static PairList asPairList(Vector x) {
    PairList.Builder builder = new PairList.Builder();
    for (int i = 0; i != x.length(); ++i) {
      builder.add(x.getName(i), x.getElementAsSEXP(i));
    }
    return builder.build();
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
  public static Environment asEnvironment(Environment arg) {
    return arg;
  }

  @Internal
  public static ListVector env2list(Environment env, boolean allNames) {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    for(Symbol name : env.getSymbolNames()) {
      if(allNames || !name.getPrintName().startsWith(".")) {
        list.add(name, env.getVariable(name));
      }
    }
    return list.build();
  }

  @Builtin("as.environment")
  public static Environment asEnvironment(@Current Context context, int pos) {
    Environment env;
    Context cptr;

    if (IntVector.isNA(pos) || pos < -1 || pos == 0) {
        throw new EvalException("invalid 'pos' argument");
    } else if (pos == -1) {
      /* make sure the context is a funcall */
      cptr = context;
      while( context.getType() != Type.FUNCTION && !cptr.isTopLevel() ) {
        cptr = cptr.getParent();
      }
      if( cptr.getType() != Type.FUNCTION) {
        throw new EvalException("no enclosing environment");
      }
      env = cptr.getCallingEnvironment();
      if (env == null) {
        throw new EvalException("invalid 'pos' argument");
      }
    } else {
      for (env = context.getGlobalEnvironment(); env != Environment.EMPTY && pos > 1;
          env = env.getParent()) {
        pos--;
      }
      if (pos != 1) {
        throw new EvalException("invalid 'pos' argument");
      }
    }
    return env;
  }
 
  @Builtin("as.environment")
  public static Environment asEnvironment(@Current Context context, String name) {
    
    if(name.equals(".GlobalEnv")) {
      return context.getGlobalEnvironment();
    }
    
    Environment result = context.getEnvironment();
    while(result != Environment.EMPTY) {
      if(Objects.equal(result.getName(), name)) {
        return result;
      }
      if(name.equals("package:base") && result == result.getBaseEnvironment()) {
        return result;
      }
      result = result.getParent();
      
    }
    throw new EvalException("no environment called '%s' on the search list", name); 
  }
  
  @Builtin("as.environment")
  public static Environment asEnvironment(ListVector list) {
    Environment env = Environment.createChildEnvironment(Environment.EMPTY);
    for(NamedValue namedValue : list.namedValues()) {
      env.setVariable(namedValue.getName(), namedValue.getValue());
    }
    return env;
  }
  
  @Builtin("as.environment")
  public static Environment asEnvironment(S4Object obj) {
    SEXP env = obj.getAttribute(Symbol.get(".xData"));
    if(env instanceof Environment) {
      return (Environment)env;
    }
    throw new EvalException("object does not extend 'environment'");
  }
  
  @Internal
  public static String environmentName(Environment env) {
    return env.getName();
  }

  @Internal("parent.env")
  public static Environment getParentEnv(Environment environment) {
    return environment.getParent();
  }

  @Internal("parent.env<-")
  public static Environment setParentEnv(Environment environment,
      Environment newParent) {
    environment.setParent(newParent);
    return environment;
  }

  @Internal
  public static StringVector ls(Environment environment, boolean allNames) {
    StringVector.Builder names = new StringVector.Builder();

    for (Symbol name : environment.getSymbolNames()) {
      if (allNames || !name.getPrintName().startsWith(".")) {
        names.add(name.getPrintName());
      }
    }
    return names.build();
  }
  
  @Internal
  public static void lockEnvironment(Environment env, boolean bindings) {
    env.lock(bindings);
  }

  @Internal
  public static void lockBinding(Symbol name, Environment env) {
    env.lockBinding(name);
  }

  @Internal
  public static void unlockBinding(Symbol name, Environment env) {
    env.unlockBinding(name);
  }
  
  @Internal
  public static boolean bindingIsLocked(Symbol name, Environment env) {
    return env.bindingIsLocked(name);
  }

  @Internal
  public static boolean environmentIsLocked(Environment env) {
    return env.isLocked();
  }

  /*----------------------------------------------------------------------
  
  do_libfixup
  
  This function copies the bindings in the loading environment to the
  library environment frame (the one that gets put in the search path)
  and removes the bindings from the loading environment.  Values that
  contain promises (created by delayedAssign, for example) are not forced.
  Values that are closures with environments equal to the loading
  environment are reparented to .GlobalEnv.  Finally, all bindings are
  removed from the loading environment.
  
  This routine can die if we automatically create a name space when
  loading a package.
   */
  @Internal("lib.fixup")
  public static Environment libfixup(Environment loadEnv, Environment libEnv) {
    for (Symbol name : loadEnv.getSymbolNames()) {
      SEXP value = loadEnv.getVariable(name);
      if (value instanceof Closure) {
        Closure closure = (Closure) value;
        if (closure.getEnclosingEnvironment() == loadEnv) {
          value = closure.setEnclosingEnvironment(libEnv);
        }
      }
      loadEnv.setVariable(name, value);
    }
    return libEnv;
  }

  @Internal
  public static SEXP drop(Vector x) {
    Vector dim = (Vector) x.getAttribute(Symbols.DIM);
    
    if(dim.length() == 0) {
      return x;
    } else {
    
      Vector dimnames = (Vector) x.getAttribute(Symbols.DIMNAMES);
      
      IntArrayVector.Builder newDim = new IntArrayVector.Builder();
      ListVector.Builder newDimnames = new ListVector.Builder();
      boolean haveDimNames = false;
      
      for(int i=0;i!=dim.length();++i) {
        if(dim.getElementAsInt(i) > 1) {
          newDim.add(dim.getElementAsInt(i));
          if(dimnames != Null.INSTANCE) {
            Vector dimNameElement = dimnames.getElementAsSEXP(i);
            if(dimNameElement != Null.INSTANCE) {
              haveDimNames = true;
            }
            newDimnames.add(dimNameElement);
          }
        }
      }
      
      if(newDim.length() == 0 ||
         (newDim.length() == 1 && dim.length() > 1)) {
        return x.setAttribute(Symbols.DIM, Null.INSTANCE)
                .setAttribute(Symbols.DIMNAMES, Null.INSTANCE);
      } else {
        return x.setAttribute(Symbols.DIM, newDim.build())
                .setAttribute(Symbols.DIMNAMES, newDimnames.build());
      }
      
    }
  }

  @Builtin
  public static ListVector list(@ArgumentList ListVector arguments) {
    return arguments;
  }

  @Internal
  public static Environment environment(@Current Context context) {
    // since this primitive is internal, we will be called by a wrapping closure,
    // so grab the parent context
    return context.getParent().getEnvironment();
  }

  @Internal
  public static SEXP environment(@Current Context context, SEXP exp) {
    if (exp == Null.INSTANCE) {
      // if the user passes null, we return the current exp
      // but since this primitive is internal, we will be called by a wrapping closure,
      // so grab the parent context      
      return context.getCallingEnvironment();
    } else if (exp instanceof Closure) {
      return ((Closure) exp).getEnclosingEnvironment();
    } else {
      return exp.getAttribute(Symbols.DOT_ENVIRONMENT);
    }
  }

  @Builtin("environment<-")
  public static SEXP setEnvironment(SEXP exp, Environment newRho) {
    if (exp instanceof Closure) {
      return ((Closure) exp).setEnclosingEnvironment(newRho);
    } else {
      return exp.setAttribute(Symbols.DOT_ENVIRONMENT.getPrintName(), newRho);
    }
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

  @Internal("new.env")
  public static Environment newEnv(boolean hash, Environment parent, int size) {
    return Environment.createChildEnvironment(parent);
  }

  @Builtin
  public static Environment baseenv(@Current Environment rho) {
    return rho.getBaseEnvironment();
  }

  @Builtin
  public static Environment emptyenv(@Current Environment rho) {
    return rho.getBaseEnvironment().getParent();
  }

  @Builtin
  public static Environment globalenv(@Current Context context) {
    return context.getGlobalEnvironment();
  }

  @Internal
  @DataParallel
  public static boolean exists(@Current Context context, @Recycle String x,
      Environment environment, String mode, boolean inherits) {
    return environment.findVariable(context, Symbol.get(x), Types.modePredicate(mode),
        inherits) != Symbol.UNBOUND_VALUE;
  }

  @Internal
  public static SEXP get(@Current Context context, String x,
      Environment environment, String mode, boolean inherits) {
    SEXP value = environment.findVariable(context, Symbol.get(x), Types.modePredicate(mode),
        inherits);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Object '%s' not found", x);
    }
    return value;
  }

  @Generic
  @Builtin
  public static int length(SEXP exp) {
    return exp.length();
  }

  @Internal
  public static SEXP vector(String mode, int length) {
    if ("logical".equals(mode)) {
      return new LogicalArrayVector(new int[length]);

    } else if ("integer".equals(mode)) {
      return new IntArrayVector(new int[length]);

    } else if ("numeric".equals(mode) || "double".equals(mode)) {
      return new ConstantDoubleVector(0, length);

    } else if ("complex".equals(mode)) {
      throw new UnsupportedOperationException("implement me!");

    } else if ("character".equals(mode)) {
      String values[] = new String[length];
      Arrays.fill(values, "");
      return new StringArrayVector(values);

    } else if ("list".equals(mode)) {
      SEXP values[] = new SEXP[length];
      Arrays.fill(values, Null.INSTANCE);
      return new ListVector(values);

    } else if ("pairlist".equals(mode)) {
      SEXP values[] = new SEXP[length];
      Arrays.fill(values, Null.INSTANCE);
      return PairList.Node.fromArray(values);

    } else if ("raw".equals(mode)) {
      byte values[] = new byte[length];
      return new RawVector(values);
    } else {
      throw new EvalException(String.format(
          "vector: cannot make a vector of mode '%s'.", mode));
    }
  }

  @Builtin("storage.mode<-")
  public static SEXP setStorageMode(Vector source, String newMode) {
    Vector.Builder builder;
    if (newMode.equals("logical")) {
      builder = new LogicalArrayVector.Builder();
    } else if (newMode.equals("double")) {
      builder = new DoubleArrayVector.Builder();
    } else if (newMode.equals("integer")) {
      builder = new IntArrayVector.Builder();
    } else if (newMode.equals("character")) {
      builder = new StringVector.Builder();
    } else {
      throw new UnsupportedOperationException("storage.mode with new mode '"
          + newMode + "' invalid or not implemented");
    }

    for (int i = 0; i != source.length(); ++i) {
      builder.setFrom(i, source, i);
    }
    return builder.copyAttributesFrom(source).build();
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
  public static StringVector search(@Current Context context) {
    List<String> names = Lists.newArrayList();
    Environment env = context.getGlobalEnvironment();
    while (env != Environment.EMPTY) {
      if(context.getNamespaceRegistry().isNamespaceEnv(env)) {
        names.add("namespace:" + env.getName());
      } else {
        names.add(env.getName());
      }
      env = env.getParent();
    }
    // special cased:
    names.set(0, ".GlobalEnv");
    names.set(names.size() - 1, "package:base");

    return new StringArrayVector(names);
  }

  @Invisible
  @Internal
  public static Environment attach(@Current Context context, SEXP what,
      int pos, String name) {

    // By default the database is attached in position 2 in the search path,
    // immediately after the user's workspace and before all previously loaded
    // packages and
    // previously attached databases. This can be altered to attach later in the
    // search
    // path with the pos option, but you cannot attach at pos=1.

    if (pos < 2) {
      throw new EvalException("Attachment position must be 2 or greater");
    }

    Environment child = context.getGlobalEnvironment();
    for (int i = 2; i != pos; ++i) {
      child = child.getParent();
    }

    Environment newEnv = Environment.createChildEnvironment(child.getParent());
    child.setParent(newEnv);

    newEnv.setAttribute(Symbols.NAME.getPrintName(), StringVector.valueOf(name));

    // copy all values from the provided environment, list  into the
    // new environment
    if (what instanceof HasNamedValues) {
      for(NamedValue namedValue : ((HasNamedValues)what).namedValues()) {
        if(!namedValue.hasName()) {
          throw new UnsupportedOperationException("all elements of a list must be named");
        }
        newEnv.setVariable(namedValue.getName(), namedValue.getValue());
      }
    } else {
      throw new EvalException("object of type '%s' cannot be attached", what.getTypeName());
    }
    return newEnv;
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
  public static ListVector options(@Current Context context,
      @ArgumentList ListVector arguments) {
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
    }
    return results.build();
  }
  
  @Builtin("length<-")
  public static Vector setLength(Vector source, int length) {

    Builder copy = source.newBuilderWithInitialSize(length);
    for(int i=0;i!=Math.min(length, source.length());++i) {
      copy.setFrom(i, source, i);
    }
    AttributeMap attribs = source.getAttributes();
    if(attribs.hasNames()) {
      copy.setAttribute(Symbols.NAMES, setLength(attribs.getNames(), length));
    }
    return copy.build();
  }

  public static Predicate<SEXP> modePredicate(String mode) {
    if(mode.equals("any")) {
      return Predicates.alwaysTrue();
    } else if(mode.equals("function")){
      return CollectionUtils.IS_FUNCTION;
    } else {
      throw new EvalException(" mode '%s' as a predicate is implemented.", mode);
    }
  }
}
