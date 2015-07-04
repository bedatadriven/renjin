package org.renjin.primitives;

import com.google.common.base.Charsets;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.RawVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

/**
 * Functions that operate on raw vectors.
 */
public final class Raw {
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
}
