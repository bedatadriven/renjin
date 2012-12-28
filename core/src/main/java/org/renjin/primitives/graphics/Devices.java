package org.renjin.primitives.graphics;

import org.renjin.eval.Context;
import org.renjin.eval.Options;
import org.renjin.graphics.GraphicsDevice;
import org.renjin.graphics.GraphicsDevices;
import org.renjin.primitives.annotations.Current;
import org.renjin.sexp.SEXP;


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
        defdev = findVar(devName, context.getGlobalEnvironment());
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
          SEXP ns = findVarInFrame(context.getSession().namespaceRegistry,
                  install("grDevices"));
          if(ns != R_UnboundValue &&
                  findVar(devName, ns) != R_UnboundValue) {
            PROTECT(defdev = lang1(devName));
            eval(defdev, context, ns);
            UNPROTECT(1);
          } else
            error(_("no active or default device"));
        }
      } else if(TYPEOF(defdev) == CLOSXP) {
        PROTECT(defdev = lang1(defdev));
        eval(defdev, context, context.getGlobalEnvironment());
        UNPROTECT(1);
      } else
        error(_("no active or default device"));
    }
    return context.getSession().getSingleton(GraphicsDevices.class).getActive();
  }

}
