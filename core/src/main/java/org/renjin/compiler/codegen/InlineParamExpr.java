package org.renjin.compiler.codegen;

import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.commons.InstructionAdapter;


public class InlineParamExpr {

  private EmitContext context;
  private Expression expression;

  public InlineParamExpr(EmitContext context, Expression expression) {
    this.context = context;
    this.expression = expression;
  }
  
  public void load(InstructionAdapter mv) {
    expression.load(context, mv);
  }
}
