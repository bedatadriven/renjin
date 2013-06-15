package org.renjin.primitives.packaging;


public interface PackageLoader {

  /**
   * @param name the package name
   * @return a set of resources implementing the Package interface or 
   * null if the package could not be found
   */
  Package load(String name);

}