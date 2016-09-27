/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
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
  public static SEXP termsFormula(@Current Context context, 
                                  FunctionCall x, 
                                  SEXP specials, 
                                  SEXP data,
                                  boolean keepOrder,
                                  boolean allowDotAsName) {
    
    Formula formula = new FormulaInterpreter()
        .withData(data)
        .allowDotAsName(allowDotAsName)
        .interpret(x);
    
    
    // define attibutes
    AttributeMap.Builder attributes = AttributeMap.builder();
    attributes.set("variables", formula.buildVariablesAttribute());
    attributes.set("factors", formula.buildFactorsMatrix());
    attributes.set("term.labels", formula.buildTermLabels());
    attributes.set("order", formula.buildInteractionOrderAttribute());
    attributes.set("intercept", formula.buildInterceptAttribute());
    attributes.set("response",  formula.buildResponseAttribute());
    attributes.set(".Environment", context.getGlobalEnvironment() );
    attributes.set("class", new StringArrayVector("terms", "formula"));
    
    if(specials != Null.INSTANCE) {
      attributes.set("specials", buildSpecials((AtomicVector)specials));
    }
    
    // return the new function call with attributes
    return formula.getExpandedFormula().setAttributes(attributes);
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
