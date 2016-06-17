package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An expression generator that uses a combination of an array plus an offset to represent a pointer. 
 */
public final class FatPtrExpr implements GExpr {

  private JExpr array;
  private JExpr offset;
  private GExpr address;

  public FatPtrExpr(@Nullable GExpr address, @Nonnull JExpr array, @Nonnull JExpr offset) {
    Preconditions.checkNotNull(array, "array");
    Preconditions.checkNotNull(offset, "offset");

    this.address = address;
    this.array = array;
    this.offset = offset;
  }

  public FatPtrExpr(@Nonnull JExpr array, @Nonnull JExpr offset) {
    this(null, array, offset);
  }
  
  public FatPtrExpr(JExpr array) {
    this(array, Expressions.zero());
  }

  @Nonnull
  public JExpr getArray() {
    return array;
  }

  @Nonnull
  public JExpr getOffset() {
    return offset;
  }

  public Type getValueType() {
    String arrayDescriptor = array.getType().getDescriptor();
    Preconditions.checkState(arrayDescriptor.startsWith("["));
    return Type.getType(arrayDescriptor.substring(1));
  }

  public FatPtrExpr copyOf() {
    return new FatPtrExpr(Expressions.copyOfArray(array), offset);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void store(MethodGenerator mv, GExpr rhsExpr) {
    
    FatPtrExpr rhs = (FatPtrExpr) rhsExpr;
    
    if(!(array instanceof JLValue)) {
      throw new InternalCompilerException(array + " is not an LValue");
    }
    ((JLValue) array).store(mv, rhs.getArray());

    // Normally, the offset must also be an LValue, but the exception 
    // is that if both the lhs and rhs are constants, and they are equal
    if(offset instanceof ConstantValue &&
        offset.equals(rhs.getOffset())) {
      // No assignment neccessary
      return;
    }
    
    // Otherwise the offset must also be assignable
    if(!(offset instanceof JLValue)) {
      throw new InternalCompilerException(offset + " offset is not an Lvalue");
    }
    ((JLValue) offset).store(mv, rhs.getOffset());
  }


  public static FatPtrExpr nullPtr(ValueFunction valueFunction) {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    JExpr nullArray = Expressions.nullRef(arrayType);
    
    return new FatPtrExpr(nullArray);
  }

  public JExpr wrap() {
    final Type wrapperType = Wrappers.wrapperType(getValueType());
    
    return new JExpr() {
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
  
  public boolean isAddressable() {
    return address != null;
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }
}
