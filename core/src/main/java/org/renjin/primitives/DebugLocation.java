package org.renjin.primitives;

import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;

public class DebugLocation
{

  @Builtin("debug.filename")
  public static String debugFilename(@Current Context context,
                                     @Current Environment environment)
  {
   throw new RuntimeException("not implemented");
  }

  @Builtin("debug.lineno")
  public static int debugLineno(@Current Context context)
  {
    throw new RuntimeException("not implemented");
  }
 

}

