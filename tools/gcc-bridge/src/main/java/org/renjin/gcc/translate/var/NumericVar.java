package org.renjin.gcc.translate.var;


import java.util.List;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleCompoundRef;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIndirection;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.ConditionalTranslator;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.types.PrimitiveTypes;

public class NumericVar extends Variable {

  private final FunctionContext context;
  private final PrimitiveType gimpleType;
  private final JimpleType jimpleType;
  private final NumericStorage storage;

  public NumericVar(FunctionContext context, PrimitiveType gimpleType, NumericStorage storage) {
    this.context = context;
    this.gimpleType = gimpleType;
    this.storage = storage;
    this.jimpleType  = PrimitiveTypes.get(gimpleType);
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    switch (op) {
      case VAR_DECL:
      case SSA_NAME:
      case NOP_EXPR:
      case PAREN_EXPR:
      case ARRAY_REF:
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
        
      case NEGATE_EXPR:
        assignNegated(operands);
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
      case MEM_REF:
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
    if(!jimpleType.equals(JimpleType.DOUBLE) && !jimpleType.equals(JimpleType.FLOAT) ) {
      throw new IllegalStateException();
    }
    storage.assign(context.asNumericExpr(gimpleExpr, jimpleType));
  }

  private void realToInteger(GimpleExpr gimpleExpr) {
    storage.assign(context.asNumericExpr(gimpleExpr, JimpleType.INT));
  }
  
  @Override
  public JimpleType getNumericType() {
    return jimpleType;
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
    JimpleExpr condition = new JimpleExpr(asTypedNumericExpr(expr) + " != 0");
    assignBoolean(condition);
  }

  private void assignBoolean(JimpleExpr booleanExpr) {
    assignIfElse(booleanExpr, JimpleExpr.integerConstant(1), JimpleExpr.integerConstant(0));
  }


  private void assignBitNot(GimpleExpr operand) {
    storage.assign(JimpleExpr.binaryInfix("^", asTypedNumericExpr(operand), JimpleExpr.integerConstant(-1)));
  }

  private void assignIfElse(JimpleExpr booleanExpr, JimpleExpr ifTrue, JimpleExpr ifFalse) {
    String trueLabel = context.newLabel();
    String doneLabel = context.newLabel();

    context.getBuilder().addStatement("if " + booleanExpr +
            " goto " + trueLabel);

    storage.assign(ifFalse);
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(trueLabel);
    storage.assign(ifTrue);
    context.getBuilder().addStatement("goto " + doneLabel);

    context.getBuilder().addLabel(doneLabel);
  }

  @Override
  public JimpleExpr asNumericExpr(JimpleType type) {
    if(type.equals(jimpleType)) {
      return storage.asNumericExpr();
    } else if(this.jimpleType.equals(JimpleType.BOOLEAN) && type.equals(JimpleType.INT)) {
      return storage.asNumericExpr();
    } else {
      String tempVar = context.declareTemp(type);
      context.getBuilder().addStatement(String.format("%s = (%s)%s", 
          tempVar,
          type.toString(), storage.asNumericExpr().toString()));
      return new JimpleExpr(tempVar);
    }
  }
  
  @Override
  public JimpleExpr addressOf() {
    return storage.addressOf();
  }

  private void assignNop(GimpleExpr gimpleExpr) {
    storage.assign(asTypedNumericExpr(gimpleExpr));
  }

  private void assignBinary(String operator, List<GimpleExpr> operands) {
    storage.assign(JimpleExpr.binaryInfix(operator,
            asTypedNumericExpr(operands.get(0)),
            asTypedNumericExpr(operands.get(1))));
  }
  

  private void assignNegated(List<GimpleExpr> operands) {
    storage.assign(new JimpleExpr("neg " + asTypedNumericExpr(operands.get(0))));
  }

  private void assignCompoundRef(GimpleCompoundRef compoundRef) {
    Variable var = context.lookupVar(compoundRef.getVar());
    storage.assign(var.memberRef(compoundRef.getMember(), PrimitiveTypes.get(gimpleType)));
  }

  private void assignAbs(GimpleExpr gimpleExpr) {
    switch(this.gimpleType) {
    case DOUBLE_TYPE:
      storage.assign(new JimpleExpr("staticinvoke <java.lang.Math: double abs(double)>(" +
          asTypedNumericExpr(gimpleExpr) + ")"));
      break;
    case INT_TYPE:
      storage.assign(new JimpleExpr("staticinvoke <java.lang.Math: int abs(int)>(" +
          asTypedNumericExpr(gimpleExpr) + ")"));
      break;
    default:
      throw new UnsupportedOperationException("abs on type " + this.gimpleType.name());
    }

  }

  private void assignMax(List<GimpleExpr> operands) {
    JimpleExpr a = asTypedNumericExpr(operands.get(0));
    JimpleExpr b = asTypedNumericExpr(operands.get(1));

    assignIfElse(JimpleExpr.binaryInfix(">", a, b), a, b);
  }


  private void assignRef(List<GimpleExpr> ops) {
    NumericPtrVar pointer = asPointer(ops.get(0));
    storage.assign(pointer.indirectRef());
  }

  private NumericPtrVar asPointer(GimpleExpr expr) {
    if(expr instanceof GimpleVar) {
      Variable var = context.lookupVar((GimpleVar) expr);
      if(var instanceof NumericPtrVar) {
        return (NumericPtrVar) var;
      }
    } else if(expr instanceof GimpleIndirection) {
      return asPointer(((GimpleIndirection) expr).getPointer());
    }
    throw new IllegalArgumentException(expr.toString());
  }


  private void assignConstant(GimpleExpr gimpleExpr) {
    storage.assign(asTypedNumericExpr(gimpleExpr));
  }

  private JimpleExpr asTypedNumericExpr(GimpleExpr gimpleExpr) {
    return context.asNumericExpr(gimpleExpr, PrimitiveTypes.get(gimpleType));
  }

  @Override
  public String toString() {
    return "NumericVar:" + storage;
  }
}
