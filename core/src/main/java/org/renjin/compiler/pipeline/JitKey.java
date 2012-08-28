package org.renjin.compiler.pipeline;

import java.util.Arrays;

/**
 * Uniquely identifies a Jitted computation subgraph.
 */
public class JitKey {

  private Class[] classes;
  private int hash;

  public JitKey(Class[] classes) {
    this.classes = classes;
    this.hash = Arrays.hashCode(classes);
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof JitKey)) {
      return false;
    }
    JitKey other = (JitKey)obj;
    return Arrays.equals(classes, other.classes);
  }
}
