package org.renjin.gcc.codegen;

import java.io.IOException;

public interface ResourceWriter {

  /**
   * Writes a resource, and returns the full name of the resource.
   */
  void writeResource(String name, byte[] bytes) throws IOException;

}
