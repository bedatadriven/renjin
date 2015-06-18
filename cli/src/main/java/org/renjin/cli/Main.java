package org.renjin.cli;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

public class Main {

  private static ExecutorService threadPool;
  
  private OptionSet options;
  private AetherPackageLoader packageLoader;
  private Session session;


  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();
    parser.accepts("e", "Evaluate 'EXPR' and exit")
        .withRequiredArg()
        .describedAs("EXPR");

    parser.accepts("f", "Take input from 'FILE'")
        .withRequiredArg()
        .describedAs("FILE");
    
    parser.accepts("args", "Script arguments")
      .withRequiredArg()
      .withValuesSeparatedBy(' ')
      .describedAs("ARGUMENTS");
    
    OptionSet options;
    try {
      options = parser.parse(args);
    } catch(OptionException e) {
      System.err.println(e.getMessage());
      parser.printHelpOn(System.out);
      System.exit(-1);
      return;
    }
    
    new Main(options).run();
  }

  public Main(OptionSet options) {
    this.options = options;
  }
  
  public void run() {

    configureLogging();

    try {
      initSession();
      if(options.has("args")) {
        List<String> rArgs = new ArrayList<String>();
        rArgs.add("--args"); /* Due to the unique way... */
        rArgs.add((String) options.valueOf("args"));
        rArgs.addAll(options.nonOptionArguments());
        this.session.setCommandLineArguments("renjin", 
            rArgs.toArray(new String[0]));
      }

      if(options.has("e")) {
        evaluateExpression((String) options.valueOf("e"));
        
      } else if(options.has("f")) {
        evaluateFile((String) options.valueOf("f"));
      } else {
        startInteractive();
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private void startInteractive() throws Exception {
    JlineRepl repl = new JlineRepl(session);
    JLineAetherListener listener = new JLineAetherListener(repl.getReader());
    packageLoader.setTransferListener(listener);
    packageLoader.setPackageListener(listener);
    repl.run();
  }

  private void configureLogging() {
    Logger.getLogger("").setLevel(Level.OFF);
  }

  private void evaluateFile(String fileName) throws Exception {
    File file = new File(fileName);
    evaluate(new FileInputStream(file));
  }

  private void evaluateExpression(String expression) throws Exception {
    evaluate(new ByteArrayInputStream(expression.getBytes()));
  }

  private void evaluate(InputStream in) throws Exception {
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
    } finally {
      if (threadPool != null && !threadPool.isShutdown()) {
        threadPool.shutdown();
      }
    }
  }

  public void initSession() throws Exception {
    int threads = Runtime.getRuntime().availableProcessors();
    String override = System.getProperty("renjin.vp.threads");
    if (override != null) {
      threads = Integer.parseInt(override);
      System.err.println("Using " + threads + " threads.");
    }
    threadPool = Executors.newFixedThreadPool(threads);

    packageLoader = new AetherPackageLoader();
    this.session = new SessionBuilder()
        .bind(PackageLoader.class, packageLoader)
        .bind(VectorPipeliner.class, new MultiThreadedVectorPipeliner(threadPool))
        .build();
    
    loadDefaultPackages();
  }


  private void loadDefaultPackages() {
    String defaultPackages[] = new String[] {
        "stats", "graphics", "grDevices", "utils", "datasets", "methods" };
    for(String packageName : defaultPackages) {
      session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"), Symbol.get(packageName)));
    }
  }
}
