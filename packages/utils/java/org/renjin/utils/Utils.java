package org.renjin.utils;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.packaging.Package;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;

import java.io.IOException;
import java.util.Optional;

public class Utils {

  public static SEXP findPackageRoot(@Current Context context, String packageName) throws IOException {
    Optional<Package> pkg = context.getNamespaceRegistry().getPackageLoader().load(packageName);
    if(!pkg.isPresent()) {
      return StringVector.valueOf("");
    }

    return StringVector.valueOf(pkg.get().getPackageRootUri(context.getFileSystemManager()));
  }
}
