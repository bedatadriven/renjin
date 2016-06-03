package org.renjin.repl;

import jline.Terminal;
import org.renjin.eval.Context;
import org.renjin.eval.SessionController;
import org.renjin.sexp.StringVector;

public class JlineSessionController extends SessionController {

  private Terminal terminal;
  private boolean interactive = true;
  
  public JlineSessionController(Terminal terminal) {
    super();
    this.terminal = terminal;
  }

  @Override
  public void quit(Context context, SaveMode saveMode, int exitCode,
      boolean runLast) {
    throw new QuitException(exitCode);
  }

  @Override
  public boolean isInteractive() {
    return interactive;
  }

  public void setInteractive(boolean interactive) {
    this.interactive = interactive;
  }

  @Override
  public int menu(StringVector choices) {
    for(int i=0;i!=choices.length();++i) {
      System.out.println(i + ": " + choices.getElementAsString(i));
    }
    return 0;
  }

  @Override
  public boolean isTerminal() {
    return terminal.isAnsiSupported();
  }
}
