package org.renjin.gcc.translate.var;


import java.lang.reflect.Array;
import java.util.List;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleArrayRef;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.types.PrimitiveTypes;

import com.google.common.primitives.Primitives;

public class NumericPtrVar extends Variable {

  private FunctionContext context;
  private String gimpleName;
  private PrimitiveType gimpleType;
  private String jimpleArrayName;
  private String jimpleOffsetName;
  private JimpleType arrayType;
  private JimpleType wrapperType;
  private JimpleType jimpleType;

  public NumericPtrVar(FunctionContext context, String gimpleName, PrimitiveType type) {
    this.context = context;
    this.gimpleName = gimpleName;
    this.gimpleType = type;
    this.jimpleArrayName = Jimple.id(gimpleName) + "_array";
    this.jimpleOffsetName = Jimple.id(gimpleName + "_offset");
    this.arrayType = PrimitiveTypes.getArrayType(type);
    this.wrapperType = PrimitiveTypes.getWrapperType(type);
    this.jimpleType = PrimitiveTypes.get(type);

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
  public void initFromConstant(Object value) {
    if(!value.getClass().isArray()) {
      throw new UnsupportedOperationException("Cannot init ptr from value '" + value + "' (" + value.getClass() + ")");
    }
    context.getBuilder().addStatement(String.format("%s = newarray (%s)[%d]",
        jimpleArrayName, 
        PrimitiveTypes.get(gimpleType),
        Array.getLength(value)));
    
    for(int i=0;i!=Array.getLength(value);++i) {
      context.getBuilder().addStatement(String.format("%s[%d] = %s",
         jimpleArrayName, i, 
         Array.get(value, i).toString()));
    }
    
    
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    switch(op) {
      case POINTER_PLUS_EXPR:
        assignPointerPlus(operands);
        break;
      case ADDR_EXPR:
        assignAddress(operands);
        break;
      case REAL_CST:
        assignNullPtr((GimpleConstant) operands.get(0));
        break;

      default:
        throw new UnsupportedOperationException(op + " " + operands);
    }
  }


  @Override
  public void assignIndirect(GimpleOp op, List<GimpleExpr> operands) {
    switch(op) {
    case VAR_DECL:
    case SSA_NAME:
    case REAL_CST:
      assignValue(operands.get(0));
      break;


    default:
      throw new UnsupportedOperationException(op + " " + operands);
    }
  }

  private void assignNullPtr(GimpleConstant constant) {
    if(constant.getNumberValue().doubleValue() != 0) {
      throw new UnsupportedOperationException("Can only assign 0 to pointers: " + constant);
    }
    context.getBuilder().addStatement(jimpleArrayName + " = null");
  }
  

  private void assignValue(GimpleExpr gimpleExpr) {
    context.getBuilder().addStatement(jimpleArrayName + "[" + jimpleOffsetName + "] = " +
        context.asNumericExpr(gimpleExpr, PrimitiveTypes.get(gimpleType)));
  }


  private void assignPointerPlus(List<GimpleExpr> operands) {
    NumericPtrVar var = asPtr(operands.get(0));
    JimpleExpr bytesToIncrement = context.asNumericExpr(operands.get(1), JimpleType.INT);
    String positionsToIncrement = context.declareTemp(JimpleType.INT);
    context.getBuilder().addStatement(positionsToIncrement + " = " + bytesToIncrement + " / " + valueSize());
    assignPointer(var, new JimpleExpr(positionsToIncrement));
  }


  private void assignAddress(List<GimpleExpr> operands) {
    GimpleExpr operand = operands.get(0);
    if(operand instanceof GimpleAddressOf) {
      GimpleExpr value = ((GimpleAddressOf) operand).getExpr();
      if(value instanceof GimpleArrayRef) {
        assignArrayElement((GimpleArrayRef)value);
      }
    } else {
      throw new UnsupportedOperationException(operand.toString());
    }
  }

  private void assignArrayElement(GimpleArrayRef arrayRef) {
    NumericPtrVar var = asPtr(arrayRef.getVar());
    JimpleExpr positionsToIncrement = context.asNumericExpr(arrayRef.getIndex(), JimpleType.INT);
    assignPointer(var, positionsToIncrement);
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

  @Override
  public JimpleExpr asNumericArrayRef(JimpleExpr index) {

    String tempIndex = context.getBuilder().addTempVarDecl(JimpleType.INT);
    context.getBuilder().addStatement(tempIndex + " = " + jimpleOffsetName + " + " + index);
    return new JimpleExpr(jimpleArrayName + "[" + tempIndex + "]");
  }
  
  
}
