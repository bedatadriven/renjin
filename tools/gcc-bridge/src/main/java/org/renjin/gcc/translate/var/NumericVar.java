package org.renjin.gcc.translate.var;


import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleCompoundRef;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.ConditionalTranslator;
import org.renjin.gcc.translate.FunctionContext;

import java.util.List;

public class NumericVar extends Variable {

  private final FunctionContext context;
  private final String jimpleName;
  private final PrimitiveType gimpleType;
  private final JimpleType jimpleType;

  public NumericVar(FunctionContext context, String gimpleName, PrimitiveType gimpleType) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
    this.gimpleType = gimpleType;
    switch (gimpleType) {
      case DOUBLE_TYPE:
        jimpleType = JimpleType.DOUBLE;
        break;
      case INT_TYPE:
        jimpleType = JimpleType.INT;
        break;
      case BOOLEAN:
        jimpleType = JimpleType.BOOLEAN;
        break;
      case LONG:
        jimpleType = JimpleType.LONG;
        break;
      default:
        throw new IllegalArgumentException(gimpleType.name());
    }
    context.getBuilder().addVarDecl(jimpleType, jimpleName);
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    switch (op) {
      case VAR_DECL:
      case SSA_NAME:
      case NOP_EXPR:
        assignNop(operands.get(0));
        break;

      case FLOAT_EXPR:
        integerToReal(operands.get(0));
        break;

      case FIX_TRUNC_EXPR:
        realToInteger(operands.get(0));
        break;

      case MULT_EXPR:
        assignBinary("*", operands);
        break;

      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
        assignBinary("/", operands);
        break;
      case ABS_EXPR:
        assignAbs(operands.get(0));
        break;

      case MAX_EXPR:
        assignMax(operands);
        break;

      case REAL_CST:
        assignConstant(operands.get(0));
        break;
      case INTEGER_CST:
        assignConstant(operands.get(0));
        break;
      case PLUS_EXPR:
        assignBinary("+", operands);
        break;
      case MINUS_EXPR:
        assignBinary("-", operands);
        break;

      case BIT_NOT_EXPR:
        assignBitNot(operands.get(0));
        break;
      case EQ_EXPR:
      case NE_EXPR:
      case LE_EXPR:
      case LT_EXPR:
      case GT_EXPR:
      case GE_EXPR:
        assignBoolean(op, operands);
        break;

      case TRUTH_NOT_EXPR:
        assignNegation(operands.get(0));
        break;

      case INDIRECT_REF:
        assignRef(operands);
        break;

      case COMPONENT_REF:
        assignCompoundRef((GimpleCompoundRef) operands.get(0));
        break;

      default:
        throw new UnsupportedOperationException("Unexpected operator in assignment to numeric variable: " + op + " " + operands);
    }
  }


  private void integerToReal(GimpleExpr gimpleExpr) {
    // Soot will complain if we convert a real to a real
    // so we need to double check that the source is really an integer
    if(gimpleExpr instanceof GimpleVar) {
      Variable var = context.lookupVar((GimpleVar) gimpleExpr);
      if(var instanceof NumericVar && ((NumericVar) var).isReal()) {
        assignNop(gimpleExpr);
        return;
      }
    } else if(gimpleExpr instanceof GimpleConstant) {
      if(((GimpleConstant) gimpleExpr).getValue() instanceof Double) {
        assignNop(gimpleExpr);
        return;
      }
    }
    JimpleExpr number = context.asNumericExpr(gimpleExpr);
    doAssign(JimpleExpr.cast(number, JimpleType.DOUBLE));
  }

  private void realToInteger(GimpleExpr gimpleExpr) {
    JimpleExpr number = context.asNumericExpr(gimpleExpr);
    doAssign(JimpleExpr.cast(number, JimpleType.INT));

  }

  @Override
  public boolean isReal() {
    return gimpleType == PrimitiveType.DOUBLE_TYPE;
  }


  private void assignBoolean(GimpleOp op, List<GimpleExpr> operands) {
    ConditionalTranslator translator = new ConditionalTranslator(context);
    JimpleExpr booleanExpr = translator.translateCondition(op, operands);

    assignBoolean(booleanExpr);
  }

  private void assignNegation(GimpleExpr expr) {
    JimpleExpr condition = new JimpleExpr(context.asNumericExpr(expr) + " != 0");
    assignBoolean(condition);
  }

  private void assignBoolean(JimpleExpr booleanExpr) {
    assignIfElse(booleanExpr, JimpleExpr.integerConstant(1), JimpleExpr.integerConstant(0));
  }


  private void assignBitNot(GimpleExpr operand) {
    doAssign(JimpleExpr.binaryInfix("^", context.asNumericExpr(operand), JimpleExpr.integerConstant(-1)));
  }

  private void assignIfElse(JimpleExpr booleanExpr, JimpleExpr ifTrue, JimpleExpr ifFalse) {
    String trueLabel = context.newLabel();
    String doneLabel = context.newLabel();

    context.getBuilder().addStatement("if " + booleanExpr +
            " goto " + trueLabel);

    context.getBuilder().addStatement(jimpleName + " = " + ifFalse);
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(trueLabel);
    context.getBuilder().addStatement(jimpleName + " = " + ifTrue);
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(doneLabel);
  }

  @Override
  public JimpleExpr asNumericExpr() {
    return new JimpleExpr(jimpleName);
  }

  private void assignNop(GimpleExpr gimpleExpr) {
    doAssign(context.asNumericExpr(gimpleExpr));
  }

  private void assignBinary(String operator, List<GimpleExpr> operands) {
    doAssign(JimpleExpr.binaryInfix(operator,
            context.asNumericExpr(operands.get(0)),
            context.asNumericExpr(operands.get(1))));
  }

  private void assignCompoundRef(GimpleCompoundRef compoundRef) {
    Variable var = context.lookupVar(compoundRef.getVar());
    doAssign(var.memberRef(compoundRef.getMember(), jimpleType));

  }


  private void assignAbs(GimpleExpr gimpleExpr) {
    doAssign(new JimpleExpr("staticinvoke <java.lang.Math: double abs(double)>(" +
            context.asNumericExpr(gimpleExpr) + ")"));
  }

  private void assignMax(List<GimpleExpr> operands) {
    JimpleExpr a = context.asNumericExpr(operands.get(0));
    JimpleExpr b = context.asNumericExpr(operands.get(1));

    assignIfElse(JimpleExpr.binaryInfix(">", a, b), a, b);
  }

  private void doAssign(JimpleExpr rhs) {
    context.getBuilder().addStatement(jimpleName + " = " + rhs);
  }

  private void assignRef(List<GimpleExpr> ops) {
    NumericPtrVar pointer = asPointer(ops.get(0));
    doAssign(pointer.indirectRef());
  }

  private NumericPtrVar asPointer(GimpleExpr expr) {
    if(expr instanceof GimpleVar) {
      Variable var = context.lookupVar((GimpleVar) expr);
      if(var instanceof NumericPtrVar) {
        return (NumericPtrVar) var;
      }
    }
    throw new IllegalArgumentException(expr.toString());
  }


  private void assignConstant(GimpleExpr gimpleExpr) {
    doAssign(context.asNumericExpr(gimpleExpr));
  }

  @Override
  public String toString() {
    return "NumericVar:" + jimpleName;
  }
}
