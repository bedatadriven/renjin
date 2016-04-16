package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An expression generator that uses a combination of an array plus an offset to represent a pointer. 
 */
public final class FatPtrExpr implements Expr, LValue<FatPtrExpr>, Addressable {

  private SimpleExpr array;
  private SimpleExpr offset;
  private Expr address;

  public FatPtrExpr(@Nullable Expr address, @Nonnull SimpleExpr array, @Nonnull SimpleExpr offset) {
    Preconditions.checkNotNull(array, "array");
    Preconditions.checkNotNull(offset, "offset");

    this.address = address;
    this.array = array;
    this.offset = offset;
  }

  public FatPtrExpr(@Nonnull SimpleExpr array, @Nonnull SimpleExpr offset) {
    this(null, array, offset);
  }
  
  public FatPtrExpr(SimpleExpr array) {
    this(array, Expressions.zero());
  }

  @Nonnull
  public SimpleExpr getArray() {
    return array;
  }

  @Nonnull
  public SimpleExpr getOffset() {
    return offset;
  }

  public Type getValueType() {
    String arrayDescriptor = array.getType().getDescriptor();
    Preconditions.checkState(arrayDescriptor.startsWith("["));
    return Type.getType(arrayDescriptor.substring(1));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void store(MethodGenerator mv, FatPtrExpr rhs) {
    if(!(array instanceof LValue)) {
      throw new InternalCompilerException(array + " is not an LValue");
    }
    ((LValue<SimpleExpr>) array).store(mv, rhs.getArray());

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
    ((LValue<SimpleExpr>) offset).store(mv, rhs.getOffset());
  }


  public static FatPtrExpr nullPtr(ValueFunction valueFunction) {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    SimpleExpr nullArray = Expressions.nullRef(arrayType);
    
    return new FatPtrExpr(nullArray);
  }

  public SimpleExpr wrap() {
    final Type wrapperType = Wrappers.wrapperType(getValueType());
    
    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return wrapperType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.anew(wrapperType);
        mv.dup();
        array.load(mv);
        offset.load(mv);
        mv.invokeconstructor(wrapperType, Wrappers.fieldArrayType(wrapperType), offset.getType());
      }
    };
  }

  @Override
  public Expr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }
}
