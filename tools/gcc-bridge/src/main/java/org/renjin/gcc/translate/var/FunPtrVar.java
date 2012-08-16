package org.renjin.gcc.translate.var;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleExternal;
import org.renjin.gcc.gimple.type.FunctionPointerType;
import org.renjin.gcc.jimple.Jimple;
import org.renjin.gcc.jimple.JimpleMethodRef;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;

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
    this.jimpleType = new JimpleType(context.getTranslationContext().getFunctionPointerInterfaceName(type));

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
    JimpleMethodRef method = context.getTranslationContext().resolveMethod(((GimpleExternal) param).getName());
    String invokerClass = context.getTranslationContext().getInvokerClass(method);
    String ptr = context.declareTemp(new JimpleType(invokerClass));
    context.getBuilder().addStatement(ptr + " = new " + invokerClass);
    context.getBuilder().addStatement("specialinvoke " + ptr + ".<" + invokerClass + ": void <init>()>()");
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


}
