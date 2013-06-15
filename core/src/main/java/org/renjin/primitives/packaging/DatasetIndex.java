package org.renjin.primitives.packaging;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DatasetIndex {
  
  private Properties index;

  public DatasetIndex(Properties index) {
    this.index = index;
  }

  /**
   * Returns the names of the R objects that a given logical dataset 
   * contains.
   */
  public List<String> getObjectNamesForLogicalDataset(String datasetName) {
    
    List<String> objectNames = Lists.newArrayList();
    
    for(Object objectName : index.keySet()) {
      if(index.get(objectName).equals(datasetName)) {
        objectNames.add((String)objectName);
      }
    }
    return objectNames;
  }

  public Set<String> getLogicalDatasets() {
    Set<String> names = Sets.newHashSet();
    for(Object name : index.values()) {
      names.add((String)name);
    }
    return names;
  }
}
