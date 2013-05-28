package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.ins.GimpleConditional;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleGoto;

public class ConditionalTranslator {
  private FunctionContext context;

  public ConditionalTranslator(FunctionContext context) {
    this.context = context;
  }

  public void translate(GimpleConditional conditional) {
    Comparison comparison = new Comparison(conditional.getOperator(),
        context.resolveExpr(conditional.getOperands().get(0)),
        context.resolveExpr(conditional.getOperands().get(1)));
        
    JimpleExpr condition = comparison.toCondition(context);
    
    context.getBuilder().addStatement(
        "if " + condition + " goto "
            + label(conditional.getTrueLabel()));
    context.getBuilder().addStatement(new JimpleGoto(label(conditional.getFalseLabel())));
  }

  private String label(int label) {
    return "BB" + label;
  }

}
