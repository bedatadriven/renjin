/**
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
package org.renjin.studio;

import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.sexp.SEXP;

import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudioSession {

  private Session session;
  private final ExecutorService threadPool;

  public StudioSession() {
    super();
    threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    this.session = new SessionBuilder()
        .setPackageLoader(new AetherPackageLoader())
        .setExecutorService(threadPool)
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

  public Session getSession() {
    return session;
  }

  public Context getTopLevelContext() {
    return session.getTopLevelContext();
  }  
}
