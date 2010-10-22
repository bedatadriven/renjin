/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.parser;

public class Def {

  private static final int PARSE_ERROR_SIZE = 256;      /* Parse error messages saved here */


  /**
   * Largest symbol size, in bytes excluding terminator
   */
  private static int MAXIDSIZE = 256;

  /**
   * Line in file of the above
   */
  public static int R_ParseContextLine = 0;

//
//  extern0 int	R_ParseError	INI_as(0); /* Line where parse error occurred */
//extern0 int	R_ParseErrorCol;    /* Column of start of token where parse error occurred */
//extern0 SEXP	R_ParseErrorFile;   /* Source file where parse error was seen */
//extern0 char	R_ParseErrorMsg[PARSE_ERROR_SIZE] INI_as("");
//#define PARSE_CONTEXT_SIZE 256	    /* Recent parse context kept in a circular buffer */
//extern0 char	R_ParseContext[PARSE_CONTEXT_SIZE] INI_as("");
//extern0 int	R_ParseContextLast INI_as(0); /* last character in context buffer */


}
