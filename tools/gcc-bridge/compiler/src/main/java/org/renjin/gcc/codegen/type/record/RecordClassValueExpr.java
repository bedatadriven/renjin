package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Addressable;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;

import javax.annotation.Nonnull;


public class RecordClassValueExpr implements SimpleLValue, Addressable {
  
  private final SimpleExpr ref;
  private Expr address;
  
  public RecordClassValueExpr(SimpleExpr ref) {
    this.ref = ref;
    this.address = null;
  }

  public RecordClassValueExpr(SimpleExpr ref, Expr address) {
    this.ref = ref;
    this.address = address;
  }

  public SimpleExpr getRef() {
    return ref;
  }

  @Nonnull
  @Override
  public Type getType() {
    return ref.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    ref.load(mv); 
  }

  @Override
  public void store(MethodGenerator mv, SimpleExpr rhs) {

    if(!rhs.getType().equals(ref.getType())) {
      throw new IllegalArgumentException(String.format("Expected rhs expression of type: %s, found: %s", 
          ref.getType(), rhs.getType()));
    }

    ref.load(mv);
    rhs.load(mv);
    
    mv.invokevirtual(ref.getType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, ref.getType()), false);
  }

  @Override
  public Expr addressOf() {
    if (address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }
}
