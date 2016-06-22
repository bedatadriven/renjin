package org.renjin.utils;


import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.StringVector;

import java.io.IOException;

public class Interactive {

  public static int menu(@Current Context context, StringVector choices) throws IOException {
    return context.getSession().getSessionController().menu(choices);
  }
}
