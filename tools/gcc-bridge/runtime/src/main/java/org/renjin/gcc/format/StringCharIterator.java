package org.renjin.gcc.format;

public class StringCharIterator implements CharIterator {
  private String input;
  private int i = 0;

  public StringCharIterator(String input) {
    this.input = input;
  }

  @Override
  public boolean hasMore() {
    return i < input.length();
  }

  @Override
  public char peek() {
    return input.charAt(i);
  }

  @Override
  public char next() {
    return input.charAt(i++);
  }
}
