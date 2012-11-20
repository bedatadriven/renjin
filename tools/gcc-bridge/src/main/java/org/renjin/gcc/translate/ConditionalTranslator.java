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
    JimpleType type = inferType(operands.get(0), operands.get(1));
    if(type.equals(JimpleType.DOUBLE) || type.equals(JimpleType.FLOAT)) {
      switch (operator) {
        case NE_EXPR:
          return floatComparison("cmpl", "!= 0", operands, type);
        case EQ_EXPR:
          return floatComparison("cmpl", "== 0", operands, type);
        case LE_EXPR:
          return floatComparison("cmpg", "<= 0", operands, type) ;
        case LT_EXPR:
          return floatComparison("cmpg", "< 0", operands, type) ;
        case GT_EXPR:
          return floatComparison("cmpl", "> 0", operands, type) ;
        case GE_EXPR:
          return floatComparison("cmpl", ">= 0", operands, type) ;
      }
    } else {
      switch (operator) {
        case NE_EXPR:
          return intComparison("!=", operands);
        case EQ_EXPR:
          return intComparison("==", operands);
        case LE_EXPR:
          return intComparison("<=", operands);
        case LT_EXPR:
          return intComparison("<", operands);
        case GT_EXPR:
          return intComparison(">", operands);
        case GE_EXPR:
          return intComparison(">=", operands);
      }
    }
    throw new UnsupportedOperationException(operator.name() + operands.toString());
  }

  private JimpleType findType(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVar) {
      Variable var = context.lookupVar(gimpleExpr);
      return var.getNumericType();
    } else {
      return null; // we treat constants as untyped 
    }
  }
  
  private JimpleType inferType(GimpleExpr e1, GimpleExpr e2) {
    JimpleType type = findType(e1);
    if(type != null) {
      return type;
    }
    type = findType(e2);
    if(type == null) {
      throw new UnsupportedOperationException("Could not deduce types from " + e1 + " or " + e2);
    }
    return type;
  }

  private JimpleExpr floatComparison(String operator, String condition, List<GimpleExpr> operands, JimpleType type) {

    String cmp = context.declareTemp(JimpleType.INT);
    context.getBuilder().addStatement(cmp + " = " +
             context.asNumericExpr(operands.get(0), type) + " " + operator + " " +
             context.asNumericExpr(operands.get(1), type));

    return new JimpleExpr(cmp + " " + condition);
  }

  private JimpleExpr intComparison(String operator, List<GimpleExpr> operands) {
    return JimpleExpr.binaryInfix(operator,
            context.asNumericExpr(operands.get(0), JimpleType.INT),
            context.asNumericExpr(operands.get(1), JimpleType.INT));

  }
}
