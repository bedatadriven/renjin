package org.renjin.studio;

import java.io.PrintWriter;

import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.sexp.SEXP;

public class StudioSession {

  private Session session;
  
  public StudioSession() {
    super();
    this.session = new SessionBuilder()
        .bind(PackageLoader.class, new AetherPackageLoader())
        .withDefaultPackages().build();
  }

  public void setStdOut(PrintWriter printWriter) {
    session.setStdOut(printWriter);
    session.setStdErr(printWriter);
  }

  public EvalResult evaluate(SEXP expression) {
    SEXP exp = session.getTopLevelContext().evaluate(expression);
    return new EvalResult(exp, session.isInvisible());
  }

  public Context getTopLevelContext() {
    return session.getTopLevelContext();
  }  
}
