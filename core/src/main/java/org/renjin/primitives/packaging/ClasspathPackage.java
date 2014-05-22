package org.renjin.primitives.packaging;

import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import org.renjin.eval.EvalException;
import org.renjin.sexp.PairList;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides access to a Renjin package that 
 * is on the classpath.
 *
 */
public class ClasspathPackage extends FileBasedPackage {


  public ClasspathPackage(FqPackageName name) {
    super(name);
  }
  
  public boolean exists() {
    return resourceExists("environment");
  }

  @Override
  public InputSupplier<InputStream> getResource(String name) throws IOException {
    String url = resourceUrl(name);
    return Resources.newInputStreamSupplier(
        Resources.getResource(url));
  }

  @Override
  public Class getClass(String name) {
    try {
      return Class.forName(getName().getGroupId() + "." + getName().getPackageName() + "." + name);
    } catch (ClassNotFoundException e) {
      throw new EvalException(e.getMessage(), e);
    }
  }

  private String resourceUrl(String name) {
    return 
        getName().getGroupId().replace('.', '/') +
        "/" +
        getName().getPackageName() +
        "/" +
        name;
  }

  @Override
  public boolean resourceExists(String name) {
    InputStream in = getClass().getResourceAsStream("/" + resourceUrl(name));
    if(in == null) {
      return false;
    } else {
      Closeables.closeQuietly(in);
      return true;
    }
  }
}
