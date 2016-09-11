package org.renjin.gcc;

import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.html.HtmlEscapers;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
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
    this(Level.INFO, "Compilation starting");
    this.loggingDirectory = loggingDirectory;
  }
  
  private HtmlTreeLogger() {
  }

  private HtmlTreeLogger(Level level, String message) {
    this.level = level;
    this.message = message;
  }
  
  @Override
  public void log(Level level, String message) {
    children.add(new HtmlTreeLogger(Level.DEBUG, message)); 
  }

  @Override
  public TreeLogger branch(Level level, String message) {
    HtmlTreeLogger child = new HtmlTreeLogger(level, message);
    children.add(child);
    return child;
  }

  @Override
  public TreeLogger debug(String message, Object code) {
    HtmlTreeLogger child = new HtmlTreeLogger(Level.DEBUG, message);
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
