package org.renjin.sexp;

import java.nio.IntBuffer;

public class IntBufferVector extends IntVector {

  private final IntBuffer buffer;
  private int length;

  public IntBufferVector(IntBuffer buffer, int length) {
    this.buffer = buffer;
    this.length = length;
  }

  public IntBufferVector(IntBuffer buffer, int length, AttributeMap attributes) {
    super(attributes);
    this.buffer = buffer;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int i) {
    return buffer.get(i);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
  @Override
  public int hashCode() {
    return buffer.hashCode();
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new IntBufferVector(buffer, length, attributes);
  }

  /**
   * Returns the underlying {@code IntBuffer} backing this vector. The 
   * returned buffer <strong>absolutely should not be modified!</strong>
   */
  public IntBuffer toIntBufferUnsafe() {
    return buffer;
  }
}
