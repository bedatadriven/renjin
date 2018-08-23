/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.runtime;

import java.util.concurrent.TimeUnit;

/**
 * Declared in the time.h header.
 */
@Deprecated
public class timespec {
  public int tv_sec;
  public int tv_nsec;
  
  public void set(long duration, TimeUnit timeUnit) {
    tv_sec = (int) timeUnit.toSeconds(duration);
    tv_nsec = (int) timeUnit.toNanos(duration - timeUnit.convert(tv_sec, TimeUnit.SECONDS));
  }

}
