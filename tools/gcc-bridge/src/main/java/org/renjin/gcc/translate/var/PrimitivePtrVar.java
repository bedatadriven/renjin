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
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.types.PrimitiveTypes;

public class PrimitivePtrVar extends Variable {

  private FunctionContext context;
  private String gimpleName;
  private PrimitiveType gimpleType;
  private String jimpleArrayName;
  private String jimpleOffsetName;
  private JimpleType arrayType;
  private JimpleType wrapperType;
  private JimpleType jimpleType;

  public PrimitivePtrVar(FunctionContext context, String gimpleName, PrimitiveType type) {
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
    assignFromWrapper(new JimpleExpr(Jimple.id(gimpleName)));
  }

  public void assignFromWrapper(JimpleExpr wrapperExpr) {
    context.getBuilder().addStatement(jimpleArrayName + " = " + wrapperExpr + ".<" +
            wrapperType + ": " + arrayType + " array>");
    context.getBuilder().addStatement(jimpleOffsetName + " = " + wrapperExpr + ".<" +
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
      case SSA_NAME:
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
    PrimitivePtrVar var = asPtr(operands.get(0));
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
    } else if(isStringConstant(operand)) {
      assignStringConstant((GimpleConstant) operand);
    } else if(operand instanceof GimpleVar) {
      assignPointer(asPtr(operand), JimpleExpr.integerConstant(0));
    } else {
      throw new UnsupportedOperationException(operand.toString());
    }
  }

  private void assignStringConstant(GimpleConstant operand) {
    String literal = (String) operand.getValue();
    // TODO: should be null terminated
    String stringVar = context.getBuilder().addTempVarDecl(new RealJimpleType(String.class));
    context.getBuilder().addStatement(stringVar + " = " + JimpleExpr.stringLiteral(literal));  
    context.getBuilder().addStatement(jimpleArrayName + 
        " = virtualinvoke " + stringVar + ".<java.lang.String: char[] toCharArray()>()");
    context.getBuilder().addStatement(jimpleOffsetName + " = 0");
  }

  private boolean isStringConstant(GimpleExpr operand) {
    return operand instanceof GimpleConstant && ((GimpleConstant) operand).getValue() instanceof String;
  }

  private void assignArrayElement(GimpleArrayRef arrayRef) {
    PrimitivePtrVar var = asPtr(arrayRef.getVar());
    JimpleExpr positionsToIncrement = context.asNumericExpr(arrayRef.getIndex(), JimpleType.INT);
    assignPointer(var, positionsToIncrement);
  }

  private void assignPointer(PrimitivePtrVar var, JimpleExpr offset) {
    context.getBuilder().addStatement(jimpleArrayName + " = " + var.jimpleArrayName);
    context.getBuilder().addStatement(jimpleOffsetName + " = " + var.jimpleOffsetName + " + " + offset);
  }

  private int valueSize() {
    switch (gimpleType) {
      case DOUBLE_TYPE:
        return 8;
      case INT_TYPE:
        return 4;
      case CHAR:
        return 1;
    }
    throw new UnsupportedOperationException(gimpleType.toString());
  }

  private PrimitivePtrVar asPtr(GimpleExpr gimpleExpr) {
    if(gimpleExpr instanceof GimpleVar) {
      Variable var = context.lookupVar((GimpleVar) gimpleExpr);
      if(var instanceof PrimitivePtrVar) {
        return (PrimitivePtrVar) var;
      }
    }
    throw new UnsupportedOperationException("Cannot interpret as pointer : " + gimpleExpr);
  }

  public JimpleExpr indirectRef() {
    return new JimpleExpr(jimpleArrayName + "[" + jimpleOffsetName + "]");
  }

  @Override
  public JimpleExpr asPrimitiveArrayRef(JimpleExpr index) {
    String tempIndex = context.getBuilder().addTempVarDecl(JimpleType.INT);
    context.getBuilder().addStatement(tempIndex + " = " + jimpleOffsetName + " + " + index);
    return new JimpleExpr(jimpleArrayName + "[" + tempIndex + "]");
  }
  
  

  @Override
  public JimpleExpr wrapPointer() {
    JimpleType wrapperType = PrimitiveTypes.getWrapperType(gimpleType);
    String tempWrapper = context.declareTemp(wrapperType);
    context.getBuilder().addStatement(tempWrapper + " = new " + wrapperType);
    context.getBuilder().addStatement("specialinvoke " + tempWrapper + ".<" + wrapperType + ": void <init>(" + 
        PrimitiveTypes.getArrayType(gimpleType) + ", int)>(" + jimpleArrayName + ", " + jimpleOffsetName +  ")");
    
    return new JimpleExpr(tempWrapper);
  }

  @Override
  public JimpleExpr returnExpr() {
    return wrapPointer();    
  }
}
