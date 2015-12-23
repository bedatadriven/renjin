package org.renjin.packaging;

import com.google.common.io.ByteSource;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
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
  public Class loadClass(String className) {
    try {
      return getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new EvalException("Could not load class %s from package %s", className, getName(), e);
    }
  }
}
