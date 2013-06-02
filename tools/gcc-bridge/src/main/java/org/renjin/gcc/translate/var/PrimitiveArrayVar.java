package org.renjin.gcc.translate.var;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleParam;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.*;

import static org.renjin.gcc.jimple.JimpleExpr.integerConstant;

public class PrimitiveArrayVar extends AbstractImExpr implements Variable {

  private final String name;
  private final ImPrimitiveArrayType type;

  public PrimitiveArrayVar(FunctionContext context, String name, ImPrimitiveArrayType type) {
    this.name = name;
    this.type = type;

    if(type.getLowerBound() != 0) {
      throw new UnsupportedOperationException("non-zero lower array bounds not yet implemented");
    }
    context.getBuilder().addVarDecl(type.componentType().getArrayClass(), name);
    context.getBuilder().addStatement(name + " = newarray (" + type.componentType().asJimple() + ")[" + type.getLength() + "]");
  }

  @Override
  public ImExpr elementAt(ImExpr index) {
    return new ArrayElement(index);
  }

  @Override
  public ImPrimitiveArrayType type() {
    return type;
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
//    if(rhs instanceof PrimitiveArrayVar) {
//      context.getBuilder().addAssignment(name, new JimpleExpr(((PrimitiveArrayVar)rhs).name));
//    } else {
//      throw new UnsupportedOperationException(rhs.toString());
//    }
    throw new UnsupportedOperationException("not sure we should be assigning arrays");
  }



  @Override
  public ArrayPointer addressOf() {
    return new ArrayPointer();
  }

  private class ArrayElement extends AbstractImExpr implements ImLValue {

    private ImExpr index;

    private ArrayElement(ImExpr index) {
      this.index = index;
    }
    @Override
    public void writeAssignment(FunctionContext context, ImExpr rhs) {
      JimpleExpr indexExpr = index.translateToPrimitive(context, ImPrimitiveType.INT);
      JimpleExpr primitiveRhs = rhs.translateToPrimitive(context, type.componentType());
      context.getBuilder().addAssignment(name + "[" + indexExpr + "]", primitiveRhs);
    }

    @Override
    public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
      JimpleExpr indexExpr = index.translateToPrimitive(context, ImPrimitiveType.INT);
      JimpleExpr arrayRef = new JimpleExpr(name + "[" + indexExpr + "]");
      return type.castIfNeeded(arrayRef, type());
    }

    @Override
    public ImExpr addressOf() {
      return new ArrayElementAddress(index);
    }

    @Override
    public ImPrimitiveType type() {
      return type.componentType();
    }
  }

  private class ArrayElementAddress extends AbstractImExpr implements ImIndirectExpr {
    private ImExpr index;

    private ArrayElementAddress(ImExpr index) {
      this.index = index;
    }

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      JimpleExpr indexExpr = index.translateToPrimitive(context, ImPrimitiveType.INT);
      return new ArrayRef(name, indexExpr);
    }

    @Override
    public ImIndirectType type() {
      return type.componentType().pointerType();
    }
  }

  /**
   * Pointer to an array
   */
  private class ArrayPointer extends AbstractImExpr implements ImIndirectExpr {

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      return new ArrayRef(name, integerConstant(0));
    }

    @Override
    public ImIndirectType type() {
      return new ImPrimitiveArrayPtrType(PrimitiveArrayVar.this.type());
    }

    @Override
    public ImExpr memref() {
      return PrimitiveArrayVar.this;
    }

    @Override
    public JimpleExpr translateToObjectReference(FunctionContext context, JimpleType className) {
      if(PrimitiveArrayVar.this.type().asJimple().equals(className)) {
        return new JimpleExpr(PrimitiveArrayVar.this.name);
      } else {
        throw new UnsupportedOperationException(className.toString());
      }
    }
  }
}
