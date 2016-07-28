package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A FatPtr expression compiled as a pair of array and an offset.
 */
public final class FatPtrPair implements FatPtr {

  private JExpr array;
  private JExpr offset;
  private GExpr address;

  public FatPtrPair(@Nullable GExpr address, @Nonnull JExpr array, @Nonnull JExpr offset) {
    Preconditions.checkNotNull(array, "array");
    Preconditions.checkNotNull(offset, "offset");

    this.address = address;
    this.array = array;
    this.offset = offset;
  }

  public FatPtrPair(@Nonnull JExpr array, @Nonnull JExpr offset) {
    this(null, array, offset);
  }
  
  public FatPtrPair(JExpr array) {
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

  @Override
  public Type getValueType() {
    String arrayDescriptor = array.getType().getDescriptor();
    Preconditions.checkState(arrayDescriptor.startsWith("["));
    return Type.getType(arrayDescriptor.substring(1));
  }

  public FatPtrPair copyOf() {
    return new FatPtrPair(Expressions.copyOfArray(array), offset);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void store(MethodGenerator mv, GExpr rhsExpr) {

    if(rhsExpr instanceof VoidPtr) {
      VoidPtr ptr = (VoidPtr) rhsExpr;
      Type wrapperType = Wrappers.wrapperType(getValueType());
      JExpr castedWrapper = Expressions.cast(ptr.unwrap(), wrapperType);
      JLValue tempVar = mv.getLocalVarAllocator().reserve(wrapperType);
      tempVar.store(mv, castedWrapper);

      JExpr arrayField = Wrappers.arrayField(castedWrapper, getValueType());
      JExpr offsetField = Wrappers.offsetField(castedWrapper);
      
      store(mv, arrayField, offsetField);

    } else if(rhsExpr instanceof FatPtr) {
      FatPtrPair pair = ((FatPtr) rhsExpr).toPair(mv);
      store(mv, pair.getArray(), pair.getOffset());

    } else {
      throw new UnsupportedOperationException("rhs: " + rhsExpr);
    }
  }

  private void store(MethodGenerator mv, JExpr arrayRhs, JExpr offsetRhs) {
    if (!(array instanceof JLValue)) {
      throw new InternalCompilerException(array + " is not an LValue");
    }
    ((JLValue) array).store(mv, arrayRhs);

    // Normally, the offset must also be an LValue, but the exception 
    // is that if both the lhs and rhs are constants, and they are equal
    if (offset instanceof ConstantValue &&
        offset.equals(offsetRhs)) {
      // No assignment neccessary
      return;
    }

    // Otherwise the offset must also be assignable
    if (!(offset instanceof JLValue)) {
      throw new InternalCompilerException(offset + " offset is not an Lvalue");
    }
    ((JLValue) offset).store(mv, offsetRhs);
  }


  public static FatPtrPair nullPtr(ValueFunction valueFunction) {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    JExpr nullArray = Expressions.nullRef(arrayType);
    
    return new FatPtrPair(nullArray);
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

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return this;
  }

  @Override
  public FatPtrPair toPair() {
    return this;
  }

  @Override
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

  public JExpr at(int i) {
    return Expressions.elementAt(array, Expressions.sum(offset, i));
  }
}
