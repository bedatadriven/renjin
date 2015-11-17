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

package org.renjin.stats.internals.models;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.Attributes;
import org.renjin.sexp.*;

public class Models {




  @Internal("terms.formula")
  public static SEXP termsFormula(@Current Context context, FunctionCall x, SEXP specials, SEXP data, boolean keepOrder,
                                  boolean allowDotAsName) {
    
    Formula formula = new FormulaInterpreter().interpret(x);
    
    
    // define attibutes
    AttributeMap.Builder attributes = AttributeMap.builder();
    attributes.set("variables", formula.buildVariablesAttribute());
    attributes.set("factors", formula.buildFactorsMatrix());
    attributes.set("term.labels", formula.buildTermLabels());
    attributes.set("order", new IntArrayVector());
    attributes.set("intercept", formula.buildInterceptAttribute());
    attributes.set("response",  formula.buildResponseAttribute());
    attributes.set(".Environment", context.getGlobalEnvironment() );
    attributes.set("class", new StringArrayVector("terms", "formula"));
    
    if(specials != Null.INSTANCE) {
      attributes.set("specials", buildSpecials((AtomicVector)specials));
    }
    
    // create an new Function Call  
    FunctionCall copy = x.clone();
    return copy.setAttributes(attributes.build());
  }

  private static PairList buildSpecials(AtomicVector specials) {
    PairList.Builder pairList = new PairList.Builder();
    for(int i=0;i!=specials.length();++i) {
      pairList.add(Symbol.get(specials.getElementAsString(i)), Null.INSTANCE);
    }
    return pairList.build();
  }


  public static int nrows(SEXP s) {
    if (s instanceof Vector) {
        SEXP dim = s.getAttribute(Symbols.DIM);
        if(dim == Null.INSTANCE) {
          return s.length();
        } else {
          return ((IntVector)dim).getElementAsInt(0);
        }
    } else if(Attributes.inherits(s, "data.frame")) {
      return nrows(s.getElementAsSEXP(0));
      
    } else {  
      throw new EvalException("object is not a matrix");
    }
  }
  
  @Internal("model.matrix")
  public static Vector modelMatrix(@Current Context context, FunctionCall terms, ListVector modelFrame) {
   
    return ModelMatrixBuilder.build(context, terms, modelFrame);
    
  }
  
}
