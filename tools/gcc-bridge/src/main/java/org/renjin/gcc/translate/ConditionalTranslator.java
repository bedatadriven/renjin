package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.GimpleConditional;
import org.renjin.gcc.gimple.GimpleLabel;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleGoto;

import java.util.List;

public class ConditionalTranslator {
  private FunctionContext context;

  public ConditionalTranslator(FunctionContext context) {
    this.context = context;
  }

  public void translate(GimpleConditional conditional) {
    context.getBuilder().addStatement("if " + translateCondition(conditional.getOperator(), conditional.getOperands()) +
            " goto " + label(conditional.getTrueTarget()));
    context.getBuilder().addStatement(new JimpleGoto(label(conditional.getFalseTarget())));
  }

  private String label(GimpleLabel target) {
    return target.getName();
  }

  public JimpleExpr translateCondition(GimpleOp operator, List<GimpleExpr> operands) {
    switch (operator) {
      case NE_EXPR:
        return relational("!=", operands);
      case EQ_EXPR:
        return relational("==", operands);
      case LE_EXPR:
        return relational("<=", operands);
      case LT_EXPR:
        return relational("<", operands);
      case GT_EXPR:
        return relational(">", operands);
      case GE_EXPR:
        return relational(">=", operands);
    }
    throw new UnsupportedOperationException(operator.name() + operands.toString());
  }

  private JimpleExpr relational(String operator, List<GimpleExpr> operands) {
    return JimpleExpr.binaryInfix(operator,
            context.asNumericExpr(operands.get(0)),
            context.asNumericExpr(operands.get(1)));

  }
}
