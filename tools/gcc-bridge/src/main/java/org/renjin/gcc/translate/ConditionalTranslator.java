package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.GimpleConditional;
import org.renjin.gcc.gimple.GimpleLabel;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleGoto;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.var.Variable;

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
    if(isReal(operands.get(0)) || isReal(operands.get(1))) {
      switch (operator) {
        case NE_EXPR:
          return floatComparison("cmpl", "!= 0", operands);
        case EQ_EXPR:
          return floatComparison("cmpl", "== 0", operands);
        case LE_EXPR:
          return floatComparison("cmpg", "<= 0", operands) ;
        case LT_EXPR:
          return floatComparison("cmpg", "< 0", operands) ;
        case GT_EXPR:
          return floatComparison("cmpl", "> 0", operands) ;
        case GE_EXPR:
          return floatComparison("cmpl", ">= 0", operands) ;
      }
    } else {
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
    }
    throw new UnsupportedOperationException(operator.name() + operands.toString());
  }

  private boolean isReal(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVar) {
      Variable var = context.lookupVar(gimpleExpr);
      return var.isReal();
    } else if(gimpleExpr instanceof GimpleConstant) {
      return ((GimpleConstant) gimpleExpr).getValue() instanceof Double;
    } else {
      throw new UnsupportedOperationException(gimpleExpr.toString());
    }
  }

  private JimpleExpr floatComparison(String operator, String condition, List<GimpleExpr> operands) {

    String cmp = context.declareTemp(JimpleType.INT);
    context.getBuilder().addStatement(cmp + " = " +
             context.asNumericExpr(operands.get(0)) + " " + operator + " " +
             context.asNumericExpr(operands.get(1)));

    return new JimpleExpr(cmp + " " + condition);
  }

  private JimpleExpr relational(String operator, List<GimpleExpr> operands) {
    return JimpleExpr.binaryInfix(operator,
            context.asNumericExpr(operands.get(0)),
            context.asNumericExpr(operands.get(1)));

  }
}
