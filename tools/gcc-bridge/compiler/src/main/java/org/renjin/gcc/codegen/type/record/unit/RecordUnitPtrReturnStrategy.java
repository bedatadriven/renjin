package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;

public class RecordUnitPtrReturnStrategy implements ReturnStrategy {
  private Type jvmType;

  public RecordUnitPtrReturnStrategy(Type jvmType) {
    this.jvmType = jvmType;
  }

  @Override
  public Type getType() {
    return jvmType;
  }

  @Override
  public JExpr marshall(GExpr expr) {
    return ((RecordUnitPtr) expr).unwrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    if(lhsTypeStrategy instanceof RecordUnitPtrStrategy) {
      RecordUnitPtrStrategy lhsUnitPtrStrategy = (RecordUnitPtrStrategy) lhsTypeStrategy;
      return new RecordUnitPtr(Expressions.cast(returnValue, lhsUnitPtrStrategy.getJvmType()));

    } else if(lhsTypeStrategy instanceof RecordClassTypeStrategy) {
      // In some cases, when you have a function like this:
      //    MyClass& do_something(MyClass& x);
      //
      // and an assignment like this:
      //
      //    MyClass x = do_something(y);
      //
      // GCC does not generate an intermediate pointer value and a mem_ref like you
      // would expect. I can't seem to reproduce this in a test case, so here is a workaround:
      RecordClassTypeStrategy lhsValueTypeStrategy = (RecordClassTypeStrategy) lhsTypeStrategy;
      return new RecordUnitPtr(Expressions.cast(returnValue, lhsValueTypeStrategy.getJvmType()));
    
    } else {
      throw new UnsupportedOperationException(
          String.format("Unsupported cast from return value %s to record unit pointer [%s]", 
              lhsTypeStrategy.getClass().getName(), jvmType));
    }
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.nullRef(jvmType);
  }
}
