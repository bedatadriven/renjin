package org.renjin.gnur;

import org.apache.commons.math.special.Erf;
import org.renjin.gcc.link.LinkContext;
import org.renjin.gnur.api.*;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.sexp.SEXP;

import javax.inject.Provider;

/**
 * Created by alex on 28-2-16.
 */
public class GnurLinkContextProvider implements Provider<LinkContext> {
  @Override
  public LinkContext get() {
    LinkContext context = new LinkContext();

    try {
      context.addClass(Class.forName("org.renjin.appl.Appl"));

      context.addClass(Class.forName("org.renjin.math.Blas"));
      context.addClass(Lapack.class);
      Class distributionsClass = Class.forName("org.renjin.stats.internals.Distributions");
      context.addClass(distributionsClass);
//      context.addMethod("Rf_dbeta", distributionsClass, "dbeta");
//      context.addMethod("Rf_pbeta", distributionsClass, "pbeta");
//      context.addMethod("erf", Erf.class, "erf");
//      context.addMethod("erfc", Erf.class, "erfc");
      context.addClass(Arith.class);
      context.addClass(Callbacks.class);
      context.addClass(Defn.class);
      context.addClass(org.renjin.gnur.api.Error.class);
      context.addClass(eventloop.class);
      context.addClass(Fileio.class);
      context.addClass(GetText.class);
      context.addClass(GetX11Image.class);
      context.addClass(Graphics.class);
      context.addClass(GraphicsBase.class);
      context.addClass(GraphicsEngine.class);
      context.addClass(Internal.class);
      context.addClass(Memory.class);
      context.addClass(MethodDef.class);
      context.addClass(Parse.class);
      context.addClass(Print.class);
      context.addClass(PrtUtil.class);
      context.addClass(QuartzDevice.class);
      context.addClass(R.class);
      context.addClass(R_ftp_http.class);
      context.addClass(Sort.class);
      context.addClass(Random.class);
      context.addClass(Rconnections.class);
      context.addClass(Rdynload.class);
      context.addClass(RenjinDebug.class);
      context.addClass(Rgraphics.class);
      context.addClass(Riconv.class);
      context.addClass(Rinterface.class);
      context.addClass(Rinternals.class);
      context.addClass(rlocale.class);
      context.addClass(Rmath.class);
      context.addClass(RS.class);
      context.addClass(RStartup.class);
      context.addClass(S.class);
      context.addClass(Startup.class);
      context.addClass(stats_package.class);
      context.addClass(stats_stubs.class);
      context.addClass(Utils.class);
//
//      context.addRecordClass("SEXPREC", SEXP.class);
//
//      context.addClass(Rdynload.class);
//      context.addRecordClass("_DllInfo", DllInfo.class);
//      context.addRecordClass("__MethodDef", MethodDef.class);
      
      return context;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
