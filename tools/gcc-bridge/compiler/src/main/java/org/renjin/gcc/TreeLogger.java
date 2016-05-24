package org.renjin.gcc;

import com.google.common.collect.Lists;
import com.google.common.html.HtmlEscapers;

import java.io.PrintWriter;
import java.util.List;

/**
 * Supports detailed logging of the compilation process
 */
public class TreeLogger {


  public enum Level {
    INFO,
    DEBUG
  }
  
  private Level level;
  private String message;
  private String code;
  private List<TreeLogger> children = Lists.newArrayListWithCapacity(0);
  
  public TreeLogger() {
    this(Level.INFO, "Compilation starting");
  }
  
  private TreeLogger(Level level, String message) {
    this.level = level;
    this.message = message;
  }
  
  public void info(String message) {
    children.add(new TreeLogger(Level.INFO, message));
  }

  public void debug(String message) {
    children.add(new TreeLogger(Level.DEBUG, message));
  }

  public TreeLogger enter(Level level, String message) {
    TreeLogger child = new TreeLogger(level, message);
    children.add(child);
    return child;
  }


  public TreeLogger debug(String message, Object code) {
    TreeLogger child = new TreeLogger(Level.DEBUG, message);
    child.code = code.toString();
    children.add(child);
    return child;
  }


  public TreeLogger enter(String message) {
    return enter(Level.INFO, message);
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
      writer.print(HtmlEscapers.htmlEscaper().escape(code));
      writer.println("</pre>");

    }
    
    if(!children.isEmpty()) {
      writer.println("<div class=\"children\">");
      for (TreeLogger child : children) {
        child.dumpNode(writer);
      }
      writer.println("</div>");
    }
    
    writer.println("</div>");
  }
}
