package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.expr.NotAddressableException;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * A FatPtr expression that is stored as an instance of an Object/Double/LongPtr, etc. 
 */
public class WrappedFatPtrExpr implements FatPtr {

  private ValueFunction valueFunction;
  private JLValue ref;

  public WrappedFatPtrExpr(ValueFunction valueFunction, JLValue paramExpr) {
    this.valueFunction = valueFunction;
    this.ref = paramExpr;
  }


  public JExpr getArray() {
    return Wrappers.arrayField(ref);
  }

  public JExpr getOffset() {
    return Wrappers.offsetField(ref);
  }

  @Override
  public Type getValueType() {
    return valueFunction.getValueType();
  }

  @Override
  public boolean isAddressable() {
    return false;
  }

  @Override
  public JExpr wrap() {
    return ref;
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return toPair();
  }

  @Override
  public FatPtrPair toPair() {
    return new FatPtrPair(valueFunction, getArray(), getOffset());
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    if(rhs instanceof FatPtr) {
      ref.store(mv, ((FatPtr) rhs).wrap());

    } else {
      throw new UnsupportedOperationException("TODO: rhs = " + rhs.getClass().getName());
    }
  }

  @Override
  public GExpr addressOf() {
    throw new NotAddressableException();
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    getArray().load(mv);
    mv.ifnull(label);
  }

  @Override
  public GExpr valueOf() {
    return valueFunction.dereference(this);
  }

  public JLValue valueExpr() {
    return new JLValue() {
      @Nonnull
      @Override
      public Type getType() {
        return valueFunction.getValueType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        ref.load(mv);
        if(ref.getType().equals(Type.getType(ObjectPtr.class))) {
          mv.invokevirtual(ref.getType(), "get", Type.getMethodDescriptor(Type.getType(Object.class)), false);
          mv.checkcast(valueFunction.getValueType());

        } else {
          mv.invokevirtual(ref.getType(), "get", Type.getMethodDescriptor(valueFunction.getValueType()), false);
        }
      }

      @Override
      public void store(MethodGenerator mv, JExpr expr) {
        ref.load(mv);
        expr.load(mv);
        if(ref.getType().equals(Type.getType(ObjectPtr.class))) {
          mv.invokevirtual(ref.getType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
        } else {
          mv.invokevirtual(ref.getType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, valueFunction.getValueType()), false);
        }
      }
    };
  }
}
