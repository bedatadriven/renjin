package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.io.ByteSource;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.util.*;

import com.google.common.io.InputSupplier;

public abstract class Package {

  private final FqPackageName name;

  protected Package(FqPackageName name) {
    this.name = name;
  }

  public final FqPackageName getName() {
    return name;
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


  public NamedByteSource getResource(String name) throws IOException {
    throw new IOException();
  }

  /**
   * @return the list of datasets contained in this package
   */
  public List<Dataset> getDatasets() throws IOException {
    return Collections.emptyList();
  }

  public SEXP getDataset(String datasetName) throws IOException {
    for(Dataset dataset : getDatasets()) {
      if(dataset.getName().equals(datasetName)) {
        return dataset.loadAll();
      }
    }
    return Null.INSTANCE;
  }
  
  public abstract Class getClass(String name);


  /**
   * @return a list of packages that should be loaded into the global search
   * path when this package is loaded. This mechanism predates the more recent
   * NAMESPACE approach.
   */
  public Collection<String> getPackageDependencies() throws IOException {
    return Collections.emptyList();
  }
}
