package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;


public class BitFieldExpr implements JLValue {

  private Type ownerClass;
  private final JExpr instance;
  private final String fieldName;
  private int offset;
  private int size;

  public BitFieldExpr(Type ownerClass, JExpr instance, String fieldName, int offset, int size) {
    this.ownerClass = ownerClass;
    this.instance = instance;
    this.fieldName = fieldName;
    this.offset = offset;
    this.size = size;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BYTE_TYPE;
  }
  
  private byte mask() {
    int mask = (1 << size) - 1;
    return (byte)(offset >>> mask);
  }
  
  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    instance.load(mv);
    mv.getfield(ownerClass.getInternalName(), fieldName, Type.BYTE_TYPE.getDescriptor());
    
    // shift from our range
    if(offset != 0) {
      mv.iconst(offset);
      mv.ushr(Type.BYTE_TYPE);
    }
    
    // zero out values outside our range
    mv.iconst((1 << size) - 1);
    mv.and(Type.BYTE_TYPE);
  }

  @Override
  public void store(MethodGenerator mv, JExpr rhs) {
    Preconditions.checkArgument(rhs.getType().equals(Type.BYTE_TYPE));

    // Given a bit value like 
    //    V1: 1010 0000
    // And a current value of 
    //    V0: 0110 1101
    //
    // We need to set the field to the value 
    // V0 | (V1 & MASK)

    instance.load(mv);
    mv.dup();

    // Load the original value
    mv.getfield(ownerClass.getInternalName(), fieldName, Type.BYTE_TYPE.getDescriptor());

    // Load the new value
    rhs.load(mv);

    // Zero out any bits outside our bit range
    mv.iconst((1 << size) - 1);
    mv.and(Type.BYTE_TYPE);

    // Shift right to our range
    if(offset != 0) {
      mv.iconst(offset);
      mv.shl(Type.BYTE_TYPE);
    }
    
    // Or with the existing value
    mv.or(Type.BYTE_TYPE);

    // Store to the field
    mv.putfield(ownerClass.getInternalName(), fieldName, Type.BYTE_TYPE.getDescriptor());
  }

}
