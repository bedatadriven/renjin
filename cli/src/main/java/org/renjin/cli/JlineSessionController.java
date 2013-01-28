package org.renjin.cli;

import jline.console.ConsoleReader;
import org.renjin.eval.Context;
import org.renjin.eval.SessionController;
import org.renjin.sexp.StringVector;

import java.io.IOException;

public class JlineSessionController extends SessionController {

  private ConsoleReader reader;
  
  public JlineSessionController(ConsoleReader reader) {
    super();
    this.reader = reader;
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
  public int menu(StringVector choices) throws IOException {
    do {
      String choice = reader.readLine("Selection: ");
      try {
        int index = Integer.parseInt(choice);
        if(index == 0) {
          return 0;
        } else if(index >= 1 && index <= choices.length()) {
          return index;
        }
      } catch(Exception e) {
      }
      reader.println("Enter an item from the menu, or 0 to exit");
    } while(true);
  }
}
