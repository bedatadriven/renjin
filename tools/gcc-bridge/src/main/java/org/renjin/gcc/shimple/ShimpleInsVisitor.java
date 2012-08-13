package org.renjin.gcc.shimple;


import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;

import java.util.List;

public class ShimpleInsVisitor extends GimpleVisitor {

  public ShimpleWriter writer;

  public ShimpleInsVisitor(ShimpleWriter writer) {
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
  public void visitConditional(GimpleConditional conditional) {
    writer.println("if " + translateExpr(conditional.getOperator(), conditional.getOperands()) +
            " goto " + label(conditional.getTrueTarget()) + ";");
    writer.println("goto " + label(conditional.getFalseTarget()) + ";");
  }

  private String translateExpr(GimpleOp operator, List<GimpleExpr> operands) {
    switch(operator) {
    case MULT_EXPR:
      return binaryInfix("*", operands);
    case PLUS_EXPR:
      return binaryInfix("+", operands);
    case NE_EXPR:
      return binaryInfix("!=", operands);
    case REAL_CST:
      return castDouble(operands);
    case INTEGER_CST:
      return castExpr("int", operands);
    case NOP_EXPR:
      return translateExpr(operands.get(0));
    case POINTER_PLUS_EXPR:
      return pointerPlus(operands);
    case INDIRECT_REF:
      return indirectRef(operands);
    case SSA_NAME:
      return translateExpr(operands.get(0));
    }
    throw new UnsupportedOperationException(operator.name() + operands.toString());
  }


  private String binaryInfix(String operatorToken, List<GimpleExpr> operands) {
    return translateExpr(operands.get(0)) + " " + operatorToken + " " + translateExpr(operands.get(1));
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
    } else {
      throw new UnsupportedOperationException(expr.toString());
    }
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
