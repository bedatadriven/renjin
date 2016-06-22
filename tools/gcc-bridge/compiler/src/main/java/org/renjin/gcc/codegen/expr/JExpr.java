package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;

import javax.annotation.Nonnull;

/**
 * Generator for "simple" expressions that are represented by a single JVM value or reference.
 */
public interface JExpr {

  /**
   * 
   * @return the type of this expression, when pushed onto the stack
   */
  @Nonnull
  Type getType();

  /**
   * Loads this expression onto the stack.
   * 
   */
  void load(@Nonnull MethodGenerator mv);
}
