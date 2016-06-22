package org.renjin.primitives.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.renjin.sexp.*;


/**
 * Wraps a {@code DoubleVector} that contains instants-in-time stored
 * as the number of seconds since the beginning of the year 1970 in the UTC timezone.
 *
 */
public class PosixCtVector extends TimeVector {
  
  private static final int MILLISECONDS_PER_SECOND = 1000;
  
  private final DoubleVector vector;

  /**
   * The zone associated with this vector. The timezone does not change the value of
   * the milliseconds in {@code vector}, in is only passed along so that if this object
   * is ever converted to string or a POSIXlt object, it will be converted with the original timezone.
   */
  private final DateTimeZone dateTimeZone;

  public PosixCtVector(DoubleVector vector) {
    super();
    this.vector = vector;
    dateTimeZone = Time.timeZoneFromPosixObject(vector);
  }
  
  @Override
  public int length() {
    return vector.length();
  }
  
  @Override
  public DateTime getElementAsDateTime(int i) {
    return new DateTime((long)(vector.getElementAsDouble(i)*MILLISECONDS_PER_SECOND), dateTimeZone);
  }  
  
  public static class Builder {
    private final DoubleArrayVector.Builder vector;
    public Builder() {
      vector = new DoubleArrayVector.Builder();
    }

    public Builder setTimeZone(SEXP timeZoneAttribute) {
      vector.setAttribute(Symbols.TZONE, timeZoneAttribute);
      return this;
    }

    public Builder(int initialCapacity) {
      vector = new DoubleArrayVector.Builder(0, initialCapacity);
    }

    public Builder add(DateTime dateTime) {
      if(dateTime == null) {
        vector.add(DoubleVector.NA);
      } else {
        vector.add(dateTime.getMillis() / 1000);
      }
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