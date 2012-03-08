/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.io;

class Version {
  private int v, p, s;
  private int packed;

  Version(int packed) {
    this.packed = packed;
    v = this.packed / 65536; packed = packed % 65536;
    p = packed / 256; packed = packed % 256;
    s = packed;
  }

  Version(int v, int p, int s) {
    this.v = v;
    this.p = p;
    this.s = s;
    this.packed = s + (p * 256) + (v * 65536);
  }

  public boolean isExperimental() {
    return packed < 0;
  }

  public int asPacked() {
    return packed;
  }

  @Override
  public String toString() {
    return String.format("%d.%d.%d", v, p, s);
  }
}
