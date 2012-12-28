package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.SessionController.SaveMode;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.StringVector;

import java.io.IOException;

/**
 * Implementation of interactive session related primitives like q(), interactive(),
 * menu(), etc
 * 
 */
public class Session {

  @Primitive
  public static void quit(@Current Context context, String saveMode, int exitCode, boolean runLast) {
    context.getSession().getSessionController().quit(context, SaveMode.valueOf(saveMode.toUpperCase()), exitCode, runLast);
  }
  
  /**
   * @return  TRUE when R is being used interactively and FALSE otherwise.
   */
  @Primitive
  public static boolean interactive(@Current Context context) {
    return context.getSession().getSessionController().isInteractive();
  }
  
  @Primitive
  public static int menu(@Current Context context, StringVector choices) throws IOException {
    return context.getSession().getSessionController().menu(choices);
  }
}
