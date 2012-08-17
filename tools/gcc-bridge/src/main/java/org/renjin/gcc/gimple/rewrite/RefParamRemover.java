package org.renjin.gcc.gimple.rewrite;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;

/**
 * Converts, when possible, parameters passed by reference to pointers
 * passed by value
 */
public class RefParamRemover {

  public void apply(GimpleFunction function) {

    for(GimpleParameter param : function.getParameters()) {
      if(isNumericPtr(param)) {

      }
    }


  }

  private boolean isNumericPtr(GimpleParameter param) {
    return param.getType() instanceof PointerType && ((PointerType) param.getType()).getInnerType() instanceof PrimitiveType;
  }

}
