package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.call.MethodRef;
import org.renjin.gcc.translate.types.FunPtrJimpleType;

import java.util.List;

public class FunPtrVar extends Variable {

  private String jimpleName;
  private FunctionPointerType type;
  private FunctionContext context;
  private JimpleType jimpleType;

  public FunPtrVar(FunctionContext context, String gimpleName, FunctionPointerType type) {
    this.context = context;
    this.jimpleName = Jimple.id(gimpleName);
    this.type = type;
    this.jimpleType = new FunPtrJimpleType(context.getTranslationContext().getFunctionPointerInterfaceName(type));

    context.getBuilder().addVarDecl(jimpleType, jimpleName);
  }

  @Override
  public void assign(GimpleOp op, List<GimpleExpr> operands) {
    switch (op) {
      case INTEGER_CST:
        assignNull(operands.get(0));
        break;

      case NOP_EXPR:
        assignPointer(operands.get(0));
        break;

      case ADDR_EXPR:
        assignPointer(operands.get(0));
        break;

      default:
        throw new UnsupportedOperationException(op + " " + operands);
    }
  }

  private void assignPointer(GimpleExpr param) {
    if(param instanceof GimpleExternal) {
      assignNewInvoker((GimpleExternal) param);

//    } else if(param instanceof GimpleVar) {
//      assignExistingPointer((GimpleVar) param);

    } else {
      throw new UnsupportedOperationException(param.toString());
    }
  }


  private void assignNewInvoker(GimpleExternal param) {
    MethodRef method = context.getTranslationContext().resolveMethod(((GimpleExternal) param).getName());
    JimpleType invokerType = context.getTranslationContext().getInvokerType(method);
    String ptr = context.declareTemp(invokerType);
    context.getBuilder().addStatement(ptr + " = new " + invokerType);
    context.getBuilder().addStatement("specialinvoke " + ptr + ".<" + invokerType + ": void <init>()>()");
    context.getBuilder().addStatement(jimpleName + " = " + ptr);
  }

  private void assignNull(GimpleExpr gimpleExpr) {
    if(!(gimpleExpr instanceof GimpleConstant)) {
      throw new UnsupportedOperationException("Expected GimpleConstant, got " + gimpleExpr);
    }
    Object value = ((GimpleConstant) gimpleExpr).getValue();
    if(!(value instanceof Number) || ((Number) value).intValue() != 0) {
      throw new UnsupportedOperationException("Can only assign 0 to function pointer");
    }
    context.getBuilder().addStatement(Jimple.id(jimpleName) + " = null");
  }

  public JimpleExpr getJimpleVariable() {
    return new JimpleExpr(jimpleName);
  }
  
  @Override
  public JimpleExpr returnExpr() {
    return new JimpleExpr(jimpleName);
  }
}
