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

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Closure;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

/**
 * A Method is a specific implementation of a {@link Generic} for a given classes of arguments.
 */
public class Method {
  private Generic generic;
  private int group;
  private Signature signature;
  private Closure definition;

  public Method(Generic generic, int group, String signature, SEXP definition) {
    this.generic = generic;
    this.group = group;
    this.signature = new Signature(signature);
    this.definition = (Closure) definition;
  }

  public int getSignatureLength() {
    return signature.getLength();
  }

  public Closure getDefinition() {
    return definition;
  }

  public Signature getSignature() {
    return signature;
  }

  public boolean isGroupGeneric() {
    return group != 0;
  }

  /**
   * @return 0 if the method is defined for a specific generic, 1 if the method is defined for a specific group,
   * such as "Arith" or "Logic", and 2 if the method is defined for a general group, like "Ops" or "Summary"
   */
  public int getGroupLevel() {
    return group;
  }

  public PairList getFormals() {
    return definition.getFormals();
  }

  public AtomicVector getFormalNames() {
    return getFormals().getNames();
  }

  @Override
  public String toString() {
    return generic.getName() + "(" + signature + ")";
  }
}
