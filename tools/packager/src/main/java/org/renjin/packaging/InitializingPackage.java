package org.renjin.packaging;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Package;
import org.renjin.sexp.NamedValue;
import org.renjin.util.NamedByteSource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class InitializingPackage extends Package {

  private final File packageRoot;

  protected InitializingPackage(FqPackageName name, File packageRoot) {
    super(name);
    this.packageRoot = packageRoot;
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return Collections.emptySet();
  }

  @Override
  public NamedByteSource getResource(String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class loadClass(String className) {
    try {
      return getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new EvalException(String.format("Could not load class %s from package %s", className, getName()), e);
    }
  }

  @Override
  public FileObject resolvePackageRoot(FileSystemManager fileSystemManager) throws FileSystemException {
    return fileSystemManager.toFileObject(packageRoot);
  }
}
