package org.renjin.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Command line options
 */
public class OptionSet {

  private String expression;
  private String file;
  
  private boolean helpRequested;

  private String[] args;

  public OptionSet(String[] args) {
    this.args = args;
    Iterator<String> argIt = Arrays.asList(args).iterator();
    while(argIt.hasNext()) {
      String option = argIt.next();
      
      if(option.equals("-e")) {
        this.expression = requireOption("-e", argIt);
      
      } else if(option.equals("-f")) {
        this.file = requireOption("-f", argIt);

      } else if(option.equals("-h") || option.equals("--help")) {
        this.helpRequested = true;
        
      } else if(option.equals("--args")) {
        // Rest of the args are destined for the script itself
        break;
      }
    }
  }

  public boolean isHelpRequested() {
    return helpRequested;
  }

  /**
   * @return all arguments passed via the command line
   */
  public List<String> getArguments() {
    return Arrays.asList(args);
  }

  private String requireOption(String option, Iterator<String> argIt) {
    if(!argIt.hasNext()) {
      throw new OptionException("Option " + option + " requires an argument."); 
    }
    return argIt.next();
  }
  
  public static void printHelp(PrintStream out) throws IOException {
    String helpText = Resources.toString(Resources.getResource("help.txt"), Charsets.UTF_8);
    out.println(helpText);
  }

  public boolean hasExpression() {
    return expression != null;
  }

  public String getExpression() {
    return expression;
  }

  public boolean hasFile() {
    return file != null;
  }

  public String getFile() {
    return file;
  }
}
