package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.RefPtrExpr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public interface FatPtr extends GExpr {

  Type getValueType();

  boolean isAddressable();

  JExpr wrap();
  
  FatPtrPair toPair(MethodGenerator mv);
  
  FatPtrPair toPair();
}
