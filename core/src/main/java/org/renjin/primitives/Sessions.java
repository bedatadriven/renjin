package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.SessionController.SaveMode;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;

/**
 * Implementation of interactive session related primitives like q(), interactive(),
 * menu(), etc
 * 
 */
public class Sessions {

  private Sessions() {}


  @Internal
  public static void quit(@Current Context context, String saveMode, int exitCode, boolean runLast) {
    context.getSession().getSessionController().quit(context, SaveMode.valueOf(saveMode.toUpperCase()), exitCode, runLast);
  }
  
  /**
   * @return  TRUE when R is being used interactively and FALSE otherwise.
   */
  @Builtin
  public static boolean interactive(@Current Context context) {
    return context.getSession().getSessionController().isInteractive();
  }
  


}
