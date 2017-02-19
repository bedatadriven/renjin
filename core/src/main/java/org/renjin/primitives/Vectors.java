/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.invoke.reflection.converters.*;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.primitives.vector.ConvertingDoubleVector;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.repackaged.guava.base.Predicates;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

import java.util.Arrays;

/**
 * Functions which operate on Vectors
 */
public class Vectors {

  @Builtin("length<-")
  public static Vector setLength(Vector source, int length) {
    
    if(length < 0) {
      throw new EvalException("%d : invalid value", length);
    }
    
    // Strange but true... 
    // if source is null, then length(source) <- x is null for all x >= 0
    if(source == Null.INSTANCE) {
      return Null.INSTANCE;
    }

    Vector.Builder copy = source.newBuilderWithInitialSize(length);
    for(int i=0;i!=Math.min(length, source.length());++i) {
      copy.setFrom(i, source, i);
    }
    AtomicVector sourceNames = source.getNames();
    if(sourceNames != Null.INSTANCE) {
      StringVector.Builder newNames = new StringVector.Builder();
      for (int i = 0; i < length; i++) {
        if(i < source.length()) {
          newNames.add(sourceNames.getElementAsString(i));
        } else {
          newNames.add("");
        }
      }
      copy.setAttribute(Symbols.NAMES, newNames.build());
    }
    return copy.build();
  }

  @Generic
  @Builtin
  public static int length(SEXP exp) {
    return exp.length();
  }

  @Generic
  @Builtin("as.character")
  public static StringVector asCharacter(PairList.Node source) {
    return (StringVector) convertToStringVector(null, new StringVector.Builder(), source.toVector());
  }
  
  @Generic
  @Builtin("as.character")
  public static StringVector asCharacter() {
    return StringArrayVector.EMPTY;
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
  @Builtin("as.logical")
  public static LogicalVector asLogical(Vector vector) {
    checkForListThatCannotBeCoercedToAtomicVector(vector, "logical");
    return (LogicalVector) convertToAtomicVector(new LogicalArrayVector.Builder(), vector);
  }

  @Generic
  @Builtin("as.logical")
  public static LogicalVector asLogical() {
    return LogicalArrayVector.EMPTY;
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
    checkForListThatCannotBeCoercedToAtomicVector(source, "integer");

    return (IntVector) convertToAtomicVector(new IntArrayVector.Builder(), source);
  }

  @Generic
  @Builtin("as.integer")
  public static IntVector asInteger() {
    return IntArrayVector.EMPTY;
  }


  @Generic
  @Builtin("as.double")
  public static DoubleVector asDouble(ExternalPtr ptr) {
    Object instance = ptr.getInstance();
    Class clazz = instance.getClass();
    if (DoubleConverter.accept(clazz)) {
      return (DoubleVector) DoubleConverter.INSTANCE.convertToR(instance);
      
    } else if (DoubleArrayConverter.accept(clazz)) {
      return (DoubleVector)new DoubleArrayConverter(clazz).convertToR(instance);
   
    } else {
      return new DoubleArrayVector(DoubleVector.NA);
    }
  }

  @Generic
  @Builtin("as.double")
  public static DoubleVector asDouble(Vector source) {
    checkForListThatCannotBeCoercedToAtomicVector(source, "double");
    
    if(source instanceof DoubleVector) {
      return (DoubleVector) source.setAttributes(AttributeMap.EMPTY);
   
    } else if(source.isDeferred() || source.length() > 100) {
      return new ConvertingDoubleVector(source);
   
    } else {
      return (DoubleVector) convertToAtomicVector(new DoubleArrayVector.Builder(), source);
    }
  }

  @Generic
  @Builtin("as.double")
  public static DoubleVector asDouble() {
    return DoubleArrayVector.EMPTY;
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
    
    checkForListThatCannotBeCoercedToAtomicVector(source, "raw");
    
    return (RawVector) Vectors.convertToAtomicVector(new RawVector.Builder(), source);
  }

  static Vector convertToAtomicVector(Vector.Builder builder, Vector source) {
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
  public static ComplexVector asComplex(Vector vector) {
    
    if(vector instanceof DoubleVector) {
      return doubleToComplex((DoubleVector) vector);
    }
    checkForListThatCannotBeCoercedToAtomicVector(vector, "");

    return (ComplexVector) convertToAtomicVector(new ComplexArrayVector.Builder(), vector);
  }

  @Generic
  @Builtin("as.complex")
  public static ComplexVector asComplex() {
    return ComplexArrayVector.EMPTY;
  }

  @Generic
  @Internal("as.vector")
  public static SEXP asVector(Vector x, String mode) {

    // Annoyingly, this function behaves a bit erraticaly
    // When "mode" 
    
    if(mode.equals("any")) {
      //  if the result is atomic all attributes are removed.
      if(x instanceof AtomicVector) {
        return x.setAttributes(AttributeMap.EMPTY);
      } else {
        return x;
      }
    }

    Vector.Builder result;
    if ("character".equals(mode)) {
      result = new StringVector.Builder();
      
    } else if ("logical".equals(mode)) {
      result = new LogicalArrayVector.Builder(x.length());
      checkForListThatCannotBeCoercedToAtomicVector(x, mode);

    } else if ("integer".equals(mode)) {
      result = new IntArrayVector.Builder(x.length());
      checkForListThatCannotBeCoercedToAtomicVector(x, mode);

    } else if ("raw".equals(mode)) {
      result = new RawVector.Builder();
      checkForListThatCannotBeCoercedToAtomicVector(x, mode);

    } else if ("numeric".equals(mode) || "double".equals(mode)) {
      result = new DoubleArrayVector.Builder(x.length());
      checkForListThatCannotBeCoercedToAtomicVector(x, mode);

    } else if ("complex".equals(mode)) {
      
      // Special case: double -> complex treats NaNs as NA
      if(x instanceof DoubleVector) {
        return doubleToComplex((DoubleVector) x);
      }
      
      result = new ComplexArrayVector.Builder(x.length());
      checkForListThatCannotBeCoercedToAtomicVector(x, mode);
      
    } else if ("list".equals(mode)) {
      // Special case: preserve names with mode = 'list'
      result = new ListVector.Builder();
      result.setAttribute(Symbols.NAMES, x.getNames());

    } else if("expression".equals(mode)) {
      result = new ExpressionVector.Builder();
      
      // Special case
      if(x == Null.INSTANCE) {
        return new ExpressionVector(Null.INSTANCE);
      
      } else if(x instanceof ListVector) {
        // Exception, for list -> expression, copy attributes
        return new ExpressionVector(((ListVector) x).toArrayUnsafe(), x.getAttributes());
      }
      
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
      if (x instanceof ListVector) {
        throw new EvalException("vector of type 'list' cannot be coerced to symbol");
      }
      return Symbol.get(x.getElementAsString(0));

    } else {
      throw new EvalException("invalid 'mode' argument: " + mode);
    }

    for (int i = 0; i != x.length(); ++i) {
      result.setFrom(i, x, i);
    }
    return result.build();
  }

  /**
   * Special handling for double -> complex
   */
  private static ComplexVector doubleToComplex(DoubleVector x) {
    ComplexArrayVector.Builder result = new ComplexArrayVector.Builder(x.length());
    // in this context, NaNs are treated exceptionally as NAs
    for (int i = 0; i < x.length(); i++) {
      result.set(i, x.getElementAsDouble(i), 0);
    }
    return result.build();
  }

  /**
   * Checks that {@code x} is not a list, or if it is a list, is soley consists of 
   * atomic vectors of length 1
   * @param x
   * @param mode
   */
  private static void checkForListThatCannotBeCoercedToAtomicVector(Vector x, String mode) {
    if(x instanceof ListVector) {
      ListVector list = (ListVector) x;
      for (int i = 0; i < list.length(); i++) {
        SEXP element = list.getElementAsSEXP(i);
        if(element == Null.INSTANCE || element.length() > 1 || !(element instanceof Vector)) {
          throw new EvalException("(list) object cannot be coerced to type '%s'", mode);
        }
      }
    }
  }

  private static PairList asPairList(Vector x) {
    PairList.Builder builder = new PairList.Builder();
    for (int i = 0; i != x.length(); ++i) {
      builder.add(x.getName(i), x.getElementAsSEXP(i));
    }
    return builder.build();
  }

  public static Predicate<SEXP> modePredicate(String mode) {
    if(mode.equals("any")) {
      return Predicates.alwaysTrue();
    } else if(mode.equals("function")){
      return CollectionUtils.IS_FUNCTION;
    } else {
      throw new EvalException(" mode '%s' as a predicate is not implemented.", mode);
    }
  }

  @Internal
  public static SEXP vector(String mode, int length) {
    if ("logical".equals(mode)) {
      return new LogicalArrayVector(new int[length]);

    } else if ("integer".equals(mode)) {
      return new IntArrayVector(new int[length]);

    } else if ("numeric".equals(mode) || "double".equals(mode)) {
      return RepDoubleVector.createConstantVector(0, length);

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

  @Internal
  public static ComplexVector complex(int lengthOut, AtomicVector realVector, AtomicVector imaginaryVector) {
    if(realVector.length() > lengthOut) {
      lengthOut = realVector.length();
    }
    if(imaginaryVector.length() > lengthOut) {
      lengthOut = imaginaryVector.length();
    }

    ComplexArrayVector.Builder result = new ComplexArrayVector.Builder(0, lengthOut);
    for(int i=0; i!=lengthOut;++i) {
      double real = 0;
      double imaginary = 0;
      if(realVector.length() > 0) {
        real = realVector.getElementAsDouble(i % realVector.length());
      }
      if(imaginaryVector.length() > 0) {
        imaginary = imaginaryVector.getElementAsDouble(i % imaginaryVector.length());
      }
      result.add(ComplexVector.complex(real, imaginary));
    }
    return result.build();
  }
  
  @Builtin
  public static ListVector list(@ArgumentList ListVector arguments) {
    return arguments;
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
      
      AttributeMap.Builder newAttributes = x.getAttributes().copy();

      if(newDim.length() == 0 ||
          (newDim.length() == 1 && dim.length() > 1)) {
        
        newAttributes.remove(Symbols.DIM);
        newAttributes.remove(Symbols.DIMNAMES);

      } else {
        newAttributes.setDim(newDim.build());
        newAttributes.setDimNames(newDimnames.build());
      }
      
      return x.setAttributes(newAttributes);
    }
  }
  
  public static AtomicVector toType(AtomicVector x, Vector.Type type) {
    if(x.getVectorType() == type) {
      return x;
    } else if(type == DoubleVector.VECTOR_TYPE) {
      return asDouble(x);
    } else if(type == IntVector.VECTOR_TYPE) {
      return asInteger(x);
    } else if(type == LogicalVector.VECTOR_TYPE) {
      return asLogical(x);
    } else if(type == ComplexVector.VECTOR_TYPE) {
      return asComplex(x);
    } else if(type == StringVector.VECTOR_TYPE) {
      return new ConvertingStringVector(x);
    } else if(type == RawVector.VECTOR_TYPE) {
      return asRaw(x);
    } else {
      throw new IllegalArgumentException("type: " + type);
    }
  }
  
  @Generic
  @Internal("as.vector")
  public static SEXP asVector(Symbol x, String mode) {
    if ("character".equals(mode)) {
      return StringVector.valueOf(x.getPrintName());
      
    } else if ("list".equals(mode)) {
      return new ListVector(x);
      
    } else if ("expression".equals(mode)) {
      return new ExpressionVector(x);

    } else if ("symbol".equals(mode)) {
      return x;
      
    } else {
      throw new EvalException("'%s' cannot be coerced to vector of type '%s'", x.getTypeName(), mode);
    }
  }

  @Generic
  @Internal("as.vector")
  public static SEXP asVector(@Current Context context, PairList x, String mode) {
    
    // Exceptionally, as.vector(x, 'pairlist') 
    // preserves *all* attributes
    if("pairlist".equals(mode) || "any".equals(mode)) {
      return x;
    }
    
    Vector.Builder result;
    NamesBuilder names = NamesBuilder.withInitialCapacity(x.length());
    if ("character".equals(mode)) {
      result = new StringVector.Builder(0, x.length());
    } else if ("logical".equals(mode)) {
      result = new LogicalArrayVector.Builder(0, x.length());
    } else if ("numeric".equals(mode)) {
      result = new DoubleArrayVector.Builder(0, x.length());
    } else if ("list".equals(mode)) {
      result = new ListVector.Builder();
    } else if ("expression".equals(mode)) {
      result = new ExpressionVector.Builder();
      
      // Special case...
      if(x instanceof FunctionCall) {
        result.add(x);
        return result.build();
      }
      
    } else if ("raw".equals(mode)) {
      result = new RawVector.Builder();
    } else {
      throw new EvalException("invalid 'mode' argument");
    }

    for (PairList.Node node : x.nodes()) {
      if (node.hasTag()) {
        names.add(node.getTag().getPrintName());
      } else {
        names.addBlank();
      }
      if(node.getValue() instanceof AtomicVector) {
        result.add(node.getValue());
      } else {
        if(result instanceof ListVector.Builder) {
          result.add(node.getValue());
        } else if(result instanceof StringArrayVector.Builder) {
          ((StringVector.Builder) result).add(Deparse.deparseExp(context, node.getValue()));
        } else {
          throw new EvalException("'%s' cannot be coerced to type '%s'", x.getTypeName(), mode);
        }
      }
    }
    result.setAttribute(Symbols.NAMES.getPrintName(), names.build(result.length()));
    return result.build();
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


}
