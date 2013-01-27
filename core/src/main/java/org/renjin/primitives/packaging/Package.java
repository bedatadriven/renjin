package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

import com.google.common.io.InputSupplier;

public abstract class Package {

  
  /**
   * 
   * @return the namespace definition, which defines the imported and exported symbols of this namespace.
   * (typically read from the NAMESPACE file)
   */
  public NamespaceDef getNamespaceDef() {
    return new NamespaceDef();
  }

  /**
   * Loads the R-language symbols that constitute this package's namespace.
   * 
   * @param context
   * @return
   * @throws IOException
   */
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return Collections.emptySet();
  }

  public InputSupplier<InputStream> getResource(String name) throws IOException {
    throw new IOException();
  }


  public SEXP loadDataset(String datasetName) throws IOException {
    return Null.INSTANCE;
  }

  public List<String> getDatasets() {
    return Collections.emptyList();
  }
}
