package org.renjin.gnur;

import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.ins.GimpleCall;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.call.CallTranslator;
import org.renjin.gcc.translate.call.MallocCallTranslator;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImLValue;
import org.renjin.gcc.translate.expr.NewArrayExpr;
import org.renjin.gcc.translate.type.ImPrimitivePtrType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

/**
 * Translates the R_alloc function into an array constructor
 */
public class RallocTranslator implements CallTranslator {


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
    return ((GimpleFunctionRef) value).getName().equals("R_alloc");
  }


  @Override
  public void writeCall(FunctionContext context, GimpleCall call) {

    if(call.getArguments().size() != 2) {
      throw new UnsupportedOperationException("Expected 2 args for R_alloc()");
    }
    
    if(call.getLhs() == null) {
      // TODO: evaluate args for side effects
      return;
    }

    ImExpr lhs = context.resolveExpr(call.getLhs());
    if(lhs.type() instanceof ImPrimitivePtrType) {
      writeNewArray(context, (ImLValue) lhs, call.getArguments().get(0), call.getArguments().get(1));
    } else {
      throw new UnsupportedOperationException("lhs: " + lhs);
    }
  }

  private void writeNewArray(FunctionContext context, ImLValue lhs, GimpleExpr n, GimpleExpr size) {
    
    ImPrimitivePtrType type = (ImPrimitivePtrType)lhs.type();

    checkSize(type, size);

    JimpleExpr elementCount = context.resolveExpr(n).translateToPrimitive(context, ImPrimitiveType.INT);
    lhs.writeAssignment(context, new NewArrayExpr(type, elementCount));
  }

  private void checkSize(ImPrimitivePtrType type, GimpleExpr sizeExpr) {
    if(sizeExpr instanceof GimpleConstant) {
      int size = ((GimpleConstant) sizeExpr).getNumberValue().intValue();
      if(size != type.baseType().getStorageSizeInBytes()) {
        throw new UnsupportedOperationException("Inconsistent sizes in R_Malloc: size_arg=" + sizeExpr + ", type=" + type.baseType());
      }
    } else {
      throw new UnsupportedOperationException("expected size arg in R_alloc to be constant");
    }
    
  }
}
