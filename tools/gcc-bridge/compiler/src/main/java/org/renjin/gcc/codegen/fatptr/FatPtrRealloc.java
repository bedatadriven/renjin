package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.gcc.runtime.Realloc;

/**
 * Invokes a type-specific realloc() method to enlarge the new array
 */
public class FatPtrRealloc implements Value {

  private FatPtrExpr pointer;
  private Value newLength;
  private Type arrayType;

  public FatPtrRealloc(FatPtrExpr pointer, Value newLength) {
    this.pointer = pointer;
    this.newLength = newLength;
    arrayType = pointer.getArray().getType();
  }

  @Override
  public Type getType() {
    return arrayType;
  }

  @Override
  public void load(MethodGenerator mv) {

    pointer.getArray().load(mv);
    pointer.getOffset().load(mv);
    newLength.load(mv);
    
    mv.invokestatic(Realloc.class, "realloc", Type.getMethodDescriptor(arrayType, arrayType, Type.INT_TYPE, Type.INT_TYPE));
  }
}
