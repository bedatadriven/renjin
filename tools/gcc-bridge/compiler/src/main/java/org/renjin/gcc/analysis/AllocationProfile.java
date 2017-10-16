/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.type.GimpleType;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes reads and writes to a block of allocated memory.
 *
 * <p>This information allows us to determine the best way to represent
 * a m'allocated block of memory or a variable on the stack.</p>
 */
public class AllocationProfile {

  private Allocation allocation;
  private Set<GimpleType> reads = new HashSet<>();

  AllocationProfile(Allocation allocation) {
    this.allocation = allocation;
  }

  void addRead(GimpleType type) {

  }
}
