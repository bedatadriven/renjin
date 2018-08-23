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
package org.renjin.s4;

import java.util.Arrays;

public class SignatureAndInheritance {
  private final String[] arguments;
  private final boolean[] inheritance;

  SignatureAndInheritance(String[] args, boolean[] inheritance) {
    assert args.length == inheritance.length;
    this.arguments = args;
    this.inheritance = inheritance;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for(int i = 0; i < arguments.length; i++) {
      sb.append(arguments[i]);
      sb.append(":");
      if(inheritance[i]) {
        sb.append("TRUE");
      } else {
        sb.append("FALSE");
      }
      if(i > 0 && (i < arguments.length - 1)) {
        sb.append("|");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignatureAndInheritance that = (SignatureAndInheritance) o;
    return Arrays.equals(arguments, that.arguments) &&
        Arrays.equals(inheritance, that.inheritance);
  }

  @Override
  public int hashCode() {

    int result = Arrays.hashCode(arguments);
    result = 31 * result + Arrays.hashCode(inheritance);
    return result;
  }
}
