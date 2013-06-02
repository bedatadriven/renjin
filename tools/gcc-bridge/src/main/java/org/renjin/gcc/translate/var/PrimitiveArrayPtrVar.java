package org.renjin.gcc.translate.var;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPrimitiveArrayPtrType;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;

public class PrimitiveArrayPtrVar extends AbstractImExpr implements Variable,ImIndirectExpr {

  private FunctionContext context;
  private final String jimpleArrayName;
  private final String jimpleStartIndexName;
  private final ImPrimitiveArrayPtrType type;
  private int lowerBound;

  public PrimitiveArrayPtrVar(FunctionContext context, String name, ImPrimitiveArrayPtrType type) {
    this.context = context;
    this.jimpleArrayName = name;
    this.jimpleStartIndexName = name + "_offset";
    this.type = type;
    this.lowerBound = type.baseType().getLowerBound();

    context.getBuilder().addVarDecl(type.baseType().componentType().getArrayClass(), name);
    context.getBuilder().addVarDecl(JimpleType.INT, jimpleStartIndexName);
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    if(rhs.isNull()) {
      context.getBuilder().addStatement(jimpleArrayName + " = null");
    } else if(rhs instanceof ImIndirectExpr) {
      ArrayRef ptr = ((ImIndirectExpr) rhs).translateToArrayRef(context);
      context.getBuilder().addStatement(jimpleArrayName + " = " +  ptr.getArrayExpr());
      context.getBuilder().addStatement(jimpleStartIndexName + " = " + ptr.getIndexExpr());
    } else {
      throw new UnsupportedOperationException(rhs.toString());
    }
  }

  @Override
  public ArrayRef translateToArrayRef(FunctionContext context) {
    return new ArrayRef(jimpleArrayName, jimpleStartIndexName);
  }

  @Override
  public ImIndirectType type() {
    return type;
  }

  @Override
  public ImExpr memref() {
    return new ArrayValue();
  }

  private JimpleExpr computeIndex(ImExpr index) {
    JimpleExpr indexExpr = index.translateToPrimitive(context, ImPrimitiveType.INT);
    JimpleExpr finalIndex = context.declareTemp(JimpleType.INT,
        JimpleExpr.binaryInfix("+", new JimpleExpr(jimpleStartIndexName), indexExpr));

    if(lowerBound != 0) {
      finalIndex = context.declareTemp(JimpleType.INT,
          JimpleExpr.binaryInfix("-", finalIndex, JimpleExpr.integerConstant(lowerBound)));
    }

    return finalIndex;
  }

  private class ArrayValue extends AbstractImExpr {

    @Override
    public ImExpr addressOf() {
      return PrimitiveArrayPtrVar.this;
    }

    @Override
    public ImExpr elementAt(ImExpr index) {
      return new ArrayElement(index);

    }

    @Override
    public ImType type() {
      return type.baseType();
    }
  }

  private class ArrayElement extends AbstractImExpr implements ImLValue {

    private final ImExpr index;

    public ArrayElement(ImExpr index) {
      this.index = index;
    }

    @Override
    public ImType type() {
      return type.baseType().componentType();
    }

    @Override
    public ImExpr addressOf() {
      return new ArrayElementAddress(index);
    }

    @Override
    public void writeAssignment(FunctionContext context, ImExpr rhs) {
      context.getBuilder().addAssignment(jimpleArrayName + "[" + computeIndex(index) + "]",
          rhs.translateToPrimitive(context, type.baseType().componentType()));
    }

    @Override
    public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
      return new JimpleExpr(jimpleArrayName + "[" + computeIndex(index) + "]");
    }
  }

  private class ArrayElementAddress extends AbstractImExpr implements ImIndirectExpr {

    private ImExpr index;

    private ArrayElementAddress(ImExpr index) {
      this.index = index;
    }

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      return new ArrayRef(jimpleArrayName, computeIndex(index));
    }

    @Override
    public ImIndirectType type() {
      return type.baseType().componentType().pointerType();
    }
  }
}
