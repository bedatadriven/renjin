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
package org.renjin.gcc.logging;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.symbols.SymbolTable;
import org.renjin.gcc.symbols.UnitSymbolTable;
import org.renjin.repackaged.asm.tree.MethodNode;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports detailed logging of the compilation process.
 *
 * <p>The log manager helps organize logging of the inputs and outputs to different steps
 * in the process, as well as diagnostic logs.
 *
 * <p>Rather than write to one large log file, the idea is to write out short logs on a mostly per-function basis
 * that are easy to find and review.</p>
 */
public class LogManager {

  private File loggingDirectory;

  private final Map<String, Logger> openLoggers = new HashMap<>();

  public LogManager() {
  }

  public File getLoggingDirectory() {
    return loggingDirectory;
  }

  /**
   * Sets the root logging directory, or {@code null} to disable logging.
   */
  public void setLoggingDirectory(File loggingDirectory) {
    this.loggingDirectory = loggingDirectory;
  }

  public boolean isEnabled() {
    return loggingDirectory != null;
  }

  /**
   * Gets a logger with the specific name. This log is written to the path
   * {loggingDirectory}/{name}.log
   */
  public Logger getLogger(String name) {

    if(!isEnabled()) {
      return Logger.NULL;
    }

    File logFile = new File(loggingDirectory, name + ".log");
    if(!logFile.getParentFile().exists()) {
      logFile.getParentFile().mkdirs();
    }

    return getLogger(logFile);
  }

  /**
   * Gets a logger with the specific name for the given function name. This log is written to the path
   * {loggingDirectory}/{sourceFile}/{functionName}.{logName}.log
   */
  public Logger getLogger(GimpleFunction gimpleFunction, String logName) {

    if(!isEnabled()) {
      return Logger.NULL;
    }

    return getLogger(logFile(gimpleFunction, logName + "log"));
  }


  /**
   * Logs an object related to a specific function.
   *
   * <p>If logging is enabled, the given {@code object} is stringified and written to a file
   * in the path {loggingDirectory}/{sourceFile}/{function}.{logType}
   */
  public void log(GimpleFunction function, String logType, Object object) {
    if(!isEnabled()) {
      return;
    }

    File logFile = logFile(function, logType);

    try {
      Files.write(object.toString(), logFile, Charsets.UTF_8);
    } catch (IOException e) {
      System.err.println("Exception dumping to " + logFile.getAbsolutePath());
    }
  }

  /**
   * If logging is enabled, Writes an html file for the function that lines up the original source, the gimple, and
   * the resulting bytecode, in the path {loggingDirectory}/{sourceFile}/{function}.{html}
   */
  public void logTriView(GimpleFunction function, SymbolTable symbolTable, MethodNode methodNode) {
    if(!isEnabled()) {
      return;
    }

    try {
      log(function, "html",
          new HtmlFunctionRenderer(symbolTable, function, methodNode).render());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public void logRecords(GimpleCompilationUnit unit, UnitSymbolTable symbolTable) {
    if(!isEnabled()) {
      return;
    }
    File logFile = logFile(unit.getSourceName(), "records", "html");
    try {
      Files.write(new HtmlRecordRenderer(symbolTable, unit).render(), logFile, Charsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private File logFile(GimpleFunction gimpleFunction, String suffix) {
    return logFile(gimpleFunction.getUnit().getSourceName(), gimpleFunction.getSafeMangledName(), suffix);
  }

  private File logFile(String dir, String file, String ext) {
    File dumpDir = new File(loggingDirectory.getAbsolutePath() + File.separator + dir);
    if(!dumpDir.exists()) {
      dumpDir.mkdirs();
    }
    return new File(dumpDir, file + "." + ext);
  }

  private Logger getLogger(File logFile) {
    Logger logger = openLoggers.get(logFile.getAbsolutePath());
    if(logger == null) {
      logger = new Logger(logFile);
      openLoggers.put(logFile.getAbsolutePath(), logger);
    }
    return logger;
  }

  public void finish() throws IOException {
    for (Logger logger : openLoggers.values()) {
      logger.close();
    }
    openLoggers.clear();
  }

}
