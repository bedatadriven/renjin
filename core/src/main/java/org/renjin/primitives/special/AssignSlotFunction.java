package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.methods.Methods;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;
import polyglot.ast.Eval;


public class AssignSlotFunction extends SpecialFunction {

    public AssignSlotFunction() {
        super("@<-");
    }

    @Override
    public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

        if(args.length() != 3) {
            throw new EvalException("Expected three arguments");
        }
        SEXP object = context.evaluate(args.getElementAsSEXP(0), rho);
        if(object == Null.INSTANCE) {
            throw new EvalException("Cannot set slots on the NULL object");
        }

        String slotName;
        SEXP which = args.getElementAsSEXP(1);
        if(which instanceof Symbol) {
            slotName = ((Symbol) which).getPrintName();
        } else if(which instanceof StringVector) {
            slotName = ((StringVector) which).getElementAsString(0);
        } else {
            throw new EvalException("invalid type '%s' for slot name", which.getTypeName());
        }

        SEXP unevaluatedValue = args.getElementAsSEXP(2);

        // Try to dispatch to an override, but repackage the name as a character
        PairList repackagedArgs = PairList.Node.fromArray(object, new StringArrayVector(slotName), unevaluatedValue);

        SEXP genericResult = S3.tryDispatchFromPrimitive(context, rho, call, getName(), object, repackagedArgs);
        if(genericResult != null) {
            return genericResult;
        }

        // Nope, we're going to use the default version so we need to evaluate the value
        SEXP rhs = context.evaluate(args.getElementAsSEXP(2));

        return Methods.R_set_slot(context, object, slotName, rhs);
    }

}
