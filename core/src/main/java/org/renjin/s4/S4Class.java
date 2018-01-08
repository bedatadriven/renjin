/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.s4;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.S4Object;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes an S4 class defined by the methods package.
 */
public class S4Class {

  public static final String PREFIX = ".__C__";

  private final S4Object metadataObject;
  private final String className;
  private final String packageName;

  private List<S4Class> contains;

  public S4Class(String packageName, S4Object metadataObject) {
    this.packageName = packageName;
    this.metadataObject = metadataObject;
    this.className = metadataObject.getAttribute(Symbol.get("className")).asString();
  }

  public S4Object getMetadataObject() {
    return metadataObject;
  }

  public String getClassName() {
    return className;
  }

  public String getPackageName() {
    return packageName;
  }

  public void link(S4Locator locator) {
    assert contains == null : "Already linked";

    contains = new ArrayList<>();

    ListVector contains = (ListVector) metadataObject.getAttribute(Symbol.get("contains"));
    for (SEXP extension : contains) {
      String packageName = extension.getAttribute(Symbol.get("package")).asString();
      String className = extension.getAttribute(Symbol.get("superClass")).asString();
      double distance = extension.getAttribute(Symbol.get("distance")).asReal();

    }
  }
}
