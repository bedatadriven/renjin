package org.renjin.compiler.ir.tac.expressions;

import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tree.TreeNode;
import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


public interface Expression extends TreeNode {


  /**
   *
   * @return true if we are absolutely certain this expression has no side effects
   */
  boolean isDefinitelyPure();

  /**
   * Emits the JVM byte code to push the value of this expression on the stack
   *
   * @param emitContext
   * @param mv
   */
  void emitPush(EmitContext emitContext, MethodVisitor mv);

  Class inferType();
}
