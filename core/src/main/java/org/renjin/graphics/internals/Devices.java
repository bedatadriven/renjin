/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.graphics.internals;

import org.renjin.eval.Context;
import org.renjin.eval.Options;
import org.renjin.graphics.GraphicsDevice;
import org.renjin.graphics.GraphicsDevices;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import static org.renjin.util.CDefines.*;


public class Devices {


  public static GraphicsDevice GEcurrentDevice(@Current Context context) {
    /* If there are no active devices
     * check the options for a "default device".
     * If there is one, start it up. */
    if (context.getSession().getSingleton(GraphicsDevices.class).isEmpty()) {
      SEXP defdev = context.getSession().getSingleton(Options.class).get("device");
      if (isString(defdev) && length(defdev) > 0) {
        SEXP devName = install(CHAR(STRING_ELT(defdev, 0)));
        /*  Not clear where this should be evaluated, since
            grDevices need not be in the search path.
            So we look for it first on the global search path.
        */
        defdev = context.getGlobalEnvironment().findVariable(context, (Symbol)devName);
        if(defdev != R_UnboundValue) {
          PROTECT(defdev = lang1(devName));
          eval(defdev, context, context.getGlobalEnvironment());
          UNPROTECT(1);
        } else {
          /* Not globally visible:
             try grDevices namespace if loaded.
             The option is unlikely to be set if it is not loaded,
             as the default setting is in grDevices:::.onLoad.
          */
          SEXP ns = context.getSession().getNamespaceRegistry().getNamespace(context, "grDevices").getNamespaceEnvironment();
          if(ns != R_UnboundValue &&
                  context.getGlobalEnvironment().findVariable(context, (Symbol)devName) != R_UnboundValue) {
            PROTECT(defdev = lang1(devName));
            eval(defdev, context, ns);
            UNPROTECT(1);
          } else {
            error(_("no active or default device"));
          }
        }
      } else if(TYPEOF(defdev) == CLOSXP) {
        PROTECT(defdev = lang1(defdev));
        eval(defdev, context, context.getGlobalEnvironment());
        UNPROTECT(1);
      } else {
        error(_("no active or default device"));
      }
    }
    return context.getSession().getSingleton(GraphicsDevices.class).getActive();
  }

}
