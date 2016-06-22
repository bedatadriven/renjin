package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;

import javax.annotation.Nonnull;

/**
 * Loads a string constant as an array of bytes
 */
public class StringConstant implements JExpr {
  
  private String constant;

  public StringConstant(String constant) {
    this.constant = constant;
  }

  public int getLength() {
    return constant.length();
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
