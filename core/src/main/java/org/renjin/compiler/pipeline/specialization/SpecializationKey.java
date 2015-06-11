package org.renjin.compiler.pipeline.specialization;

import java.util.Arrays;

/**
 * Uniquely identifies a function specialized by its the classes of its operands
 */
public class SpecializationKey {

  private Class[] classes;
  private int hash;

  public SpecializationKey(Class[] classes) {
    this.classes = classes;
    this.hash = Arrays.hashCode(classes);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof SpecializationKey)) {
      return false;
    }
    SpecializationKey other = (SpecializationKey)obj;
    return Arrays.equals(classes, other.classes);
  }
}
