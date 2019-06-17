package org.renjin.gcc.format;

public interface CharIterator {
  boolean hasMore();

  char peek();

  char next();
}
