package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.SessionController.SaveMode;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.StringVector;

/**
 * Implementation of interactive session related primitives like q(), interactive(),
 * menu(), etc
 * 
 */
public class Session {

  @Primitive
  public static void q(@Current Context context, String saveMode, int exitCode, boolean runLast) {
    context.getGlobals().getSessionController().quit(context, SaveMode.valueOf(saveMode.toUpperCase()), exitCode, runLast);
  }
  
  /**
   * @return  TRUE when R is being used interactively and FALSE otherwise.
   */
  @Primitive
  public static boolean interactive(@Current Context context) {
    return context.getGlobals().getSessionController().isInteractive();
  }
  
  @Primitive
  public static int menu(@Current Context context, StringVector choices) {
    return context.getGlobals().getSessionController().menu(choices);
  }
}
