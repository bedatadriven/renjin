package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;

import javax.annotation.Nonnull;

/**
 * Loads a string constant as an array of bytes
 */
public class StringConstant implements Value {
  
  private String constant;

  public StringConstant(String constant) {
    this.constant = constant;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.getType("[B");
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    // TODO: character set
    // What even makes sense here?
    mv.aconst(constant);
    mv.invokevirtual(String.class, "getBytes", getType());
  }
}
