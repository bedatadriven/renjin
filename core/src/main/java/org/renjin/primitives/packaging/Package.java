package org.renjin.primitives.packaging;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.sexp.NamedValue;

public interface Package {

  NamespaceDef getNamespaceDef();

  Iterable<NamedValue> loadSymbols(Context context) throws IOException;
  
}
