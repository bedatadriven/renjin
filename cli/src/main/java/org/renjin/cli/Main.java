package org.renjin.cli;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.cli.build.Builder;
import org.renjin.compiler.pipeline.MultiThreadedVectorPipeliner;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.eval.Profiler;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repl.JlineRepl;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  private static ExecutorService threadPool;
  
  private OptionSet options;
  private AetherPackageLoader packageLoader;
  private Session session;


  public static void main(String[] args) throws Exception {

    if(args.length >= 1 && (args[0].equals("build") || args[0].equals("install")) ) {
      Builder.execute(args[0], Arrays.copyOfRange(args, 1, args.length));
      return;
    }
    
    
    OptionSet optionSet;
    try {
      optionSet = new OptionSet(args);
    } catch(OptionException e) {
      System.err.println(e.getMessage());
      OptionSet.printHelp(System.out);
      System.exit(-1);
      return;
    }
    
    if(optionSet.isHelpRequested()) {
      OptionSet.printHelp(System.out);
      System.exit(0);
    }
    
    try {
      new Main(optionSet).run();
    } finally {
      if(Profiler.ENABLED) {
        System.out.flush();
        Profiler.dumpTotalRunningTime();
        Profiler.dump(System.out);
      }
    }
  }

  public Main(OptionSet options) {
    this.options = options;
  }
  
  public void run() {

    configureLogging();

    try {
      initSession();
      Profiler.reset();
      
      this.session.setCommandLineArguments("renjin", options.getArguments());

      if(options.hasExpression()) {
        evaluateExpression(options.getExpression());
        
      } else if(options.hasFile()) {
        evaluateFile(options.getFile());
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
    repl.setInteractive(true);
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
    repl.setInteractive(false);

    try {
      repl.run();
    } catch(Exception e) {
      // Stack trace already printed by Repl
      System.err.println("Execution halted");
      
    } finally {
      if (threadPool != null && !threadPool.isShutdown()) {
        threadPool.shutdown();
      }
    }
  }

  public void initSession() throws Exception {
    threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
