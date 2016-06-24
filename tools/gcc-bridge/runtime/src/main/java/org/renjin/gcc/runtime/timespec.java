package org.renjin.gcc.runtime;

import java.util.concurrent.TimeUnit;

/**
 * Declared in the time.h header.
 */
public class timespec {
  public int tv_sec;
  public int tv_nsec;
  
  public void set(long duration, TimeUnit timeUnit) {
    tv_sec = (int) timeUnit.toSeconds(duration);
    tv_nsec = (int) timeUnit.toNanos(duration - timeUnit.convert(tv_sec, TimeUnit.SECONDS));
  }
  
}
