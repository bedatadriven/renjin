package org.renjin.cli;

import org.renjin.eval.Context;
import org.renjin.eval.SessionController;
import org.renjin.sexp.StringVector;

public class CliSessionController extends SessionController {

  private Console console;
  
  public CliSessionController(Console console) {
    super();
    this.console = console;
  }

  @Override
  public void quit(Context context, SaveMode saveMode, int exitCode,
      boolean runLast) {
    System.exit(exitCode);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }

  @Override
  public int menu(StringVector choices) {
    for(int i=0;i!=choices.length();++i) {
      console.println(i + ": " + choices.getElementAsString(i));
    }
    return 0;
  }

}
