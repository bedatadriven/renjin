package org.renjin.primitives.time;

import org.joda.time.DateTime;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;


/**
 * Wraps a {@code DoubleVector} that contains instants-in-time stored
 * as the number of seconds since the beginning of the year 1970 in the UTC timezone.
 *
 */
public class PosixCtVector extends TimeVector {
  
  private static final int MILLISECONDS_PER_SECOND = 1000;
  
  private final DoubleVector vector;

  public PosixCtVector(DoubleVector vector) {
    super();
    this.vector = vector;
  }
  
  @Override
  public int length() {
    return vector.length();
  }
  
  @Override
  public DateTime getElementAsDateTime(int i) {
    return new DateTime((long)(vector.getElementAsDouble(i)*MILLISECONDS_PER_SECOND));
  }  
  
  public static class Builder {
    private final DoubleArrayVector.Builder vector = new DoubleArrayVector.Builder();
    
    public Builder add(DateTime dateTime) {
      vector.add(dateTime.getMillis() / 1000);
      return this;
    }
    
    public Builder addNA() {
      vector.addNA();
      return this;
    }

    public Builder addAll(Iterable<DateTime> dateTimes) {
      for(DateTime dateTime : dateTimes) {
        add(dateTime);
      }
      return this;
    }
    
    public DoubleVector buildDoubleVector() {
      vector.setAttribute(Symbols.CLASS, new StringArrayVector("POSIXct", "POSIXt"));
      return vector.build();
    }
  }
}