package org.renjin.gcc.translate.call;


import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.expr.NewArrayExpr;
import org.renjin.gcc.translate.type.ImPrimitiveArrayPtrType;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * Translates a call to malloc
 */
public class MallocCallTranslator extends NamedCallTranslator {

  public MallocCallTranslator(String functionName) {
    super(functionName);
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

    } else if(lhs.type() instanceof ImPrimitiveArrayPtrType) {
      writeNewPrimitiveArrayPtr(context, lhs, sizeArg);

    } else {
      throw new UnsupportedOperationException("type: " + lhs.type());
    }
  }

  private void writeNewPrimitiveArray(FunctionContext context, ImExpr lhs, ImExpr sizeArg) {

    ImPrimitivePtrType type = (ImPrimitivePtrType) lhs.type();

    JimpleExpr elementCount = getNumElementsExpr(context, sizeArg, type.getBaseType());

    // assign to our lhs
    ((ImLValue)lhs).writeAssignment(context, new NewArrayExpr(type, elementCount));
  }


  private void writeNewPrimitiveArrayPtr(FunctionContext context, ImExpr lhs, ImExpr sizeArg) {

    ImPrimitiveArrayPtrType type = (ImPrimitiveArrayPtrType) lhs.type();

    JimpleExpr elementCount = getNumElementsExpr(context, sizeArg, type.baseType().componentType());

    // assign to our lhs
    ((ImLValue)lhs).writeAssignment(context, new NewArrayExpr(type.baseType().componentType().pointerType(),
        elementCount));
  }


  private JimpleExpr getNumElementsExpr(FunctionContext context, ImExpr sizeArg, ImPrimitiveType type) {
    // malloc is given a size in bytes, we need to divide by the underlying storage
    // size to get the number of elements to allocate

    // get the argument as a primitive expression that evaluates to number of bytes
    JimpleExpr byteSizeExpr = sizeArg.translateToPrimitive(context, ImPrimitiveType.INT);

    // calculate number of elements
    JimpleExpr elementCount = new JimpleExpr(context.declareTemp(JimpleType.INT));
    context.getBuilder().addAssignment(elementCount, JimpleExpr.binaryInfix("/", byteSizeExpr,
      JimpleExpr.integerConstant(type.getStorageSizeInBytes())));
    return elementCount;
  }

  protected ImExpr computeSize(FunctionContext context, GimpleCall call) {
    if(call.getArguments().size() != 1) {
      throw new UnsupportedOperationException("expected call to malloc with 1 arg");
    }
    return context.resolveExpr(call.getArguments().get(0));
  }

}
