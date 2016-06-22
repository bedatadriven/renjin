package org.renjin.gnur.api;

import com.google.common.base.Charsets;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.sexp.AbstractSEXP;
import org.renjin.sexp.SexpVisitor;

/**
 * Internal character SEXP
 */
public class GnuCharSexp extends AbstractSEXP {
  
  private byte[] value;

  public GnuCharSexp(byte[] value) {
    this.value = value;
  }

  public GnuCharSexp(String value) {
    this(BytePtr.nullTerminatedString(value, Charsets.UTF_8).array);
  }

  @Override
  public String getTypeName() {
    return "char";
  }

  @Override
  public void accept(SexpVisitor visitor) {
  }

  public BytePtr getValue() {
    return new BytePtr(value, 0);
  }
}
