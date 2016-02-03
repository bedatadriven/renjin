package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;


public class FatPtrExpr implements ExprGenerator, LValue<FatPtrExpr>, PtrExpr {

  private Value array;
  private Value offset;
  private ValueFunction valueFunction;

  public FatPtrExpr(Value array, Value offset, ValueFunction valueFunction) {
    this.array = array;
    this.offset = offset;
    this.valueFunction = valueFunction;
  }

  public Value getArray() {
    return array;
  }

  public Value getOffset() {
    return offset;
  }

  public Type getValueType() {
    String arrayDescriptor = array.getType().getDescriptor();
    Preconditions.checkState(arrayDescriptor.startsWith("["));
    return Type.getType(arrayDescriptor.substring(1));
  }

  @Override
  public void store(MethodGenerator mv, FatPtrExpr rhs) {
    if(!(array instanceof LValue) || !(offset instanceof LValue)) {
      throw new InternalCompilerException(this + " is not an LValue");
    }
    ((LValue<Value>) array).store(mv, rhs.getArray());
    ((LValue<Value>) offset).store(mv, rhs.getOffset());
  }
  

  public static PtrExpr alloc(ValueFunction valueFunction, Value length) {
    Value array = Values.newArray(valueFunction.getValueType(), length);
    Value offset = Values.constantInt(0);
    
    return new FatPtrExpr(array, offset, valueFunction);
  }
  

  @Override
  public ExprGenerator valueOf() {
    return valueFunction.dereference(Values.elementAt(array, offset));
  }

  @Override
  public PtrExpr pointerPlus(Value offset) {
    return new FatPtrExpr(array, Values.add(this.offset, offset), valueFunction);
  }
  
  public Value wrap() {
    final Type wrapperType = FatPtrStrategy.wrapperType(getValueType());
    
    return new Value() {
      @Override
      public Type getType() {
        return wrapperType;
      }

      @Override
      public void load(MethodGenerator mv) {
        mv.anew(wrapperType);
        mv.dup();
        array.load(mv);
        offset.load(mv);
        mv.invokeconstructor(wrapperType, array.getType(), offset.getType());
      }
    };
  }
}
