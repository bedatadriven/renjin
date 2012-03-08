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

class SerializationFormat {

  public static final String ASCII_FORMAT = "RDA2\nA\n";
  public static final String BINARY_FORMAT = "RDB2\nB\n";
  public static final String XDR_FORMAT = "RDX2\nX\n";

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
  public static final int  WEAKREFSXP = 23;    /* weak reference */
  public static final int  RAWSXP     = 24;    /* raw bytes */
  public static final int  S4SXP      = 25;    /* S4, non-vector */
  public static final int  FUNSXP    =  99;    /* Closure or Builtin or Special */
  public static final int REFSXP =           255 ;
  public static final int  NILVALUE_SXP  =    254 ;
  public static final int  GLOBALENV_SXP  =   253 ;
  public static final int  UNBOUNDVALUE_SXP =  252;
  public static final int  MISSINGARG_SXP =   251;
  public static final int  BASENAMESPACE_SXP= 250;
  public static final int  NAMESPACESXP=      249;
  public static final int  PACKAGESXP  =      248;
  public static final int  PERSISTSXP   =     247;
  /* the following are speculative--we may or may not need them soon */
  public static final int  CLASSREFSXP  =     246;
  public static final int  GENERICREFSXP  =   245;
  public static final int  EMPTYENV_SXP	= 242;
  public static final int  BASEENV_SXP	=  241;
  static final int CE_NATIVE = 0;
  static final int CE_UTF8   = 1;
  static final int CE_LATIN1 = 2;
  static final int CE_SYMBOL = 5;
  static final int CE_ANY    =99;
  static final int LATIN1_MASK  = (1<<2);
  static final int UTF8_MASK = (1<<3);
  static final int CACHED_MASK = (1<<5);
  static final int  HASHASH_MASK =  1;

  public static final int VERSION2 = 2;

  public static final long XDR_NA_BITS = 0x7ff80000000007a2L;

}
