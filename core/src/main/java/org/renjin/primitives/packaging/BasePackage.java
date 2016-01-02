package org.renjin.primitives.packaging;

import com.google.common.io.ByteSource;
import org.renjin.eval.Context;
import org.renjin.sexp.NamedValue;
import org.renjin.util.NamedByteSource;

import java.io.IOException;

public class BasePackage extends Package {

  protected BasePackage() {
    super(FqPackageName.BASE);
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public NamedByteSource getResource(String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class loadClass(String name) {
    throw new UnsupportedOperationException();
  }
}
