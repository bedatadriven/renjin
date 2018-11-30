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
package org.renjin.cli;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.cli.build.Builder;
import org.renjin.eval.Profiler;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.special.ForFunction;
import org.renjin.repl.JlineRepl;

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
  
  private OptionSet options;
  private AetherPackageLoader packageLoader;
  private Session session;


  public static void main(String[] args) throws Exception {

    if(args.length >= 1 && (args[0].equals("build"))) {
      new Builder(args).execute();
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
    
    // Set process-wide flags
    if(optionSet.isFlagSet(OptionSet.COMPILE_LOOPS)) {
      ForFunction.COMPILE_LOOPS = true;
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
      
      this.session.setCommandLineArguments("renjin", options.getArguments());

      if(options.isFlagSet(OptionSet.PROFILE)) {
        Profiler.ENABLED = true;
      }

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

  public void initSession() {
    threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    packageLoader = new AetherPackageLoader();

    this.session = new SessionBuilder()
        .setPackageLoader(packageLoader)
        .setClassLoader(packageLoader.getClassLoader())
        .setExecutorService(threadPool)
        .withDefaultPackages()
        .build();
  }

}
