package org.renjin.cli.build;

/**
 * Reports status of build
 */
public class BuildReporter {
  
  private boolean verbose = true;
  
  
  
  public void info(String message, Object... args) {
    System.out.println(String.format(message, args));  
  }
  
  public void debug(String message, Object... args) {
    if(verbose) {
      System.out.println(String.format(message, args));
    }
  }

  public void warn(String message, Exception e) {
    System.out.println(message);
    e.printStackTrace();
  }
}
