package org.renjin.gcc.shimple;


import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.expr.GimpleVar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ShimpleInsVisitor extends GimpleVisitor {

  private MethodTable methodTable;
  public ShimpleWriter writer;

  public ShimpleInsVisitor(MethodTable methodTable, ShimpleWriter writer) {
    this.methodTable = methodTable;
    this.writer = writer;
  }

  @Override
  public void blockStart(GimpleBasicBlock bb) {
    this.writer.println("label" + bb.getNumber() + ":");
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    writer.println(Shimple.id(assignment.getRHS()) + " = " +
            translateExpr(assignment.getOperator(), assignment.getOperands()) + ";");
  }

  @Override
  public void visitReturn(GimpleReturn gimpleReturn) {
    writer.println("return " + translateExpr(gimpleReturn.getValue()) + ";");
  }

  @Override
  public void visitGoto(Goto gotoIns) {
    writer.println("goto " + label(gotoIns.getTarget()) + ";");
  }

  private String label(GimpleLabel target) {
    return "label" + target.getBasicBlockNumber();
  }

  @Override
  public void visitCall(GimpleCall gimpleCall) {
    Method method = methodTable.resolve(gimpleCall);
  }

  @Override
  public void visitConditional(GimpleConditional conditional) {
    writer.println("if " + translateExpr(conditional.getOperator(), conditional.getOperands()) +
            " goto " + label(conditional.getTrueTarget()) + ";");
    writer.println("goto " + label(conditional.getFalseTarget()) + ";");
  }

  private String translateExpr(GimpleOp operator, List<GimpleExpr> operands) {
    switch (operator) {
      case MULT_EXPR:
        return binaryInfix("*", operands);
      case PLUS_EXPR:
        return binaryInfix("+", operands);
      case MINUS_EXPR:
        return binaryInfix("-", operands);
      case NE_EXPR:
        return binaryInfix("!=", operands);
      case EQ_EXPR:
        return binaryInfix("==", operands);
      case RDIV_EXPR:
        return binaryInfix("/", operands);
      case LE_EXPR:
        return binaryInfix("<=", operands);
      case LT_EXPR:
        return binaryInfix("<", operands);
      case GT_EXPR:
        return binaryInfix(">", operands);
      case GE_EXPR:
        return binaryInfix(">=", operands);
      case TRUTH_NOT_EXPR:
        return unaryPrefix("!" , operands);
      case REAL_CST:
        return castDouble(operands);
      case INTEGER_CST:
        return castExpr("int", operands);
      case ABS_EXPR:
        return absValue(operands);
      case POINTER_PLUS_EXPR:
        return pointerPlus(operands);
      case INDIRECT_REF:
        return indirectRef(operands);
      case FLOAT_EXPR:
      case VAR_DECL:
      case NOP_EXPR:
      case SSA_NAME:
        return translateExpr(operands.get(0));
    }
    throw new UnsupportedOperationException(operator.name() + operands.toString());
  }

  private String absValue(List<GimpleExpr> operands) {
    return "staticinvoke <java.lang.Math: double abs(double)>(" + translateExpr(operands.get(0)) + ")";
  }


  private String binaryInfix(String operatorToken, List<GimpleExpr> operands) {
    return translateExpr(operands.get(0)) + " " + operatorToken + " " + translateExpr(operands.get(1));
  }

  private String unaryPrefix(String operator, List<GimpleExpr> operands) {
    return operator + translateExpr(operands.get(0));
  }


  private String castDouble(List<GimpleExpr> operands) {
    if(operands.get(0) instanceof GimpleConstant) {
      GimpleConstant constant = (GimpleConstant) operands.get(0);
      return Double.toString(((Number) constant.getValue()).doubleValue());
    } else {
      return castExpr("double", operands);
    }
  }

  private String castExpr(String type, List<GimpleExpr> operands) {
    return "(" + type + ")" + translateExpr(operands.get(0));
  }

  private String translateExpr(GimpleExpr expr) {
    if(expr instanceof GimpleVar) {
      return Shimple.id((GimpleVar) expr);
    } else if(expr instanceof GimpleConstant) {
      return Shimple.constant(((GimpleConstant) expr).getValue());
    } else if(expr instanceof GimpleExternal) {
      return resolveExternal((GimpleExternal) expr);
    } else {
      throw new UnsupportedOperationException(expr.toString());
    }
  }

  private String resolveExternal(GimpleExternal external) {
    Field field = methodTable.findField(external);
    return "<" + field.getDeclaringClass().getName() + ": " + Shimple.type(field.getType()) + " " + external.getName() + ">";
  }

  private String pointerPlus(List<GimpleExpr> operands) {
    GimpleVar pointer = (GimpleVar) operands.get(0);
    GimpleExpr increment = operands.get(1);
    return "virtualinvoke " + Shimple.id(pointer) +
            ".<org.renjin.gcc.runtime.Pointer: org.renjin.gcc.runtime.Pointer plus(int)>(" +
            translateExpr(increment) + ")";
  }

  private String indirectRef(List<GimpleExpr> operands) {
      GimpleVar pointer = (GimpleVar) operands.get(0);
    return "virtualinvoke " + Shimple.id(pointer) + ".<org.renjin.gcc.runtime.Pointer: double asDouble()>()";
  }
}
