package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;

import javax.annotation.Nonnull;


public interface Value extends ExprGenerator {

  @Nonnull
  Type getType();

  void load(@Nonnull MethodGenerator mv);
}
