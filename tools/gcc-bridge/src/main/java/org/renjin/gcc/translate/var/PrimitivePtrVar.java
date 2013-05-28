package org.renjin.gcc.translate.var;

import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TypeChecker;
import org.renjin.gcc.translate.PrimitiveAssignment;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.*;


public class PrimitivePtrVar extends AbstractImExpr implements Variable, ImIndirectExpr {


  private enum OffsetType {
    BYTES,
    ELEMENTS
  }

  private FunctionContext context;
  private String gimpleName;
  private final ImPrimitivePtrType type;
  private String jimpleArrayName;
  private String jimpleOffsetName;

  public PrimitivePtrVar(FunctionContext context, String gimpleName, ImPrimitivePtrType type) {
    this.context = context;
    this.gimpleName = gimpleName;
    this.type = type;
    this.jimpleArrayName = Jimple.id(gimpleName) + "_array";
    this.jimpleOffsetName = Jimple.id(gimpleName + "_offset");

    context.getBuilder().addVarDecl(type.getArrayClass(), jimpleArrayName);
    context.getBuilder().addVarDecl(JimpleType.INT, jimpleOffsetName);
  }

  private int sizeOf() {
    return type.getBaseType().getStorageSizeInBytes();
  }

  @Override
  public void writeAssignment(FunctionContext context, ImExpr rhs) {
    if(rhs.isNull()) {
      context.getBuilder().addStatement(jimpleArrayName + " = null");
    } else if(rhs instanceof ImIndirectExpr) {
      ArrayRef ptr = ((ImIndirectExpr) rhs).translateToArrayRef(context);
      context.getBuilder().addStatement(jimpleArrayName + " = " +  ptr.getArrayExpr());
      context.getBuilder().addStatement(jimpleOffsetName + " = " + ptr.getIndexExpr());
    }
  }

  @Override
  public ArrayRef translateToArrayRef(FunctionContext context) {
    return new ArrayRef(jimpleArrayName, jimpleOffsetName);
  }

  @Override
  public ImExpr memref() {
    return new ValueExpr();
  }

  @Override
  public ImPrimitivePtrType type() {
    return type;
  }

  @Override
  public ImExpr pointerPlus(ImExpr offset) {
    return new OffsetExpr(offset, OffsetType.BYTES);
  }

  @Override
  public String toString() {
    return gimpleName + ":" + type;
  }

  /**
   * An expression representing this pointer + an offset (p+4)
   *
   */
  public class OffsetExpr extends AbstractImExpr implements ImIndirectExpr {

    private ImExpr offset;
    private OffsetType offsetType;


    public OffsetExpr(ImExpr offset, OffsetType offsetType) {
      super();
      this.offset = offset;
      this.offsetType = offsetType;
    }

    @Override
    public ImType type() {
      return PrimitivePtrVar.this.type();
    }

    public PrimitivePtrVar variable() {
      return PrimitivePtrVar.this;
    }

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      return new ArrayRef(jimpleArrayName, computeIndex());
    }

    private JimpleExpr computeIndex() {
      if(offsetType == OffsetType.BYTES) {
        JimpleExpr bytesToIncrement = offset.translateToPrimitive(context, ImPrimitiveType.INT);
        String positionsToIncrement = context.declareTemp(JimpleType.INT);
        context.getBuilder().addStatement(positionsToIncrement + " = " + bytesToIncrement + " / " + sizeOf());
        return new JimpleExpr(jimpleOffsetName + " + " + positionsToIncrement);    
      } else {
        return new JimpleExpr(jimpleOffsetName + " + " +
            offset.translateToPrimitive(context, ImPrimitiveType.INT));
      }
    }
  }

  /**
   * An expression representing the value of the pointer
   * (*x)
   *
   */
  public class ValueExpr extends AbstractImExpr implements PrimitiveLValue, ImLValue {


    @Override
    public ImExpr addressOf() {
      return PrimitivePtrVar.this;
    }

    @Override
    public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
      return type.castIfNeeded(
          new JimpleExpr(jimpleArrayName + "[" + jimpleOffsetName + "]"),
          type());
    }

    @Override
    public ImPrimitiveType type() {
      return type.getBaseType();
    }

    @Override
    public void writePrimitiveAssignment(JimpleExpr expr) {
      context.getBuilder().addStatement(jimpleArrayName + "[" + jimpleOffsetName + "] = " + expr);
    }

    @Override
    public void writeAssignment(FunctionContext context, ImExpr rhs) {
      PrimitiveAssignment.assign(context, this, rhs);
    }

    @Override
    public ImExpr elementAt(ImExpr index) {
      return new ArrayElementExpr(index);
    }

  }

  public class ArrayElementExpr extends AbstractImExpr implements PrimitiveLValue, ImLValue {

    /**
     * Index of the array, with reference to the current offset.
     */
    private ImExpr index;

    public ArrayElementExpr(ImExpr index) {
      if(!TypeChecker.isInt(index.type())) {
        throw new UnsupportedOperationException();
      }
      this.index = index;
    }

    @Override
    public JimpleExpr translateToPrimitive(FunctionContext context, ImPrimitiveType type) {
      // get the overall index
      return type.castIfNeeded(
          new JimpleExpr(jimpleArrayName + "[" + computeOverallIndex(context) + "]"),
          this.type());
    }

    
    /**
     * Create a temporary variable storing the index of the element this expr references
     * with reference to the beginning of the array.
     * @return the name of the temporary variable
     */
    private String computeOverallIndex(FunctionContext context) {
      String overallIndex = context.declareTemp(JimpleType.INT);
      context.getBuilder().addStatement(overallIndex + " = " + jimpleOffsetName + " + " +
              index.translateToPrimitive(context, ImPrimitiveType.INT));
      return overallIndex;
    }


    @Override
    public ImExpr addressOf() {
      return new OffsetExpr(index, OffsetType.ELEMENTS);
    }

    @Override
    public ImPrimitiveType type() {
      return type.getBaseType();
    }

    @Override
    public void writePrimitiveAssignment(JimpleExpr expr) {
      context.getBuilder().addStatement(jimpleArrayName + "[" + computeOverallIndex(context) + "] = " + expr);
    }

    @Override
    public void writeAssignment(FunctionContext context, ImExpr rhs) {
      PrimitiveAssignment.assign(context, this, rhs);
    }
  }
}

