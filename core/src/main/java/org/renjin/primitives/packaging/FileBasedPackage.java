package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.packaging.LazyLoadFrame;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.PairList;
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
  public List<String> getDatasets() {
    try {
      Properties index = readDatasetIndex();
      return Lists.newArrayList(index.stringPropertyNames());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PairList loadDataset(String datasetName) throws IOException {
    InputStream in = getResource(datasetName).getInput();
    try {
      RDataReader reader = new RDataReader(in);
      SEXP exp = reader.readFile();
      if(exp instanceof PairList) {
        return (PairList)exp;
      } else {
        throw new EvalException("expected pairlist from " + datasetName + ", got " + exp.getTypeName());
      }
    } finally {
      Closeables.closeQuietly(in);
    }
  }
}
