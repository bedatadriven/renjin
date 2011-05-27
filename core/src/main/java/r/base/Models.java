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

import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;

public class Models {


  @Primitive("~")
  public static SEXP tilde(Context context, Environment rho, FunctionCall call) {
    PairList.Builder attributes = PairList.Node.newBuilder();
    attributes.add(Symbol.CLASS, new StringVector("formula"));
    attributes.add(Symbol.DOT_ENVIRONMENT, rho);

    return new FunctionCall(call.getFunction(), call.getArguments(),
        attributes.build());

  }

  @Primitive("terms.formula")
  public static SEXP termsFormula(@Current Context context, FunctionCall x, SEXP specials, SEXP data, boolean keepOrder,
                                  boolean allowDotAsName) {

    if(specials != Null.INSTANCE) {
      throw new EvalException("specials != NULL is not supported");
    }

    // define attibutes
    ListVector.Builder attributes = new ListVector.Builder();
    attributes.add("variables", new ListVector());
    attributes.add("factors", new IntVector());
    attributes.add("term.labels", new StringVector());
    attributes.add("order", new IntVector());
    attributes.add("intercept", new IntVector(1));
    attributes.add("response", new IntVector(0));
    attributes.add(".Environment", context.getEnvironment().getGlobalEnvironment() );

    // create an new Function Call
    FunctionCall copy = x.clone();
    return copy.setAttributes(attributes.build());
  }

  @Primitive("model.frame")
  public static SEXP modelFrame(
      @Current Context context,
      @Current Environment rho,
      SEXP terms,
      StringVector row_names,
      ListVector variables,
      Vector varnames,
      Vector dots,
      Vector dotnames,
      SEXP subset,
      SEXP naAction) {

    int i, j, nr, nc;
    int nvars, ndots, nactualdots;

    /* Argument Sanity Checks */
    nvars = variables.length();
    if (variables.length() != varnames.length()) {
      throw new EvalException("number of variables != number of variable names");
    }
    if (dots != Null.INSTANCE && !(dots instanceof ListVector)) {
      throw new EvalException("invalid extra variables");
    }
    if ((ndots = dots.length()) != dotnames.length()) {
      throw new EvalException("number of variables != number of variable names");
    }
    if ( ndots != 0 && !(dotnames instanceof StringVector)) {
      throw new EvalException("invalid extra variable names");
    }

    /*  check for NULL extra arguments -- moved from interpreted code */

    nactualdots = 0;
    for (i = 0; i < ndots; i++) {
        if (dots.getElementAsSEXP(i) != Null.INSTANCE) {
          nactualdots++;
        }
    }

    /* Assemble the base data frame. */
    ListVector.Builder data = new ListVector.Builder();
    StringVector.Builder names = new StringVector.Builder();

    for (i = 0; i < nvars; i++) {
        data.addFrom(variables, i);
        names.addFrom(varnames, i);
    }
    for (i = 0,j = 0; i < ndots; i++) {
        String ss;
        if (dots.getElementAsSEXP(i) == Null.INSTANCE) {
          continue;
        }
        ss = "(" + ((StringVector)dotnames).getElementAsString(i) + ")";
        data.setFrom(nvars+j, dots, i);
        names.set(nvars+j, new StringVector(ss));
        j++;
    }
    data.setAttribute(Symbol.NAMES, names.build());

    /* Sanity checks to ensure that the the answer can become */
    /* a data frame.  Be deeply suspicious here! */

    nc = data.length();
    nr = 0;                     /* -Wall */
    if (nc > 0) {
      throw new UnsupportedOperationException("todo");
//        nr = nrows(  _ELT(data, 0));
//        for (i = 0; i < nc; i++) {
//            ans = VECTOR_ELT(data, i);
//            switch(TYPEOF(ans)) {
//            case LGLSXP:
//            case INTSXP:
//            case REALSXP:
//            case CPLXSXP:
//            case STRSXP:
//            case RAWSXP:
//                break;
//            default:
//                error(_("invalid type (%s) for variable '%s'"),
//                      type2char(TYPEOF(ans)),
//                      translateChar(STRING_ELT(names, i)));
//            }
//            if (nrows(ans) != nr)
//                error(_("variable lengths differ (found for '%s')"),
//                      translateChar(STRING_ELT(names, i)));
//        }
    } else {
      nr = row_names.length();
    }

    /* Turn the data "list" into a "data.frame" */
    /* so that subsetting methods will work. */
    /* To do this we must attach "class"  and */
    /* "row.names" attributes */

    data.setAttribute(Symbol.CLASS, new StringVector("data.frame"));
    if (row_names.length() == nr) {
        data.setAttribute(Symbol.ROW_NAMES, row_names);
    } else {
        throw new UnsupportedOperationException("todo");
        /*
        PROTECT(row_names = allocVector(INTSXP, nr));
        for (i = 0; i < nr; i++) INTEGER(row_names)[i] = i+1; */
//        PROTECT(row_names = allocVector(INTSXP, 2));
//        INTEGER(row_names)[0] = NA_INTEGER;
//        INTEGER(row_names)[1] = nr;
//        setAttrib(data, R_RowNamesSymbol, row_names);
//        UNPROTECT(1);
    }

    /* Do the subsetting, if required. */
    /* Need to save and restore 'most' attributes */

    if (subset != Null.INSTANCE) {
      throw new UnsupportedOperationException("todo");
//        PROTECT(tmp=install("[.data.frame"));
//        PROTECT(tmp=LCONS(tmp,list4(data,subset,R_MissingArg,mkFalse())));
//        data = eval(tmp, rho);
//        UNPROTECT(2);
    }

    /* finally, we run na.action on the data frame */
    /* usually, this will be na.omit */

//    if (naAction != Null.INSTANCE) {
//        /* some na.actions need this to distinguish responses from
//           explanatory variables */
//      data.setAttribute(new Symbol("terms"), terms);
//
//      if(naAction instanceof StringVector && naAction.length() > 0) {
//        naAction = new Symbol(((StringVector) naAction).getElementAsString(0));
//      }
//
//      SEXP result = FunctionCall.newCall(naAction, data.build()).evalToExp(context, rho);
//
//      if (!isNewList(result) || result.length() != data.length()) {
//        throw new EvalException("invalid result from na.action");
//      }
//        /* need to transfer _all but tsp and dim_ attributes, possibly lost
//           by subsetting in na.action.  */
//
//      if(result == Null.INSTANCE) {
//        return result;
//      } else {
//        throw new UnsupportedOperationException("todo");
//      }
//
////        for ( i = length(ans) ; i-- ; )
////                copyMostAttribNoTs(VECTOR_ELT(data, i),VECTOR_ELT(ans, i));
//
//    } else {
      return data.build();
 //   }
  }

  private static boolean isNewList(SEXP sexp) {
    return sexp == Null.INSTANCE || sexp instanceof ListVector;
  }
}
