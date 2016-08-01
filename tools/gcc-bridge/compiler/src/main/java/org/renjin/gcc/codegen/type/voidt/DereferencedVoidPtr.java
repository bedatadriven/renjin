package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.repackaged.asm.Type;

public class DereferencedVoidPtr extends VoidPtr {

  private JExpr array;
  private JExpr offset;

  public DereferencedVoidPtr(JExpr array, JExpr offset) {
    super(Expressions.elementAt(array, offset), new FatPtrPair(array, offset));
    this.array = array;
    this.offset = offset;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {

    if(rhs instanceof VoidPtr) {
      // Need to do some runtime casting in case 
      // our array is not an Object[] but rather a DoublePtr[] for example

      String assignDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, 
          Type.getType("[Ljava/lang/Object;"), 
          Type.INT_TYPE, 
          Type.getType(Object.class));
      
      JExpr call = Expressions.staticMethodCall(Type.getType(org.renjin.gcc.runtime.VoidPtr.class),
          "assign", assignDescriptor,
          array, offset, ((VoidPtr) rhs).unwrap());
      
      call.load(mv);

    } else {
      super.store(mv, rhs); 
    }
  }
}
