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

package org.renjin.primitives.models;

import java.util.List;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Types;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.matrix.DoubleMatrixBuilder;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;


import com.google.common.collect.Lists;

public class Models {


  @Primitive("~")
  public static SEXP tilde(Context context, Environment rho, FunctionCall call) {
    PairList.Builder attributes = PairList.Node.newBuilder();
    attributes.add(Symbols.CLASS, new StringVector("formula"));
    attributes.add(Symbols.DOT_ENVIRONMENT, rho);

    return new FunctionCall(call.getFunction(), call.getArguments(),
        attributes.build());

  }

  @Primitive("terms.formula")
  public static SEXP termsFormula(@Current Context context, FunctionCall x, SEXP specials, SEXP data, boolean keepOrder,
                                  boolean allowDotAsName) {

    if(specials != Null.INSTANCE) {
      throw new EvalException("specials != NULL is not supported");
    }
    
    Formula formula = new FormulaInterpreter().interpret(x);
    
    
    // define attibutes
    ListVector.NamedBuilder attributes = ListVector.newNamedBuilder();
    attributes.add("variables", formula.buildVariablesAttribute());
    attributes.add("factors", formula.buildFactorsMatrix());
    attributes.add("term.labels", formula.buildTermLabels());
    attributes.add("order", new IntVector());
    attributes.add("intercept", formula.buildInterceptAttribute());
    attributes.add("response",  formula.buildResponseAttribute());
    attributes.add(".Environment", context.getGlobalEnvironment() );
    attributes.add("class", new StringVector("terms", "formula"));
    
    // create an new Function Call
    FunctionCall copy = x.clone();
    return copy.setAttributes(attributes.build());
  }

  /**
   * 
   * Default implementation of model.frame, called from the model.frame.default closure.
   *  
   * <p>All the variables in formula, subset and in ... are looked for first in data and then in the 
   * environment of formula (see the help for formula() for further details) and collected into a data 
   * frame. Then the subset expression is evaluated, and it is used as a row index to the data frame.
   * Then the na.action function is applied to the data frame (and may well add attributes).
   * The levels of any factors in the data frame are adjusted according to the drop.unused.levels and 
   * xlev arguments: if xlev specifies a factor and a character variable is found, it is
   * converted to a factor (as from R 2.10.0).
   *   
   * <p>Unless na.action = NULL, time-series attributes will be removed from the variables found 
   * (since they will be wrong if NAs are removed).
   * 
   * <p>Note that all the variables in the formula are included in the data frame, even those preceded by -.
   * 
   * <p>Only variables whose type is raw, logical, integer, real, complex or character can be included
   * in a model frame: this includes classed variables such as factors (whose underlying type is integer),
   * but excludes lists.    if(Types.inherits(terms, "terms") )

   * 
   * get_all_vars returns a data.frame containing the variables used in formula plus those specified .... Unlike model.frame.default, it returns the input variables and not those resulting from function calls in formula.
   * 
   * @param context
   * @param rho
   * @param terms a model formula or terms object 
   * @param row_names
   * @param variables
   * @param varnames
   * @param dots
   * @param dotnames
   * @param subset
   * @param naAction
   * @return
   */
  @Primitive("model.frame")
  public static SEXP modelFrame(
      @Current Context context,
      @Current Environment rho,
      SEXP terms,
      StringVector row_names,
      Vector variables,
      Vector varnames,
      Vector dots,
      Vector dotnames,
      SEXP subset,
      SEXP naAction) {

    int nr, nc;
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
    for (int i = 0; i < ndots; i++) {
        if (dots.getElementAsSEXP(i) != Null.INSTANCE) {
          nactualdots++;
        }
    }

    /* Assemble the base data frame. */
    List<SEXP> data = Lists.newArrayList(); 
    List<String> names = Lists.newArrayList();
    
    PairList.Node.Builder attributes = new PairList.Node.Builder();
    
    for (int i = 0; i < nvars; i++) {
        data.add(variables.getElementAsSEXP(i));
        names.add(varnames.getElementAsString(i));
    }
    for (int i = 0, j = 0; i < ndots; i++) {
        String ss;
        if (dots.getElementAsSEXP(i) == Null.INSTANCE) {
          continue;
        }
        ss = "(" + ((StringVector)dotnames).getElementAsString(i) + ")";
        data.add(dots.getElementAsSEXP(i));
        names.add(ss);
        j++;
    }
    attributes.add(Symbols.NAMES, new StringVector(names));

    /* Sanity checks to ensure that the the answer can become */
    /* a data frame.  Be deeply suspicious here! */

    nc = data.size();
    nr = 0;                     /* -Wall */
    if (nc > 0) {
      nr = data.get(0).length();
      for(int i=0;i<nc;++i) {
        SEXP element = data.get(i);
        if(element instanceof AtomicVector) {
          if(nrows(element) != nr) {
            throw new EvalException("variable lengths differ (found for '%s')", names.get(i));
          }
        } else {
          throw new EvalException("invalid type (%s) for variable '%s'", element.getTypeName(), names.get(i));
        }
      }
    } else {
      nr = row_names.length();
    }

    /* Turn the data "list" into a "data.frame" */
    /* so that subsetting methods will work. */
    /* To do this we must attach "class"  and */
    /* "row.names" attributes */

    attributes.add(Symbols.CLASS, new StringVector("data.frame"));
    if (row_names.length() == nr) {
        attributes.add(Symbols.ROW_NAMES, row_names);
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
    
      return new ListVector(data, attributes.build());
 //   }
      
      
  }

  
  
  
  private static boolean isNewList(SEXP sexp) {
    return sexp == Null.INSTANCE || sexp instanceof ListVector;
  }
  
  private static int nrows(SEXP s) {
    SEXP t;
    if (s instanceof Vector) {
        SEXP dim = s.getAttribute(Symbols.DIM);
        if(dim == Null.INSTANCE) {
          return s.length();
        } else {
          return ((IntVector)s).getElementAsInt(0);
        }
    } else if(Types.inherits(s, "data.frame")) {
      return nrows(s.getElementAsSEXP(0));
      
    } else {  
      throw new EvalException("object is not a matrix");
    }
  }
  
  @Primitive("model.matrix")
  public static Vector modelMatrix(@Current Context context, FunctionCall terms, ListVector modelFrame) {
   
    return new ModelMatrixBuilder(context, terms, modelFrame).build();
    
  }
  
}
