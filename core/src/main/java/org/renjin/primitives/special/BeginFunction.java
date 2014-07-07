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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SpecialFunction;
import org.renjin.sexp.ListVector;

import static org.renjin.util.CDefines.*;

public class BeginFunction extends SpecialFunction {

  public BeginFunction() {
    super("{");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    if(args == Null.INSTANCE) {
      context.setInvisibleFlag();
      return Null.INSTANCE;
    } else {
      SEXP srcRefs = call.getAttribute(R_SrcrefSymbol);
      ListVector lsrcRefs = null;
      int        lsrcRefsLen = 0;
      if (srcRefs == Null.INSTANCE) {
        context.setSrcRef(Null.INSTANCE);
      } else {
        lsrcRefs = (ListVector)srcRefs;
        lsrcRefsLen = lsrcRefs.length();
      }
      context.setSrcFile(call.getAttribute(R_SrcfileSymbol));
      int i = 0;
      SEXP lastResult = Null.INSTANCE;
      for (SEXP sexp : call.getArguments().values()) {
        if (i < lsrcRefsLen) {
           context.setSrcRef(lsrcRefs.get(i++));
        } else if (lsrcRefsLen > 0) {
           // i.e. we have
           //   (impossible)
           // TODO: think about better reporting
           System.err.println("warning: too small srcRefs: file="+context.getSrcFile()+", srcRefs.lenght="+lsrcRefsLen+", srcref=" + lsrcRefs.get(0) );
        }
        lastResult = context.evaluate( sexp, rho);
      }
      return lastResult;
    }
  }
}
