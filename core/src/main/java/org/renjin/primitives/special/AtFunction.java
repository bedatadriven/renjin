package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Types;
import org.renjin.sexp.*;

public class AtFunction extends SpecialFunction {

  public AtFunction() {
    super("@");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {

    if(call.getArguments().length() != 2) {
      throw new EvalException(call.getArguments().length() + " arguments passed to '@', which requires 2");
    }

    SEXP object = call.getArgument(0);
    SEXP evaluatedObject = context.evaluate(object, rho);

    Symbol slotName = call.getArgument(1);

    return getSlotValue(context, evaluatedObject, slotName);
  }

  /**
   * Accesses the value of a slot from a given object.
   * @param context
   * @param object the evaluated object
   * @param slotName the name of symbol
   * @return
   */
  public static SEXP getSlotValue(Context context, SEXP object, Symbol slotName) {
    if(slotName.getPrintName().equals(".Data")) {
      Environment methodsNamespace = context.getNamespaceRegistry()
          .getNamespace(context, "methods")
          .getNamespaceEnvironment();
      return context.evaluate(FunctionCall.newCall(Symbol.get("getDataPart"), object), methodsNamespace);
    }
    if(!Types.isS4(object)) {
      SEXP className = object.getAttribute(Symbols.CLASS_NAME);
      if(className.length() == 0) {
        throw new EvalException("trying to get slot \"%s\" from an object of a basic class (\"%s\") with no slots",
            slotName.getPrintName(),
            object.getS3Class().getElementAsString(0));
      } else {
        throw new EvalException("trying to get slot \"%s\" from an object (class \"%s\") that is not an S4 object ",
            slotName.getPrintName(),
            className.getElementAsSEXP(0));
      }
    }

    SEXP value = object.getAttribute(slotName);
    if(value == Null.INSTANCE) {
      if (slotName == Symbol.get(".S3Class")) { /* defaults to class(obj) */
        throw new EvalException("not implemented: .S3Class");
        //return R_data_class(obj, FALSE);
      } else if (slotName == Symbols.NAMES && object instanceof ListVector) {
        /* needed for namedList class */
        return value;
      } else {
        throw new EvalException("cannot get slot %s", slotName);
      }
    }
    if(value == Symbols.S4_NULL) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }
}
