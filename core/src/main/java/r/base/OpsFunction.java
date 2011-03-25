/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package r.base;

import r.lang.BuiltinFunction;

public class OpsFunction extends BuiltinFunction {

  public OpsFunction(String name) {
    super(name, Ops.class);
  }

//  @Override
//  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList args) {
//    return super.apply(context, rho, call, arguments);
//  }
//
//
//  attribute_hidden
//  int DispatchGroup(const char* group, SEXP call, SEXP op, SEXP args, SEXP rho,
//                    SEXP *ans) {
//    int i, j, nargs, lwhich, rwhich, set;
//    SEXP lclass, s, t, m, lmeth, lsxp, lgr, newrho;
//    SEXP rclass, rmeth, rgr, rsxp, value;
//    char lbuf[512], rbuf[512], generic[128];
//
//    boolean useS4 = true, isOps = false;
//
//    /* pre-test to avoid string computations when there is nothing to
//     dispatch on because either there is only one argument and it
//     isn't an object or there are two or more arguments but neither
//     of the first two is an object -- both of these cases would be
//     rejected by the code following the string examination code
//     below */
////	if (args != R_NilValue && !isObject(CAR(args)) && (CDR(args) == R_NilValue
////			|| !isObject(CADR(args))))
////		return 0;
//
//    //isOps = strcmp(group, "Ops") == 0;
//    isOps = true;
//
////    /* try for formal method */
////    if (length(args) == 1 && !IS_S4_OBJECT(CAR(args)))
////      useS4 = FALSE;
////    if (length(args) == 2 && !IS_S4_OBJECT(CAR(args)) && !IS_S4_OBJECT(CADR(
////        args)))
////      useS4 = FALSE;
////    if (useS4) {
////      /* Remove argument names to ensure positional matching */
////      if (isOps)
////        for (s = args; s != R_NilValue; s = CDR(s))
////          SET_TAG(s, R_NilValue);
////      if (R_has_methods(op) && (value = R_possible_dispatch(call, op, args,
////          rho, FALSE))) {
////        *ans = value;
////        return 1;
////      }
////      /* else go on to look for S3 methods */
////    }
//
//    /* check whether we are processing the default method */
//    if (isSymbol(CAR(call))) {
////      if (strlen(CHAR(PRINTNAME(CAR(call)))) >= 512)
////        error(_("call name too long in '%s'"), CHAR(PRINTNAME(CAR(call))));
//
//      String symbolName = ((Symbol)CAR(call)).getPrintName();
//      //sprintf(lbuf, "%s", CHAR(PRINTNAME(CAR(call))) );
//      int pt = symbolName.indexOf('.');
//      pt = symbolName.indexOf('.', pt);
//
//
//      if (pt != -1 && symbolName.substring(pt).equals("default")) {
//        return 0;
//      }
//    }
//
//    if (isOps)
//      nargs = args.length();
//    else
//      nargs = 1;
//
////    if (nargs == 1 && !isObject(CAR(args)))
////      return 0;
////
////    if (!isObject(CAR(args)) && !isObject(CADR(args)))
////      return 0;
//
//    if (strlen(PRIMNAME(op)) >= 128)
//      error(_("generic name too long in '%s'"), PRIMNAME(op));
//    sprintf(generic, "%s", PRIMNAME(op) );
//
//    lclass = IS_S4_OBJECT(CAR(args)) ? R_data_class2(CAR(args)) : getAttrib(
//        CAR(args), R_ClassSymbol);
//
//    if (nargs == 2)
//      rclass = IS_S4_OBJECT(CADR(args)) ? R_data_class2(CADR(args))
//          : getAttrib(CADR(args), R_ClassSymbol);
//    else
//      rclass = R_NilValue;
//
//    lsxp = R_NilValue;
//    lgr = R_NilValue;
//    lmeth = R_NilValue;
//    rsxp = R_NilValue;
//    rgr = R_NilValue;
//    rmeth = R_NilValue;
//
//    findmethod(lclass, group, generic, &lsxp, &lgr, &lmeth, &lwhich, lbuf, rho);
//    PROTECT(lgr);
//    if (isFunction(lsxp) && IS_S4_OBJECT(CAR(args)) && lwhich > 0
//        && isBasicClass(translateChar(STRING_ELT(lclass, lwhich)))) {
//      /* This and the similar test below implement the strategy
//        for S3 methods selected for S4 objects.  See ?Methods */
//      value = CAR(args);
//      if (NAMED(value))
//        SET_NAMED(value, 2);
//      value = R_getS4DataSlot(value, S4SXP); /* the .S3Class obj. or NULL*/
//      if (value != R_NilValue) /* use the S3Part as the inherited object */
//        SETCAR(args, value);
//    }
//
//    if (nargs == 2)
//      findmethod(rclass, group, generic, &rsxp, &rgr, &rmeth, &rwhich, rbuf,
//        rho);
//    else
//    rwhich = 0;
//
//    if (isFunction(rsxp) && IS_S4_OBJECT(CADR(args)) && rwhich > 0
//        && isBasicClass(translateChar(STRING_ELT(rclass, rwhich)))) {
//      value = CADR(args);
//      if (NAMED(value))
//        SET_NAMED(value, 2);
//      value = R_getS4DataSlot(value, S4SXP);
//      if (value != R_NilValue)
//        SETCADR(args, value);
//    }
//
//    PROTECT(rgr);
//
//    if (!isFunction(lsxp) && !isFunction(rsxp)) {
//      UNPROTECT(2);
//      return 0; /* no generic or group method so use default*/
//    }
//
//    if (lsxp != rsxp) {
//      if (isFunction(lsxp) && isFunction(rsxp)) {
//        /* special-case some methods involving difftime */
//        const char *lname = CHAR(PRINTNAME(lmeth)), *rname =
//            CHAR(PRINTNAME(rmeth));
//        if (streql(rname, "Ops.difftime") && (streql(lname, "+.POSIXt")
//            || streql(lname, "-.POSIXt") || streql(lname, "+.Date")
//            || streql(lname, "-.Date")))
//          rsxp = R_NilValue;
//        else if (streql(lname, "Ops.difftime")
//            && (streql(rname, "+.POSIXt") || streql(rname, "+.Date")))
//          lsxp = R_NilValue;
//        else {
//          warning(_("Incompatible methods (\"%s\", \"%s\") for \"%s\""),
//              lname, rname, generic);
//          UNPROTECT(2);
//          return 0;
//        }
//      }
//      /* if the right hand side is the one */
//      if (!isFunction(lsxp)) { /* copy over the righthand stuff */
//        lsxp = rsxp;
//        lmeth = rmeth;
//        lgr = rgr;
//        lclass = rclass;
//        lwhich = rwhich;
//        strcpy(lbuf, rbuf);
//      }
//    }
//
//    /* we either have a group method or a class method */
//
//    PROTECT(newrho = allocSExp(ENVSXP));
//    PROTECT(m = allocVector(STRSXP,nargs));
//    s = args;
//    for (i = 0; i < nargs; i++) {
//      t = IS_S4_OBJECT(CAR(s)) ? R_data_class2(CAR(s)) : getAttrib(CAR(s),
//          R_ClassSymbol);
//      set = 0;
//      if (isString(t)) {
//        for (j = 0; j < length(t); j++) {
//          if (!strcmp(translateChar(STRING_ELT(t, j)), translateChar(
//              STRING_ELT(lclass, lwhich)))) {
//            SET_STRING_ELT(m, i, mkChar(lbuf));
//            set = 1;
//            break;
//          }
//        }
//      }
//      if (!set)
//        SET_STRING_ELT(m, i, R_BlankString);
//      s = CDR(s);
//    }
//
//    defineVar(install(".Method"), m, newrho);
//    UNPROTECT(1);
//    PROTECT(t = mkString(generic));
//    defineVar(install(".Generic"), t, newrho);
//    UNPROTECT(1);
//    defineVar(install(".Group"), lgr, newrho);
//    set = length(lclass) - lwhich;
//    PROTECT(t = allocVector(STRSXP, set));
//    for (j = 0; j < set; j++)
//      SET_STRING_ELT(t, j, duplicate(STRING_ELT(lclass, lwhich++)));
//    defineVar(install(".Class"), t, newrho);
//    UNPROTECT(1);
//    defineVar(install(".GenericCallEnv"), rho, newrho);
//    defineVar(install(".GenericDefEnv"), R_BaseEnv, newrho);
//
//    PROTECT(t = LCONS(lmeth, CDR(call)));
//
//    /* the arguments have been evaluated; since we are passing them */
//    /* out to a closure we need to wrap them in promises so that */
//    /* they get duplicated and things like missing/substitute work. */
//
//    PROTECT(s = promiseArgs(CDR(call), rho));
//    if (length(s) != length(args))
//      error(_("dispatch error in group dispatch"));
//    for (m = s; m != R_NilValue; m = CDR(m), args = CDR(args)) {
//      SET_PRVALUE(CAR(m), CAR(args));
//      /* ensure positional matching for operators */
//      if (isOps)
//        SET_TAG(m, R_NilValue);
//    }
//
//    *ans = applyClosure(t, lsxp, s, rho, newrho);
//    UNPROTECT(5);
//    return 1;
//  }

}
