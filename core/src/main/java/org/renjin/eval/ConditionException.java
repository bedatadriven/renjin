package org.renjin.eval;

import org.renjin.sexp.SEXP;

/**
 * Exception to pass control out of a block where a condition was signaled
 */
public class ConditionException extends RuntimeException {

    private SEXP condition;
    private Context handlerContext;
    private SEXP handler;

    public ConditionException(SEXP condition, Context handlerContext, SEXP handler) {
        this.condition = condition;
        this.handlerContext = handlerContext;
        this.handler = handler;
    }

    public SEXP getCondition() {
        return condition;
    }

    public Context getHandlerContext() {
        return handlerContext;
    }

    public SEXP getHandler() {
        return handler;
    }
}
