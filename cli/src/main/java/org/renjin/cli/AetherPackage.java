package org.renjin.cli;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.primitives.packaging.Package;
import org.renjin.sexp.NamedValue;

public class AetherPackage extends Package {

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return super.loadSymbols(context);
  }

}
