package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.repackaged.asm.Type;


public interface FatPtr extends PtrExpr {

  Type getValueType();

  boolean isAddressable();

  JExpr wrap();
  
  FatPtrPair toPair(MethodGenerator mv);

}
