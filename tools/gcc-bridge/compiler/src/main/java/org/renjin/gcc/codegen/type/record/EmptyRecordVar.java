package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;

import javax.annotation.Nonnull;

/**
 * Records with no fields have no values, so we don't really need to do
 * anything.
 */
public class EmptyRecordVar implements Addressable, LValue, SimpleExpr {


  @Nonnull
  @Override
  public Type getType() {
    return Type.getType(Object.class);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    mv.aconst(null);
  }
  
  @Override
  public Expr addressOf() {
    return Expressions.nullRef(Type.getType(Object.class));
  }

  @Override
  public void store(MethodGenerator mv, Expr rhs) {
    // NOOP
  }

}
