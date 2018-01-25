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

import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

/**
 * Describes an S4 method
 */
public class S4Method {

  public static final String PREFIX = ".__T__";

  private final SEXP metadataObject;
  private final String generic;
  private final String genericPackage;

  public S4Method(SEXP metadata) {
    this.metadataObject = metadata;
    this.generic = metadata.getAttribute(Symbol.get("generic")).asString();
    this.genericPackage = metadata.getAttribute(Symbol.get("generic")).getAttribute(Symbol.get("package")).asString();
  }

}
