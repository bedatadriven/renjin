package org.renjin.primitives;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.invoke.reflection.converters.*;
import org.renjin.primitives.vector.ConstantDoubleVector;
import org.renjin.primitives.vector.ConvertingDoubleVector;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

import java.util.Arrays;

/**
 * Functions which operate on Vectors
 */
public class Vectors {

  @Builtin("length<-")
  public static Vector setLength(Vector source, int length) {

    Vector.Builder copy = source.newBuilderWithInitialSize(length);
    for(int i=0;i!=Math.min(length, source.length());++i) {
      copy.setFrom(i, source, i);
    }
    AttributeMap attribs = source.getAttributes();
    if(attribs.hasNames()) {
      copy.setAttribute(Symbols.NAMES, setLength(attribs.getNames(), length));
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
      throw new EvalException(" mode '%s' as a predicate is implemented.", mode);
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
