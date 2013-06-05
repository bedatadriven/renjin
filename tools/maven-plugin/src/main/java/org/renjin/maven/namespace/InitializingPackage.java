package org.renjin.maven.namespace;

import com.google.common.io.InputSupplier;
import org.renjin.eval.Context;
import org.renjin.primitives.packaging.NamespaceDef;
import org.renjin.primitives.packaging.Package;
import org.renjin.sexp.NamedValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class InitializingPackage extends Package {

  @Override
  public NamespaceDef getNamespaceDef() {
    return new NamespaceDef();
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return Collections.emptySet();
  }

  @Override
  public InputSupplier<InputStream> getResource(String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class getClass(String name) {
    throw new UnsupportedOperationException();
  }

}
