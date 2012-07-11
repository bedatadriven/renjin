package org.renjin.primitives.io.connections;

import org.renjin.primitives.io.connections.Connection.Type;

import com.google.common.base.Strings;

/**
 * 
 * Defines the way a connection should be opened.
 * Possible values are:
 * <ul>
 * <li>‘"r"’ or ‘"rt"’ Open for reading in text mode.
 * 
 * <li>‘"w"’ or ‘"wt"’ Open for writing in text mode.
 * <li>‘"a"’ or ‘"at"’ Open for appending in text mode.
 * <li>‘"rb"’ Open for reading in binary mode.
 * <li>‘"wb"’ Open for writing in binary mode.
 * <li>‘"ab"’ Open for appending in binary mode.
 * <li>‘"r+"’, ‘"r+b"’ Open for reading and writing.
 * <li>‘"w+"’, ‘"w+b"’ Open for reading and writing, truncating file
 *         initially.
 * <li>‘"a+"’, ‘"a+b"’ Open for reading and appending.
 * </ul>
 *
 */
public class OpenSpec {
  private String spec;

  public OpenSpec(String spec) {
    super();
    this.spec = Strings.nullToEmpty(spec).toLowerCase();
  }

  public boolean forReading() {
    return spec.startsWith("r") || spec.equals("w+b");
  }
  
  public boolean forWriting() {
    return spec.equals("r+") || spec.contains("w") || spec.contains("a");
  }
  
  public boolean isText() {
    return !spec.contains("b");
  }
  
  public boolean isBinary() {
    return spec.contains("b");
  }
  
  public Type getType() {
    return isText() ? Type.TEXT : Type.BINARY;
  }

  @Override
  public String toString() {
    return spec;
  }
  
}
