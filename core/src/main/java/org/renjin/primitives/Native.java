package org.renjin.primitives;

import com.sun.jna.NativeLibrary;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.*;

public class Native {


  @Primitive("dyn.load")
  public static ListVector dynLoad(String libraryPath, SEXP local, SEXP now, SEXP dllPath) {

    NativeLibrary library = NativeLibrary.getInstance(libraryPath);
    
    ListVector.NamedBuilder result = new ListVector.NamedBuilder();

    result.add("name", library.getName());
    result.add("path", libraryPath);
    result.add("dynamicLookup", LogicalVector.TRUE);
    result.add("handle", new ExternalExp<NativeLibrary>(library));
    result.add("info", "something here");
    result.setAttribute(Symbols.CLASS, new StringVector("DLLInfo"));
    return result.build();
  }

}
