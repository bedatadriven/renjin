package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrame;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.SEXP;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * Implements a standard package layout used by renjin's tools
 *
 */
public abstract class FileBasedPackage extends Package {


  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return LazyLoadFrame.load(context, getResource("environment"));
  }

  public abstract boolean resourceExists(String name);

  @Override
  public NamespaceDef getNamespaceDef() {
    try {
      NamespaceDef def = new NamespaceDef();
      def.parse(CharStreams.newReaderSupplier(getResource("NAMESPACE"), Charsets.UTF_8));
      return def;
    } catch(IOException e) {
      throw new RuntimeException("IOException while parsing NAMESPACE file");
    }
  }
  
  private Properties readDatasetIndex() throws IOException {
    Properties datasets = new Properties();
    if(resourceExists("datasets")) {
      InputStream in = getResource("datasets").getInput();
      try {
        datasets.load(in);
      } finally {
        Closeables.closeQuietly(in);
      }
    }
    return datasets;
  }

  @Override
  public List<Dataset> getDatasets() {
    try {
      Properties index = readDatasetIndex();
      List<Dataset> datasets = Lists.newArrayList();
      for(String logicalDatasetName : index.stringPropertyNames()) {
        datasets.add(new FileBasedDataset(logicalDatasetName, 
            index.getProperty(logicalDatasetName).split("\\s*,\\s*")));
      }
      return datasets;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  private class FileBasedDataset extends Dataset {

    private String datasetName;
    private List<String> objectNames;

    public FileBasedDataset(String name, String[] objectNames) {
      this.datasetName = name;
      this.objectNames = Arrays.asList(objectNames);
    }

    @Override
    public String getName() {
      return datasetName;
    }

    @Override
    public Collection<String> getObjectNames() {
      return objectNames;
    }

    @Override
    public SEXP loadObject(String name) throws IOException {
      if(!objectNames.contains(name)) {
        throw new IllegalArgumentException(name);
      }
      InputStream in = getResource("data/" + name).getInput();
      try {
        RDataReader reader = new RDataReader(in);
        return reader.readFile();
      } finally {
        Closeables.closeQuietly(in);
      }
    }
  }
}
