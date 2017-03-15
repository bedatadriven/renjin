/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc;

import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.html.HtmlEscapers;
import org.renjin.repackaged.guava.io.ByteStreams;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;


public class HtmlTreeLogger extends TreeLogger {
  
  
  private File loggingDirectory;
  private Level level;
  private String message;
  private Object code;
  private List<HtmlTreeLogger> children = Lists.newArrayListWithCapacity(0);

  public HtmlTreeLogger(File loggingDirectory) {
    this(loggingDirectory, Level.INFO, "Compilation starting");
    this.loggingDirectory = loggingDirectory;
  }
  
  private HtmlTreeLogger() {
  }

  private HtmlTreeLogger(File loggingDirectory, Level level, String message) {
    this.loggingDirectory = loggingDirectory;
    this.level = level;
    this.message = message;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public PrintWriter debugLog(String name) {
    File dumpFile = new File(loggingDirectory, name + ".log");
    if(!dumpFile.getParentFile().exists()) {
      dumpFile.getParentFile().mkdirs();
    }

    try {
      return new PrintWriter(dumpFile);
    } catch (FileNotFoundException e) {
      System.err.println("Warning: could not open log file at " + dumpFile.getAbsolutePath());
      return new PrintWriter(ByteStreams.nullOutputStream());
    }
  }

  @Override
  public void dump(String dir, String file, String ext, Object value) {
    File dumpDir = new File(loggingDirectory.getAbsolutePath() + File.separator + dir);
    if(!dumpDir.exists()) {
      dumpDir.mkdirs();
    }

    File dumpFile = new File(dumpDir, file + "." + ext);
    try {
      Files.write(value.toString(), dumpFile, Charsets.UTF_8);
    } catch (IOException e) {
      System.err.println("Exception dumping to " + dumpFile.getAbsolutePath());
    }
  }

  @Override
  public void log(Level level, String message) {
    children.add(new HtmlTreeLogger(loggingDirectory, Level.DEBUG, message));
  }

  @Override
  public TreeLogger branch(Level level, String message) {
    HtmlTreeLogger child = new HtmlTreeLogger(loggingDirectory, level, message);
    children.add(child);
    return child;
  }

  @Override
  public TreeLogger debug(String message, Object code) {
    HtmlTreeLogger child = new HtmlTreeLogger(loggingDirectory, Level.DEBUG, message);
    child.code = code;
    children.add(child);
    return child;
  }

  @Override
  public void finish() throws IOException {
    Preconditions.checkState(loggingDirectory != null, "finish() may only be called on root logger.");
    
    if(!loggingDirectory.exists()) {
      loggingDirectory.mkdirs();
    }
    File logFile = new File(loggingDirectory, "compile-log.html");
    try(PrintWriter writer = new PrintWriter(logFile)) {
      URL headerResource = Resources.getResource(GimpleCompiler.class, "log-head.html");
      String header = Resources.toString(headerResource, Charsets.UTF_8);
      writer.println(header);
      dumpHtml(writer);
    }
  }

  public void dumpHtml(PrintWriter writer) {
    writer.println("<html>");
    writer.println("<body>");
    dumpNode(writer);
    writer.print("</body>");
    writer.print("</html>");
  }

  private void dumpNode(PrintWriter writer) {
    writer.println("<div class=\"node " + level.name().toLowerCase()  +
        (children.isEmpty() ? " leaf" : " parent") + "\">");

    writer.print("<div class=\"message\">");
    writer.print(HtmlEscapers.htmlEscaper().escape(message));
    writer.println("</div>");

    if(code != null) {
      writer.print("<pre class=\"code\">");
      writer.print(HtmlEscapers.htmlEscaper().escape(code.toString()));
      writer.println("</pre>");
    }

    if(!children.isEmpty()) {
      writer.println("<div class=\"children\">");
      for (HtmlTreeLogger child : children) {
        child.dumpNode(writer);
      }
      writer.println("</div>");
    }

    writer.println("</div>");
  }
}
