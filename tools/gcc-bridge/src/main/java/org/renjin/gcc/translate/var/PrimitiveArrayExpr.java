package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPrimitiveArrayType;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;


public abstract class PrimitiveArrayExpr extends AbstractImExpr {

  protected final String name;
  private final ImPrimitiveArrayType type;

  protected PrimitiveArrayExpr(String name, ImPrimitiveArrayType type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public ImExpr elementAt(ImExpr index) {
    return new Element(index);
  }

  @Override
  public ImPrimitiveArrayType type() {
    return type;
  }

  private class Element extends AbstractImExpr implements ImLValue {

    private final ImExpr index;

    public Element(ImExpr index) {
      this.index = index;
    }

    @Override
    public ImExpr addressOf() {
      return new ElementPointer(index);
    }

    @Override
    public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
      return elementRefExpr(context);
    }

    private JimpleExpr elementRefExpr(FunctionContext context) {
      JimpleExpr tempIndex = computeIndex(context);
      return new JimpleExpr(name + "[" + tempIndex + "]");
    }

    private JimpleExpr computeIndex(FunctionContext context) {
      return context.declareTemp(JimpleType.INT, index.translateToPrimitive(context, ImPrimitiveType.INT));
    }

    @Override
    public ImType type() {
      return PrimitiveArrayExpr.this.type().componentType();
    }

    @Override
    public void writeAssignment(FunctionContext context, ImExpr rhs) {
      JimpleExpr jimpleRhs = rhs.translateToPrimitive(context, type.componentType());
      context.getBuilder().addAssignment(elementRefExpr(context), jimpleRhs);
    }
  }

  private class ElementPointer extends AbstractImExpr implements ImIndirectExpr {
    private ImExpr index;

    public ElementPointer(ImExpr index) {

      this.index = index;
    }

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      JimpleExpr jimpleIndex = index.translateToPrimitive(context, ImPrimitiveType.INT);
      return new ArrayRef(name, jimpleIndex);
    }

    @Override
    public ImIndirectType type() {
      return PrimitiveArrayExpr.this.type().componentType().pointerType();
    }
  }
}
