package org.renjin.studio;

import org.renjin.aether.AetherPackageLoader;
import org.renjin.compiler.pipeline.MultiThreadedVectorPipeliner;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.PackageLoader;
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
        .bind(PackageLoader.class, new AetherPackageLoader())
        .bind(VectorPipeliner.class, new MultiThreadedVectorPipeliner(threadPool))
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
