package org.renjin.primitives.packaging;

import java.io.IOException;
import java.util.Collection;

import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

/**
 * A dataset is associated with a package and 
 * can contain one or more named R objects.
 *
 */
public abstract class Dataset {
  
  /**
   * 
   * @return the name of this dataset 
   */
  public abstract String getName();
  
  /**
   * 
   * @return the names of R objects contained in this dataset
   */
  public abstract Collection<String> getObjectNames();
  
  /**
   * 
   * @param name loads the object named {@code name} from the 
   * dataset
   * @return
   * @throws IOException
   */
  public abstract SEXP loadObject(String name) throws IOException;

  /**
   * Loads all the objects in this dataset as a PairList
   */
  public PairList loadAll() throws IOException {
    PairList.Builder pairList = new PairList.Builder();
    for(String objectName : getObjectNames()) {
      pairList.add(objectName, loadObject(objectName));
    }
    return pairList.build();
  }
}
