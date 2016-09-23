/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.util.Collection;

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
