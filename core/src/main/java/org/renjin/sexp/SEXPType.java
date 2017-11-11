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

package org.renjin.sexp;


public enum SEXPType {

  NILSXP(0, "NULL"),
  SYMSXP(1, "name"),
  LISTSXP(2, "pairlist"),
  CLOSXP(3, "closure"),
  ENVSXP(4, "environment"),
  PROMSXP(5, "promise"),
  LANGSXP(6, "language"),
  SPECIALSXP(7, "special"),
  BUILTIN(8, "builtin"),
  CHARSXP(9, "char"),
  LGLSXP(10, "logical"),
  INTSXP(13, "integer"),
  REALSXP(14, "double"),
  CPLXSXP(15, "complex"),
  STRSXP(16, "character"),
  DOTSXP(17, "..."),
  VECSXP(19, "list"),
  EXPRSXP(20, "expression"),
  BCODESXP(21, "bytecode"),
  EXTPTRSXP(22, "exteralptr"),
  RAWSXP(24, "raw"),
  S4SXP(25, "S4");

  private final int code;
  private final String name;

  SEXPType(int code, String name) {
    this.code = code;
    this.name = name;
  }

  public String typeName() {
    return name;
  }

  public int code() {
    return code;
  }
}
