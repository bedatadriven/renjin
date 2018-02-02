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

import org.renjin.repackaged.guava.base.Joiner;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Signature {
  private final String signature;
  private final String[] arguments;

  public Signature(String signature) {
    this.signature = signature;
    this.arguments = signature.split("#");
  }

  public Signature(String[] classes) {
    this.arguments = classes;
    this.signature = Joiner.on('#').join(classes);
  }

  public int getLength() {
    return arguments.length;
  }

  public String[] getArguments() {
    return arguments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Signature signature1 = (Signature) o;
    return Objects.equals(signature, signature1.signature) &&
        Arrays.equals(arguments, signature1.arguments);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(signature);
    result = 31 * result + Arrays.hashCode(arguments);
    return result;
  }

  public String getClass(int index) {
    if(index < arguments.length) {
      return arguments[index];
    } else {
      return "ANY";
    }
  }

  public List<String> getClasses() {
    return Arrays.asList(arguments);
  }
}
