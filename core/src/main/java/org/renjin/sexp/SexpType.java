/**
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
package org.renjin.sexp;

/**
 * SEXP Type numerical type codes
 */
public final class SexpType {
  
  private SexpType() { }
  
  public static final int  NILSXP	  =   0;  /* nil = NULL */
  public static final int  SYMSXP	  =   1;	  /* symbols */
  public static final int  LISTSXP	 =    2;	  /* lists of dotted pairs */
  public static final int  CLOSXP	   =  3;	  /* closures */
  public static final int  ENVSXP	   =  4	;  /* environments */
  public static final int  PROMSXP	  =   5	;  /* promises: [un]evaluated closure arguments */
  public static final int  LANGSXP	  =   6;	  /* language constructs (special lists) */
  public static final int  SPECIALSXP =  7;	  /* special forms */
  public static final int  BUILTINSXP =  8;	  /* builtin non-special forms */
  public static final int  CHARSXP	  =   9;	  /* "scalar" string type (internal only)*/
  public static final int  LGLSXP	   = 10	;  /* logical vectors */
  public static final int  INTSXP	   = 13;	  /* integer vectors */
  public static final int  REALSXP	 =   14	;  /* real variables */
  public static final int  CPLXSXP	 =   15;	  /* complex variables */
  public static final int  STRSXP	   = 16	;  /* string vectors */
  public static final int  DOTSXP	   = 17	;  /* dot-dot-dot object */
  public static final int  ANYSXP	   = 18;	  /* make "any" args work.
			     Used in specifying types for symbol
			     registration to mean anything is okay  */
  public static final int  VECSXP	  =  19;	  /* generic vectors */
  public static final int  EXPRSXP	=    20;	  /* expressions vectors */
  public static final int  BCODESXP  =  21;    /* byte code */
  public static final int  EXTPTRSXP  = 22;    /* external pointer */
  public static final int  RAWSXP     = 24;    /* raw bytes */
  public static final int  S4SXP      = 25;    /* S4, non-vector */
  public static final int  FUNSXP    =  99;    /* Closure or Builtin or Special */
  public static final int REFSXP =           255 ;

  private static final String UNKNOWN = "unknown type";
  // @formatter:off
  private static final String[] NAMES = new String[] { 
    "NULL",
    "name",
    "pairlist",
    "closure",
    "environment",
    "promise",
    "language",
    "special",
    "builtin",
    "char",
    "logical",
    UNKNOWN,
    UNKNOWN,
    "integer",
    "numeric",
    "complex",
    "character",
    "...",
    "any",
    "list",
    "expression",
    "bytecode",
    "externalptr",
    UNKNOWN,
    "raw",
    "S4",
  };
  // @formatter:on

  public static final String typeName(int typeIndex) {
    if (typeIndex == REFSXP) {
      return "reference";
    } else if (typeIndex == FUNSXP) {
      return "function";
    } else if (0 <= typeIndex && typeIndex <= S4SXP) {
      return NAMES[typeIndex];
    } else {
      return UNKNOWN;
    }
  }
}
