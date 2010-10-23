/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang;

/**
 * A specialized {@code ListExp} used for storing 
 */
public class LangExp extends ListExp {
  public static final int TYPE_CODE = 6;
  public static final String TYPE_NAME = "language";

  public LangExp(SEXP value, ListExp nextNode) {
    super(value, nextNode);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public SEXP evaluate(EnvExp rho) {

    FunExp functionExpr = (FunExp) getFunction().evaluate(rho);

    return functionExpr.apply(this, getArguments(), rho);

//    SEXP op;
//    if (TYPEOF(CAR(this)) == Type.SYMSXP)
//      /* This will throw an error if the function is not found */
//      PROTECT(op = rho.findFun(CAR(this)));
//    else
//      PROTECT(op = CAR(this).evaluate(rho));
//
////    if(RTRACE(op) && R_current_trace_state()) {
////      Rprintf("trace: ");
////      PrintValue(e);
////    }
//    if (TYPEOF(op) == Type.SPECIALSXP) {
//      int save = R_PPStackTop, flag = PRIMPRINT(op);
//      PROTECT(CDR(e));
//      //R_Visible = flag != 1;
//      tmp = PRIMFUN(op) (e, op, CDR(e), rho);
//
//      if (flag < 2)
//        R_Visible = flag != 1;
//    }
//    else if (TYPEOF(op) == Type.BUILTINSXP) {
//      int save = R_PPStackTop, flag = PRIMPRINT(op);
//      RCNTXT cntxt;
//      PROTECT(tmp = evalList(CDR(e), rho, op));
//      if (flag < 2) R_Visible = flag != 1;
//      /* We used to insert a context only if profiling,
//            but helps for tracebacks on .C etc. */
//      if (R_Profiling || (PPINFO(op).kind == PP_FOREIGN)) {
//        begincontext(&cntxt, CTXT_BUILTIN, e,
//            R_BaseEnv, R_BaseEnv, R_NilValue, R_NilValue);
//        tmp = PRIMFUN(op) (e, op, tmp, rho);
//        endcontext(&cntxt);
//      } else {
//        tmp = PRIMFUN(op) (e, op, tmp, rho);
//      }
//      if (flag < 2) R_Visible = flag != 1;
//    }
//    else if (TYPEOF(op) == Type.CLOSXP) {
//      PROTECT(tmp = promiseArgs(CDR(e), rho));
//      tmp = applyClosure(e, op, tmp, rho, R_BaseEnv);
//    }
//    else
//      throw new EvalException(this, "attempt to apply non-function");

  }

  public static SEXP fromListExp(ListExp listExp) {
    return new LangExp(listExp.value, listExp.nextNode);
  }

  public SEXP getFunction() {
    return value;
  }

  public ListExp getArguments() {
    return nextNode;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);

  }
}
