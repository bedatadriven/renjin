/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrame;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.ImmutableList;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Implements a standard package layout used by renjin's tools
 *
 */
public abstract class FileBasedPackage extends Package {


  protected FileBasedPackage(FqPackageName name) {
    super(name);
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return LazyLoadFrame.load(context, new Function<String, InputStream>() {

      @Override
      public InputStream apply(String name) {
        try {
          return getResource(name).openStream();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  public abstract boolean resourceExists(String name);


  private Properties readDatasetIndex() throws IOException {
    Properties datasets = new Properties();
    if(resourceExists("datasets")) {
      try(InputStream in = getResource("datasets").openStream()) {
        datasets.load(in);
      }
    }
    return datasets;
  }

  @Override
  public Collection<String> getPackageDependencies() throws IOException {
    if(resourceExists("requires")) {
      ImmutableList<String> lines = getResource("requires").asCharSource(Charsets.UTF_8).readLines();
      List<String> dependencies = Lists.newArrayList();
      for(String line : lines) {
        if(!Strings.isNullOrEmpty(line)) {
          dependencies.add(line);
        }
      }
      return dependencies;

    } else {
      return Collections.emptyList();
    }
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
    private Map<String, String> objectNameMap = new HashMap<>();

    public FileBasedDataset(String name, String[] objectNames) {
      this.datasetName = name;
      
      // Prior to Renjin 0.8.1942, members of a pairlist were stored unqualified,
      // for example:
      // wiod04=countries,industries,final04,output04,inter04
      // wiod05=countries,industries,final05,output05,inter05
      //
      // Which lead to conflicts when two different datasets, like wiod04 and wiod05 
      // had members of the same name (like "countries")

      // To fix this, the format was changed to qualify the element name 
      // wiod04=wiod04/countries,wiod04/industries,wiod04/final04,wiod04/output04,wiod04/inter04
      // wiod05=wiod05/countries,wiod05/industries,wiod05/final05,wiod05/output05,wiod05/inter05

      // To ensure that packages build with older version of Renjin can still be loaded,
      // we need to support both formats
      for (String objectName : objectNames) {
        if(objectName.contains("/")) {
          // Qualified
          // dataset/member
          String parts[] = objectName.split("/");
          String member = parts[1];
          objectNameMap.put(member, objectName);
        } else {
          // Unqualified
          objectNameMap.put(objectName, objectName);
        }
      }
    }

    @Override
    public String getName() {
      return datasetName;
    }

    @Override
    public Collection<String> getObjectNames() {
      return objectNameMap.keySet();
    }

    @Override
    public SEXP loadObject(String name) throws IOException {
      if(!objectNameMap.containsKey(name)) {
        throw new IllegalArgumentException(name);
      }
      try(InputStream in = getResource("data/" + objectNameMap.get(name)).openStream()) {
        RDataReader reader = new RDataReader(in);
        return reader.readFile();
      }
    }
  }
}
