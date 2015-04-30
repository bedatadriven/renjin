package org.renjin.gcc.translate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.expr.PrimitiveLValue;
import org.renjin.gcc.translate.type.ImPrimitiveType;

import java.util.List;

public class AssignmentTranslator {
  private FunctionContext context;

  public AssignmentTranslator(FunctionContext context) {
    this.context = context;
  }

  public void translate(GimpleAssign assign) {

    if(assign.getOperator() == GimpleOp.CONSTRUCTOR) {
      return;
    }

    ImExpr lhs = context.resolveExpr(assign.getLHS());
    List<ImExpr> operands = resolveOps(assign.getOperands());

    switch(assign.getOperator()) {
    case INTEGER_CST:
    case MEM_REF:
    case ADDR_EXPR:
    case REAL_CST:
    case VAR_DECL:
    case PARM_DECL:
    case FLOAT_EXPR:
    case NOP_EXPR:
    case ARRAY_REF:
    case PAREN_EXPR:
    case COMPONENT_REF:
    case CONVERT_EXPR:
      assign(lhs, operands.get(0));
      return;
      
    case POINTER_PLUS_EXPR:
      assign(lhs, operands.get(0).pointerPlus(operands.get(1)));
      return;
     
    case EQ_EXPR:
    case NE_EXPR:
    case LE_EXPR:
    case LT_EXPR:
    case GT_EXPR:
    case GE_EXPR:
      assignComparison(lhs, new Comparison(assign.getOperator(), operands.get(0), operands.get(1)));
      break;
      
    case MULT_EXPR:
      assignBinaryOp(lhs, "*", operands);
      break;
      
    case PLUS_EXPR:
      assignBinaryOp(lhs, "+", operands);
      break;

    case MINUS_EXPR:
      assignBinaryOp(lhs, "-", operands);
      break;

    case RDIV_EXPR:
    case TRUNC_DIV_EXPR:
    case EXACT_DIV_EXPR:
      assignDiv(lhs, operands);
      break;
      
    case TRUNC_MOD_EXPR:
      assignBinaryOp(lhs, "%", operands);
      break;

    case FIX_TRUNC_EXPR:
      assignFixTruncExpr(lhs, operands.get(0));
      break;

    case BIT_NOT_EXPR:
      assignBitNot(lhs, operands.get(0));
      break;

    case BIT_AND_EXPR:
      assignBinaryOp(lhs, "&", operands);
      break;

    case BIT_IOR_EXPR:
      assignBinaryOp(lhs, "|", operands);
      break;

    case BIT_XOR_EXPR:
      assignBinaryOp(lhs, "^", operands);
      break;
    
    case LSHIFT_EXPR:
      assignBinaryOp(lhs, "<<", operands);
      break;
    
    case RSHIFT_EXPR:
      assignBinaryOp(lhs, ">>", operands);
      break;
    
    case NEGATE_EXPR:
      assignNegated(lhs, operands.get(0));
      break;

    case ABS_EXPR:
      assignAbs(lhs, operands.get(0));
      break;

    case MAX_EXPR:
      assignMax(lhs, operands);
      break;

    case UNORDERED_EXPR:
      assignUnordered(lhs, operands);
      break;

    case TRUTH_NOT_EXPR:
      assignTruthNot(lhs, operands.get(0));
      break;
    
    case TRUTH_AND_EXPR:
      assignTruthAnd(lhs, operands);
      break;

    case TRUTH_OR_EXPR:
      assignTruthOr(lhs, operands);
      break;
      
    default:
      throw new UnsupportedOperationException(assign.getOperator().toString());
    }
  }

  private void assignFixTruncExpr(ImExpr lhs, ImExpr rhs) {
    JimpleExpr jimpleRhs = rhs.translateToPrimitive(context, (ImPrimitiveType) lhs.type());
    assignPrimitive(lhs, jimpleRhs);
  }

  private void assignDiv(ImExpr lhs, List<ImExpr> operands) {
    ImExpr x = operands.get(0);
    ImExpr y = operands.get(1);

    TypeChecker.assertSamePrimitiveType(x, y);

    assignBinaryOp(lhs, "/", operands);
  }


  private void assignNegated(ImExpr lhs, ImExpr expr) {
    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(lhs, expr);

    assignPrimitive(lhs, new JimpleExpr("neg " + expr.translateToPrimitive(context, type)));
  }

  private void assignBinaryOp(ImExpr lhs, String operator, List<ImExpr> operands) {

    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(lhs, operands.get(0), operands.get(1));
    
    JimpleExpr a = operands.get(0).translateToPrimitive(context, type);
    JimpleExpr b = operands.get(1).translateToPrimitive(context, type);

    assignPrimitive(lhs, JimpleExpr.binaryInfix(operator, a, b));
  }
  
  private List<ImExpr> resolveOps(List<GimpleExpr> operands) {
    List<ImExpr> exprs = Lists.newArrayList();
    for(GimpleExpr op : operands) {
      exprs.add(context.resolveExpr(op));
    }
    return exprs;
  }

  private void assignComparison(ImExpr lhs, Comparison comparison) {
    assignIfElse(lhs, comparison.toCondition(context), JimpleExpr.integerConstant(1), JimpleExpr.integerConstant(0));
  }
  
  private void assignPrimitive(ImExpr lhs, JimpleExpr jimpleExpr) {
    ((PrimitiveLValue)lhs).writePrimitiveAssignment(jimpleExpr);
  }

  private void assignTruthNot(ImExpr lhs, ImExpr op) {
    JimpleExpr expr = op.translateToPrimitive(context, ImPrimitiveType.BOOLEAN);
    JimpleExpr condition = new JimpleExpr(expr + " == 0");
    assignBoolean(lhs, condition);
  }

  private void assignTruthOr(ImExpr lhs, List<ImExpr> ops) {
    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(ops.get(0), ops.get(1));
    if(type != ImPrimitiveType.BOOLEAN) {
      throw new UnsupportedOperationException(type.toString());
    }

    JimpleExpr a = ops.get(0).translateToPrimitive(context, ImPrimitiveType.BOOLEAN);
    JimpleExpr b = ops.get(1).translateToPrimitive(context, ImPrimitiveType.BOOLEAN);
    
    String checkB = context.newLabel();
    String noneIsTrue = context.newLabel();
    String doneLabel = context.newLabel();


    context.getBuilder().addStatement("if " + a + " == 0 goto " + checkB);
    assignPrimitive(lhs, JimpleExpr.integerConstant(1));
    context.getBuilder().addStatement("goto " + doneLabel);
    
    context.getBuilder().addLabel(checkB);
    context.getBuilder().addStatement("if " + b + " == 0 goto " + noneIsTrue);
    assignPrimitive(lhs, JimpleExpr.integerConstant(1));
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(noneIsTrue);
    assignPrimitive(lhs, JimpleExpr.integerConstant(0));

    context.getBuilder().addLabel(doneLabel);

  }
  
  private void assignTruthAnd(ImExpr lhs, List<ImExpr> ops) {
    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(ops.get(0), ops.get(1));
    if(type != ImPrimitiveType.BOOLEAN) {
      throw new UnsupportedOperationException(type.toString());
    }

    JimpleExpr a = ops.get(0).translateToPrimitive(context, ImPrimitiveType.BOOLEAN);
    JimpleExpr b = ops.get(1).translateToPrimitive(context, ImPrimitiveType.BOOLEAN);

    String checkB = context.newLabel();
    String noneIsTrue = context.newLabel();
    String doneLabel = context.newLabel();


    context.getBuilder().addStatement("if " + a + " != 0 goto " + checkB);
    assignPrimitive(lhs, JimpleExpr.integerConstant(0));
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(checkB);
    context.getBuilder().addStatement("if " + b + " != 0 goto " + noneIsTrue);
    assignPrimitive(lhs, JimpleExpr.integerConstant(1));
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(noneIsTrue);
    assignPrimitive(lhs, JimpleExpr.integerConstant(0));

    context.getBuilder().addLabel(doneLabel);
  }

  private void assignBoolean(ImExpr lhs, JimpleExpr booleanExpr) {
    assignIfElse(lhs, booleanExpr, JimpleExpr.integerConstant(1), JimpleExpr.integerConstant(0));
  }

  private void assignBitNot(ImExpr lhs, ImExpr op) {
    TypeChecker.assertSameType(lhs, op);

    assignPrimitive(lhs, JimpleExpr.binaryInfix("^",
        op.translateToPrimitive(context, ImPrimitiveType.INT),
        JimpleExpr.integerConstant(-1)));
  }

  private void assignIfElse(ImExpr lhs, JimpleExpr booleanExpr, JimpleExpr ifTrue, JimpleExpr ifFalse) {
    String trueLabel = context.newLabel();
    String doneLabel = context.newLabel();

    context.getBuilder().addStatement("if " + booleanExpr + " goto " + trueLabel);

    assignPrimitive(lhs, ifFalse);
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(trueLabel);
    assignPrimitive(lhs, ifTrue);
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(doneLabel);
  }


  private void assignUnordered(ImExpr lhs, List<ImExpr> operands) {
    ImExpr x = operands.get(0);
    ImExpr y = operands.get(1);

    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(x, y);
    Preconditions.checkArgument(type == ImPrimitiveType.DOUBLE);

    if(TypeChecker.isDouble(x.type())) {
      //assignPrimitive(lhs, JimpleExpr.integerConstant(0));
      assignPrimitive(lhs, new JimpleExpr(String.format(
              "staticinvoke <org.renjin.gcc.runtime.Builtins: boolean unordered(double, double)>(%s, %s)",
              x.translateToPrimitive(context, ImPrimitiveType.DOUBLE),
              y.translateToPrimitive(context, ImPrimitiveType.DOUBLE))));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private ImPrimitiveType prtype(ImExpr expr) {
    if(expr.type() instanceof ImPrimitiveType) {
      return (ImPrimitiveType) expr.type();
    } else {
      throw new UnsupportedOperationException("Expected expression of primitive type, got: " + expr);
    }
  }


  private void assignAbs(ImExpr lhs, ImExpr expr) {
    
    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(lhs, expr);

    assignPrimitive(lhs, new JimpleExpr(String.format("staticinvoke <java.lang.Math: %s>(%s)",
        absMethodForType(prtype(expr)),
        expr.translateToPrimitive(context, type))));
    
  }

  private String absMethodForType(ImPrimitiveType type) {
    switch(type) {
      case DOUBLE:
        return "double abs(double)";
      case INT:
        return "int abs(int)";
      case FLOAT:
        return "float abs(float)";
    }
    throw new UnsupportedOperationException("abs on type " + type.toString());
  }

  private void assignMax(ImExpr lhs, List<ImExpr> operands) {
    ImPrimitiveType type = TypeChecker.assertSamePrimitiveType(lhs, operands.get(0), operands.get(1));

    String signature = "{t} max({t}, {t})"
            .replace("{t}", prtype(lhs).asJimple().toString());
    
    JimpleExpr a = operands.get(0).translateToPrimitive(context, type);
    JimpleExpr b = operands.get(1).translateToPrimitive(context, type);

    assignPrimitive(lhs, new JimpleExpr(String.format(
            "staticinvoke <java.lang.Math: %s>(%s, %s)",
            signature, a.toString(), b.toString())));


  }
  
  private void assign(ImExpr lhs, ImExpr rhs) {
    if(lhs instanceof PrimitiveLValue) {
      PrimitiveAssignment.assign(context, lhs, rhs);
    } else if(lhs instanceof ImLValue) {
      ((ImLValue) lhs).writeAssignment(context, rhs);
    } else {
      throw new UnsupportedOperationException("Unsupported assignment of " + rhs.toString() + " to " + lhs.toString());
    }
  }

}
