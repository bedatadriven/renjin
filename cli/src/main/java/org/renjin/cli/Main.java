package org.renjin.cli;

import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repl.JlineRepl;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.Symbol;

public class Main {

  public static void main(String[] args) throws Exception {
    new JlineRepl(createSession());
  }

  public static Session createSession() throws Exception {
    Session session = new SessionBuilder()
    .bind(PackageLoader.class, new AetherPackageLoader())
    .build();
    Environment replEnv = session.getGlobalEnvironment().insertAbove(new HashFrame());
    loadDefaultPackages(session);
    return session;
  }

  private static void loadDefaultPackages(Session session) {
    String defaultPackages[] = new String[] { "stats", "graphics", "grDevices", "utils", "datasets", "methods" };
    for(String packageName : defaultPackages) {
      session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get(packageName)));
    }
  }
}
