package org.renjin.maven.namespace;

import com.google.common.io.ByteSource;
import org.renjin.eval.Context;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Package;
import org.renjin.sexp.NamedValue;

import java.io.IOException;
import java.util.Collections;

public class InitializingPackage extends Package {

  protected InitializingPackage(FqPackageName name) {
    super(name);
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return Collections.emptySet();
  }

  @Override
  public ByteSource getResource(String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class getClass(String name) {
    throw new UnsupportedOperationException();
  }

}
