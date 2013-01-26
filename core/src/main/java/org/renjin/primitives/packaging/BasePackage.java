package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;

import org.renjin.eval.Context;
import org.renjin.sexp.NamedValue;

import com.google.common.io.InputSupplier;

public class BasePackage extends Package {

  @Override
  public NamespaceDef getNamespaceDef() {
    return new NamespaceDef();
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputSupplier<InputStream> getResource(String name) throws IOException {
    throw new UnsupportedOperationException();
  }

}
