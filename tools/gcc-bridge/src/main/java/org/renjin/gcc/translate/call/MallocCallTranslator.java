package org.renjin.gcc.translate.call;


import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.*;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * Translates a call to malloc
 */
public class MallocCallTranslator implements CallTranslator {
  
  private final String functionName;

  public MallocCallTranslator(String functionName) {
    this.functionName = functionName;
  }


  @Override
  public boolean accept(GimpleCall call) {
    GimpleExpr functionExpr = call.getFunction();
    if(!(functionExpr instanceof GimpleAddressOf)) {
      return false;
    }
    GimpleExpr value = ((GimpleAddressOf) functionExpr).getValue();
    if (!(value instanceof GimpleFunctionRef)) {
      return false;
    }
    if (!(value instanceof GimpleFunctionRef)) {
      return false;
    }
    return ((GimpleFunctionRef) value).getName().equals(functionName);

  }

  @Override
  public void writeCall(FunctionContext context, GimpleCall call) {
    if(call.getLhs() == null) {
      // no effect
      return;
    }

    ImExpr sizeArg = computeSize(context, call);
    ImExpr lhs = context.resolveExpr(call.getLhs());

    if(lhs.type() instanceof ImPrimitivePtrType) {
      writeNewPrimitiveArray(context, lhs, sizeArg);

    } else {
      throw new UnsupportedOperationException("type: " + lhs.type());
    }
  }

  private void writeNewPrimitiveArray(FunctionContext context, ImExpr lhs, ImExpr sizeArg) {

    ImPrimitivePtrType type = (ImPrimitivePtrType) lhs.type();

    // malloc is given a size in bytes, we need to divide by the underlying storage
    // size to get the number of elements to allocate

    // get the argument as a primitive expression that evaluates to number of bytes
    JimpleExpr byteSizeExpr = sizeArg.translateToPrimitive(context, ImPrimitiveType.INT);

    // calculate number of elements
    JimpleExpr elementCount = new JimpleExpr(context.declareTemp(JimpleType.INT));
    context.getBuilder().addAssignment(elementCount, JimpleExpr.binaryInfix("/", byteSizeExpr,
        JimpleExpr.integerConstant(type.baseType().getStorageSizeInBytes())));

    // assign to our lhs
    ((ImLValue)lhs).writeAssignment(context,
      new NewArrayExpr(type, elementCount));
  }

  protected ImExpr computeSize(FunctionContext context, GimpleCall call) {
    if(call.getArguments().size() != 1) {
      throw new UnsupportedOperationException("expected call to malloc with 1 arg");
    }
    return context.resolveExpr(call.getArguments().get(0));
  }

}
