package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import com.sun.corba.se.impl.orbutil.closure.Constant;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;


public class FatPtrExpr implements ExprGenerator, LValue<FatPtrExpr> {

  private Value array;
  private Value offset;

  public FatPtrExpr(Value array, Value offset) {
    this.array = array;
    this.offset = offset;
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
    if(!(array instanceof LValue)) {
      throw new InternalCompilerException(array + " is not an LValue");
    }
    ((LValue<Value>) array).store(mv, rhs.getArray());

    // Normally, the offset must also be an LValue, but the exception 
    // is that if both the lhs and rhs are constants, and they are equal
    if(offset instanceof ConstantValue &&
        offset.equals(rhs.getOffset())) {
      // No assignment neccessary
      return;
    }
    
    // Otherwise the offset must also be assignable
    if(!(offset instanceof LValue)) {
      throw new InternalCompilerException(offset + " offset is not an Lvalue");
    }

    ((LValue<Value>) array).store(mv, rhs.getArray());
    ((LValue<Value>) offset).store(mv, rhs.getOffset());
  }
  

  public static FatPtrExpr alloc(ValueFunction valueFunction, Value length) {
    Value totalLength = Values.product(length, valueFunction.getElementLength());
    Value array = Values.newArray(valueFunction.getValueType(), totalLength);
    Value offset = Values.constantInt(0);
    
    return new FatPtrExpr(array, offset);
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
