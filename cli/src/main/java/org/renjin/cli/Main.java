package org.renjin.cli;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.compiler.pipeline.MultiThreadedVectorPipeliner;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repl.JlineRepl;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.Symbol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  private static ExecutorService threadPool;

  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();
    parser.accepts("e", "Evaluate 'EXPR' and exit")
        .withRequiredArg()
        .describedAs("EXPR");

    parser.accepts("f", "Take input from 'FILE'")
        .withRequiredArg()
        .describedAs("FILE");

    OptionSet options;
    try {
      options = parser.parse(args);
    } catch(OptionException e) {
      System.err.println(e.getMessage());
      parser.printHelpOn(System.out);
      System.exit(-1);
      return;
    }

    configureLogging();

    try {
      Session session = createSession();

      if(options.has("e")) {
        evaluateExpression(session, (String) options.valueOf("e"));
      } else if(options.has("f")) {
        evaluateFile(session, (String) options.valueOf("f"));
      } else {
        JlineRepl repl = new JlineRepl(session);
        repl.run();
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private static void configureLogging() {
    Logger.getLogger("").setLevel(Level.OFF);
  }

  private static void evaluateFile(Session session, String fileName) throws Exception {
    File file = new File(fileName);
    evaluate(session, new FileInputStream(file));
  }

  private static void evaluateExpression(Session session, String expression) throws Exception {
    evaluate(session, new ByteArrayInputStream(expression.getBytes()));
  }

  private static void evaluate(Session session, InputStream in) throws Exception {
    UnsupportedTerminal term = new UnsupportedTerminal();
    ConsoleReader consoleReader = new ConsoleReader(in, System.out, term);
    JlineRepl repl = new JlineRepl(session, consoleReader);
    repl.setEcho(true);
    repl.setStopOnError(true);

    try {
      repl.run();
    } catch(Exception e) {
      e.printStackTrace(System.err);
      System.err.println("Execution halted");
    }
  }

  public static Session createSession() throws Exception {
    threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    Session session = new SessionBuilder()
        .bind(PackageLoader.class, new AetherPackageLoader())
        .bind(VectorPipeliner.class, new MultiThreadedVectorPipeliner(threadPool))
        .build();
    Environment replEnv = session.getGlobalEnvironment().insertAbove(new HashFrame());
    loadDefaultPackages(session);
    return session;
  }


  private static void loadDefaultPackages(Session session) {
    String defaultPackages[] = new String[] {
        "stats", "graphics", "grDevices", "utils", "datasets", "methods" };
    for(String packageName : defaultPackages) {
      session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get(packageName)));
    }
  }

}
