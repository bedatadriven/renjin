package org.renjin.gcc.translate.expr;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.call.MethodRef;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * An expression that evaluates to a function
 */
public class ImFunctionExpr extends AbstractImExpr {
  
  private MethodRef methodRef;

  public ImFunctionExpr(MethodRef methodRef) {
    super();
    this.methodRef = methodRef;
  }

  @Override
  public ImPrimitiveType type() {
    throw new UnsupportedOperationException();
  }

  public MethodRef getMethodRef() {
    return methodRef;
  }

  @Override
  public ImExpr addressOf() {
    return new Pointer();
  }


  public class Pointer extends AbstractImExpr implements ImFunctionPtrExpr {

    @Override
    public ImPrimitiveType type() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ImExpr memref() {
      return ImFunctionExpr.this;
    }

    @Override
    public JimpleExpr invokerReference(FunctionContext context) {
      JimpleType invokerType = context.getTranslationContext().getInvokerType(getMethodRef());
      String ptr = context.declareTemp(invokerType);
      context.getBuilder().addStatement(ptr + " = new " + invokerType);
      context.getBuilder().addStatement("specialinvoke " + ptr + ".<" + invokerType + ": void <init>()>()");
      return new JimpleExpr(ptr);
    }

    @Override
    public String toString() {
      return "&" + methodRef.getMethodName();
    }
    
  }
  
}

