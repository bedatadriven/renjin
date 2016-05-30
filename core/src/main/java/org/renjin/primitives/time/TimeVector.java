package org.renjin.primitives.time;

import org.joda.time.DateTime;
import org.renjin.repackaged.guava.collect.UnmodifiableIterator;

import java.util.Iterator;

public abstract class TimeVector implements Iterable<DateTime> {

  public abstract int length();

  public abstract DateTime getElementAsDateTime(int index);

  @Override
  public Iterator<DateTime> iterator() {
    return new ElementIterator();
  }

  private class ElementIterator extends UnmodifiableIterator<DateTime> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < length();
    }

    @Override
    public DateTime next() {
      return getElementAsDateTime(i++);
    }  
  }
}
