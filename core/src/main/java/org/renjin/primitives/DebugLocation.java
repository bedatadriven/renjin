package org.renjin.primitives;

import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;

public class DebugLocation
{

  @Builtin("debug.filename")
  public static SEXP debugFilename(@Current Context context,
                                     @Current Environment environment)
  {
    SEXP srcFile = context.getSrcFile();
    return srcFile;
  }

  @Builtin("debug.lineno")
  public static int debugLineno(@Current Context context)
  {
    SEXP srcRef = context.getSrcRef();
    if (srcRef != Null.INSTANCE) {
       return ((Vector)srcRef).getElementAsInt(0);
    } else {
       return 0;
    }
  }
 

}

