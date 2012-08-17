package org.renjin.gcc.translate.var;


import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.translate.FunctionContext;

import java.util.List;

public class NumericPtrVar extends Variable {

  private FunctionContext context;
  private String gimpleName;
  private PrimitiveType gimpleType;
  private String jimpleArrayName;
  private String jimpleOffsetName;
  private JimpleType arrayType;
  private JimpleType wrapperType;

  public NumericPtrVar(FunctionContext context, String gimpleName, PrimitiveType type) {
    this.context = context;
    this.gimpleName = gimpleName;
    this.gimpleType = type;
    this.jimpleArrayName = Jimple.id(gimpleName) + "_array";
    this.jimpleOffsetName = Jimple.id(gimpleName + "_offset");
    switch(type) {
    case DOUBLE_TYPE:
      arrayType = new JimpleType("double[]");
      wrapperType = new JimpleType(DoublePtr.class);
      break;
    case INT_TYPE:
      arrayType = new JimpleType("int[]");
      wrapperType = new JimpleType(IntPtr.class);
      break;
    }

    context.getBuilder().addVarDecl(arrayType, jimpleArrayName);
    context.getBuilder().addVarDecl(JimpleType.INT, jimpleOffsetName);
  }

  @Override
  public void initFromParameter() {
    context.getBuilder().addStatement(jimpleArrayName + " = " + gimpleName + ".<" +
            wrapperType + ": " + arrayType + " array>");
    context.getBuilder().addStatement(jimpleOffsetName + " = " + gimpleName + ".<" +
            wrapperType + ": int offset>");
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    switch(op) {
      case POINTER_PLUS_EXPR:
        assignPointerPlus(operands);
        break;
      case SSA_NAME:
        assignValue(operands.get(0));
        break;
      case REAL_CST:
        assignConstant((GimpleConstant) operands.get(0));
        break;

      default:
        throw new UnsupportedOperationException(op + " " + operands);
    }
  }

  private void assignConstant(GimpleConstant constant) {
    if(constant.getNumberValue().doubleValue() != 0) {
      throw new UnsupportedOperationException("Can only assign 0 to pointers: " + constant);
    }
    context.getBuilder().addStatement(jimpleArrayName + " = null");
  }

  private void assignValue(GimpleExpr gimpleExpr) {
 //   context.getBuilder().addStatement("staticinvoke <org.renjin.gcc.runtime.Debug: void println(java.lang.String)>(\"assignValue\")");
    context.getBuilder().addStatement(jimpleArrayName + "[" + jimpleOffsetName + "] = " +
        context.asNumericExpr(gimpleExpr));
  }


  private void assignPointerPlus(List<GimpleExpr> operands) {
    NumericPtrVar var = asPtr(operands.get(0));
    JimpleExpr bytesToIncrement = context.asNumericExpr(operands.get(1));
    String positionsToIncrement = context.declareTemp(JimpleType.INT);
    context.getBuilder().addStatement(positionsToIncrement + " = " + bytesToIncrement + " / " + valueSize());
    assignPointer(var, new JimpleExpr(positionsToIncrement));
  }

  private void assignPointer(NumericPtrVar var, JimpleExpr offset) {
    context.getBuilder().addStatement(jimpleArrayName + " = " + var.jimpleArrayName);
    context.getBuilder().addStatement(jimpleOffsetName + " = " + var.jimpleOffsetName + " + " + offset);
  }

  private int valueSize() {
    switch (gimpleType) {
      case DOUBLE_TYPE:
        return 8;
      case INT_TYPE:
        return 4;
    }
    throw new UnsupportedOperationException(gimpleType.toString());
  }

  private NumericPtrVar asPtr(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVar) {
      Variable var = context.lookupVar((GimpleVar) gimpleExpr);
      if(var instanceof NumericPtrVar) {
        return (NumericPtrVar) var;
      }
    }
    throw new UnsupportedOperationException("Cannot interpret as pointer : " + gimpleExpr);
  }

  public JimpleExpr indirectRef() {
    return new JimpleExpr(jimpleArrayName + "[" + jimpleOffsetName + "]");
  }
}
