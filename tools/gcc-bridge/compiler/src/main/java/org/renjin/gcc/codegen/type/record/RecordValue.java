package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.repackaged.asm.Type;

import static org.renjin.gcc.codegen.expr.Expressions.elementAt;


public class RecordValue implements GSimpleExpr {
  
  private final JExpr ref;
  private GExpr address;
  
  public RecordValue(JExpr ref) {
    this.ref = ref;
    this.address = null;
  }

  public RecordValue(JExpr ref, GExpr address) {
    this.ref = ref;
    this.address = address;
  }

  public Type getJvmType() {
    return ref.getType();
  }
  
  public JExpr getRef() {
    return ref;
  }


  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    JExpr rhsRef;
    if(rhs instanceof RecordValue) {
      rhsRef = ((RecordValue) rhs).unwrap();
    } else if(rhs instanceof RecordUnitPtr) {
      rhsRef = ((RecordUnitPtr) rhs).unwrap();
    } else if(rhs instanceof FatPtrPair) {
      FatPtrPair fatPtrExpr = (FatPtrPair) rhs;
      rhsRef =  Expressions.cast(elementAt(fatPtrExpr.getArray(), fatPtrExpr.getOffset()), getJvmType());
    } else {
      throw new InternalCompilerException("Cannot assign " + rhs + " to " + this);
    }

    ref.load(mv);
    rhsRef.load(mv);
    
    mv.invokevirtual(ref.getType(), "set", Type.getMethodDescriptor(Type.VOID_TYPE, ref.getType()), false);
  }

  @Override
  public GExpr addressOf() {
    if (address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }

  @Override
  public JExpr unwrap() {
    return ref;
  }
}
