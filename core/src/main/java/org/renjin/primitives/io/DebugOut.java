package org.renjin.primitives.io;

import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Builtin;

public class DebugOut
{

  @Builtin("debug.stderr")
  public static String debugStderr(String message)
  {
   System.err.println(message);
   return message;
  }

}

