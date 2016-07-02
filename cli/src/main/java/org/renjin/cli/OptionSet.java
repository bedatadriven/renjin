package org.renjin.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Command line options
 */
public class OptionSet {
  
  
  public static final String COMPILE_LOOPS = "--compile-loops";
  public static final String PROFILE = "--profile";
  
  private String expression;
  private String file;
  
  private boolean helpRequested;

  private String[] args;
  
  private Set<String> flags = new HashSet<>();
  
  public OptionSet(String[] args) {
    this.args = args;
    Iterator<String> argIt = Arrays.asList(args).iterator();

    while (argIt.hasNext()) {
      String option = argIt.next();

      if(option.equals("--args")) {
        // Rest of the args are destined for the script itself
        break;
      }
      
      switch (option) {
        case "-e":
          this.expression = requireOption("-e", argIt);
          break;

        case "-f":
          this.file = requireOption("-f", argIt);
          break;

        case "-h":
        case "--help":
          this.helpRequested = true;
          break;

        case PROFILE:
        case COMPILE_LOOPS:
          flags.add(option);
          break;
        
        default:
          throw new OptionException("Unknown option '" + option + "'");
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

  public boolean isFlagSet(String flag) {
    return flags.contains(flag);
  }
}
