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
package org.renjin.primitives.io.serialization;

class SerializationFormat {

  public static final String ASCII_MAGIC_HEADER = "RDA2\n";
  public static final String BINARY_MAGIC_HEADER = "RDB2\n";
  public static final String XDR_MAGIC_HEADER = "RDX2\n";
  
  public static final byte ASCII_FORMAT = 'A';
  public static final byte BINARY_FORMAT = 'B';
  public static final byte XDR_FORMAT = 'X';

  public static final int  WEAKREFSXP = 23;    /* weak reference */
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

  public static final int BCREPDEF = 244;
  public static final int BCREPREF = 243;
  public static final int ATTRLANGSXP = 240;
  public static final int ATTRLISTSXP = 239;
  
  static final int LATIN1_MASK  = (1<<2);
  static final int UTF8_MASK = (1<<3);
  static final int  ASCII_MASK =  (1<<6);
  
  public static final int VERSION2 = 2;

}
