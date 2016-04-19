package org.renjin.primitives;


import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.Function;
import org.renjin.sexp.Symbol;

import java.io.IOException;

public class Args {
  
  public static Function args(@Current Context context, String name) throws IOException {
    Function function = context.getEnvironment().findFunction(context, Symbol.get(name));
    return args(context, function);
  }
  
  public static Function args(@Current Context context, Function function) throws IOException {
    return function;
  }
  
}
