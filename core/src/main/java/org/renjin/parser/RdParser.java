package org.renjin.parser;

import org.renjin.sexp.*;
import org.renjin.util.CDefines;

import java.io.IOException;
import java.io.Reader;

import static org.renjin.util.CDefines.*;


/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton implementation for Bison LALR(1) parsers in Java

      Copyright (C) 2007, 2008 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* First part of user declarations.  */

/* Line 32 of lalr1.java  */
/* Line 2 of "gramRd.y"  */

/*
 *  R : A Computer Langage for Statistical Data Analysis
 *  Copyright (C) 1995, 1996, 1997  Robert Gentleman and Ross Ihaka
 *  Copyright (C) 1997--2008  Robert Gentleman, Ross Ihaka and the
 *                            R Development Core Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
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
 *  http://www.r-project.org/Licenses/
 */


/**
 * A Bison parser, automatically generated from <tt>gramRd.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
public class RdParser
{


  /** Version number for the Bison executable that generated this parser.  */
  public static final String bisonVersion = "2.4.1";

  /** Name of the skeleton that generated this parser.  */
  public static final String bisonSkeleton = "lalr1.java";


  /** True if verbose error messages are enabled.  */
  public boolean errorVerbose = false;


  /**
   * A class defining a pair of positions.  Positions, defined by the
   * <code>Position</code> class, denote a point in the input.
   * Locations represent a part of the input through the beginning
   * and ending positions.  */
  public class Location {
    /** The first, inclusive, position in the range.  */
    public Position begin;

    /** The first position beyond the range.  */
    public Position end;

    /**
     * Create a <code>Location</code> denoting an empty range located at
     * a given point.
     * @param loc The position at which the range is anchored.  */
    public Location (Position loc) {
      this.begin = this.end = loc;
    }

    /**
     * Create a <code>Location</code> from the endpoints of the range.
     * @param begin The first position included in the range.
     * @param end   The first position beyond the range.  */
    public Location (Position begin, Position end) {
      this.begin = begin;
      this.end = end;
    }

    /**
     * Print a representation of the location.  For this to be correct,
     * <code>Position</code> should override the <code>equals</code>
     * method.  */
    public String toString () {
      if (begin.equals (end))
        return begin.toString ();
      else
        return begin.toString () + "-" + end.toString ();
    }
  }



  /** Token returned by the scanner to signal the end of its input.  */
  public static final int EOF = 0;

  /* Tokens.  */
  /** Token number, to be returned by the scanner.  */
  public static final int END_OF_INPUT = 258;
  /** Token number, to be returned by the scanner.  */
  public static final int ERROR = 259;
  /** Token number, to be returned by the scanner.  */
  public static final int SECTIONHEADER = 260;
  /** Token number, to be returned by the scanner.  */
  public static final int RSECTIONHEADER = 261;
  /** Token number, to be returned by the scanner.  */
  public static final int VSECTIONHEADER = 262;
  /** Token number, to be returned by the scanner.  */
  public static final int SECTIONHEADER2 = 263;
  /** Token number, to be returned by the scanner.  */
  public static final int RCODEMACRO = 264;
  /** Token number, to be returned by the scanner.  */
  public static final int SEXPR = 265;
  /** Token number, to be returned by the scanner.  */
  public static final int RDOPTS = 266;
  /** Token number, to be returned by the scanner.  */
  public static final int LATEXMACRO = 267;
  /** Token number, to be returned by the scanner.  */
  public static final int VERBMACRO = 268;
  /** Token number, to be returned by the scanner.  */
  public static final int OPTMACRO = 269;
  /** Token number, to be returned by the scanner.  */
  public static final int ESCAPE = 270;
  /** Token number, to be returned by the scanner.  */
  public static final int LISTSECTION = 271;
  /** Token number, to be returned by the scanner.  */
  public static final int ITEMIZE = 272;
  /** Token number, to be returned by the scanner.  */
  public static final int DESCRIPTION = 273;
  /** Token number, to be returned by the scanner.  */
  public static final int NOITEM = 274;
  /** Token number, to be returned by the scanner.  */
  public static final int LATEXMACRO2 = 275;
  /** Token number, to be returned by the scanner.  */
  public static final int VERBMACRO2 = 276;
  /** Token number, to be returned by the scanner.  */
  public static final int LATEXMACRO3 = 277;
  /** Token number, to be returned by the scanner.  */
  public static final int IFDEF = 278;
  /** Token number, to be returned by the scanner.  */
  public static final int ENDIF = 279;
  /** Token number, to be returned by the scanner.  */
  public static final int TEXT = 280;
  /** Token number, to be returned by the scanner.  */
  public static final int RCODE = 281;
  /** Token number, to be returned by the scanner.  */
  public static final int VERB = 282;
  /** Token number, to be returned by the scanner.  */
  public static final int COMMENT = 283;
  /** Token number, to be returned by the scanner.  */
  public static final int UNKNOWN = 284;
  /** Token number, to be returned by the scanner.  */
  public static final int STARTFILE = 285;
  /** Token number, to be returned by the scanner.  */
  public static final int STARTFRAGMENT = 286;




  private Location yylloc (YYStack rhs, int n)
  {
    if (n > 0)
      return new Location (rhs.locationAt (1).begin, rhs.locationAt (n).end);
    else
      return new Location (rhs.locationAt (0).end);
  }

  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>YYParser</tt>.
   */
  public interface Lexer {
    /**
     * Method to retrieve the beginning position of the last scanned token.
     * @return the position at which the last scanned token starts.  */
    Position getStartPos ();

    /**
     * Method to retrieve the ending position of the last scanned token.
     * @return the first position beyond the last scanned token.  */
    Position getEndPos ();

    /**
     * Method to retrieve the semantic value of the last scanned token.
     * @return the semantic value of the last scanned token.  */
    Object getLVal ();

    /**
     * Entry point for the scanner.  Returns the token identifier corresponding
     * to the next token and prepares to return the semantic value
     * and beginning/ending positions of the token. 
     * @return the token identifier corresponding to the next token. */
    int yylex () throws java.io.IOException;

    /**
     * Entry point for error reporting.  Emits an error
     * referring to the given location in a user-defined way.
     *
     * @param loc The location of the element to which the
     *                error message is related
     * @param s The string for the error message.  */
    void yyerror (Location loc, String s);
  }

  /** The object doing lexical analysis for us.  */
  private Lexer yylexer;





  /**
   * Instantiates the Bison-generated parser.
   * @param yylexer The scanner that will supply tokens to the parser.
   */
  public RdParser() {
    this.yylexer = new RdLexer();

  }

  private java.io.PrintStream yyDebugStream = System.err;

  /**
   * Return the <tt>PrintStream</tt> on which the debugging output is
   * printed.
   */
  public final java.io.PrintStream getDebugStream () { return yyDebugStream; }

  /**
   * Set the <tt>PrintStream</tt> on which the debug output is printed.
   * @param s The stream that is used for debugging output.
   */
  public final void setDebugStream(java.io.PrintStream s) { yyDebugStream = s; }

  private int yydebug = 0;

  /**
   * Answer the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   */
  public final int getDebugLevel() { return yydebug; }

  /**
   * Set the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   * @param level The verbosity level for debugging output.
   */
  public final void setDebugLevel(int level) { yydebug = level; }

  private final int yylex () throws java.io.IOException {
    return yylexer.yylex ();
  }
  protected final void yyerror (Location loc, String s) {
    yylexer.yyerror (loc, s);
  }


  protected final void yyerror (String s) {
    yylexer.yyerror ((Location)null, s);
  }
  protected final void yyerror (Position loc, String s) {
    yylexer.yyerror (new Location (loc), s);
  }

  protected final void yycdebug (String s) {
    if (yydebug > 0)
      yyDebugStream.println (s);
  }

  private final class YYStack {
    private int[] stateStack = new int[16];
    private Location[] locStack = new Location[16];
    private Object[] valueStack = new Object[16];

    public int size = 16;
    public int height = -1;

    public final void push (int state, Object value, Location loc) {
      height++;
      if (size == height) 
      {
        int[] newStateStack = new int[size * 2];
        System.arraycopy (stateStack, 0, newStateStack, 0, height);
        stateStack = newStateStack;

        Location[] newLocStack = new Location[size * 2];
        System.arraycopy (locStack, 0, newLocStack, 0, height);
        locStack = newLocStack;

        Object[] newValueStack = new Object[size * 2];
        System.arraycopy (valueStack, 0, newValueStack, 0, height);
        valueStack = newValueStack;

        size *= 2;
      }

      stateStack[height] = state;
      locStack[height] = loc;
      valueStack[height] = value;
    }

    public final void pop () {
      height--;
    }

    public final void pop (int num) {
      // Avoid memory leaks... garbage collection is a white lie!
      if (num > 0) {
        java.util.Arrays.fill (valueStack, height - num + 1, height, null);
        java.util.Arrays.fill (locStack, height - num + 1, height, null);
      }
      height -= num;
    }

    public final int stateAt (int i) {
      return stateStack[height - i];
    }

    public final Location locationAt (int i) {
      return locStack[height - i];
    }

    public final SEXP valueAt (int i) {
      return (SEXP)valueStack[height - i];
    }

    // Print the state stack on the debug stream.
    public void print (java.io.PrintStream out)
    {
      out.print ("Stack now");

      for (int i = 0; i < height; i++)
      {
        out.print (' ');
        out.print (stateStack[i]);
      }
      out.println ();
    }
  }

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return success (<tt>true</tt>).  */
  public static final int YYACCEPT = 0;

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return failure (<tt>false</tt>).  */
  public static final int YYABORT = 1;

  /**
   * Returned by a Bison action in order to start error recovery without
   * printing an error message.  */
  public static final int YYERROR = 2;

  /**
   * Returned by a Bison action in order to print an error message and start
   * error recovery.  */
  public static final int YYFAIL = 3;

  private static final int YYNEWSTATE = 4;
  private static final int YYDEFAULT = 5;
  private static final int YYREDUCE = 6;
  private static final int YYERRLAB1 = 7;
  private static final int YYRETURN = 8;

  private int yyerrstatus_ = 0;

  /**
   * Return whether error recovery is being done.  In this state, the parser
   * reads token until it reaches a known state, and then restarts normal
   * operation.  */
  public final boolean recovering ()
  {
    return yyerrstatus_ == 0;
  }

  private int yyaction (int yyn, YYStack yystack, int yylen) 
  {
    Object yyval;
    Location yyloc = yylloc (yystack, yylen);

    /* If YYLEN is nonzero, implement the default value of the action:
       `$$ = $1'.  Otherwise, use the top of the stack.

       Otherwise, the following line sets YYVAL to garbage.
       This behavior is undocumented and Bison
       users should not rely upon it.  */
    if (yylen > 0)
      yyval = yystack.valueAt (yylen - 1);
    else
      yyval = yystack.valueAt (0);

    yy_reduce_print (yyn, yystack);

    switch (yyn)
    {
    case 2:
      if (yyn == 2)

        /* Line 353 of lalr1.java  */
        /* Line 181 of "gramRd.y"  */
      { xxsavevalue(((yystack.valueAt (3-(2)))), yyloc); UNPROTECT_PTR(((yystack.valueAt (3-(1))))); return 0; };
      break;


    case 3:
      if (yyn == 3)

        /* Line 353 of lalr1.java  */
        /* Line 182 of "gramRd.y"  */
      { xxsavevalue(((yystack.valueAt (3-(2)))), yyloc); UNPROTECT_PTR(((yystack.valueAt (3-(1))))); return 0; };
      break;


    case 4:
      if (yyn == 4)

        /* Line 353 of lalr1.java  */
        /* Line 183 of "gramRd.y"  */
      { PROTECT(Value = R_NilValue);  /*YYABORT;*/ return 1; };
      break;


    case 5:
      if (yyn == 5)

        /* Line 353 of lalr1.java  */
        /* Line 186 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (2-(2)))); UNPROTECT_PTR(((yystack.valueAt (2-(1))))); };
      break;


    case 6:
      if (yyn == 6)

        /* Line 353 of lalr1.java  */
        /* Line 189 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (1-(1)))); };
      break;


    case 7:
      if (yyn == 7)

        /* Line 353 of lalr1.java  */
        /* Line 192 of "gramRd.y"  */
      { yyval = xxnewlist(((yystack.valueAt (1-(1))))); };
      break;


    case 8:
      if (yyn == 8)

        /* Line 353 of lalr1.java  */
        /* Line 193 of "gramRd.y"  */
      { yyval = xxlist(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2))))); };
      break;


    case 9:
      if (yyn == 9)

        /* Line 353 of lalr1.java  */
        /* Line 195 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 10:
      if (yyn == 10)

        /* Line 353 of lalr1.java  */
        /* Line 196 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), HAS_SEXPR, yyloc); };
      break;


    case 11:
      if (yyn == 11)

        /* Line 353 of lalr1.java  */
        /* Line 197 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 12:
      if (yyn == 12)

        /* Line 353 of lalr1.java  */
        /* Line 198 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 13:
      if (yyn == 13)

        /* Line 353 of lalr1.java  */
        /* Line 199 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 14:
      if (yyn == 14)

        /* Line 353 of lalr1.java  */
        /* Line 200 of "gramRd.y"  */
      { yyval = xxmarkup2(((yystack.valueAt (3-(1)))), ((yystack.valueAt (3-(2)))), ((yystack.valueAt (3-(3)))), 2, STATIC, yyloc); };
      break;


    case 15:
      if (yyn == 15)

        /* Line 353 of lalr1.java  */
        /* Line 201 of "gramRd.y"  */
      { yyval = xxmarkup2(((yystack.valueAt (4-(1)))), ((yystack.valueAt (4-(2)))), ((yystack.valueAt (4-(3)))), 2, HAS_IFDEF, yyloc); UNPROTECT_PTR(((yystack.valueAt (4-(4))))); };
      break;


    case 16:
      if (yyn == 16)

        /* Line 353 of lalr1.java  */
        /* Line 202 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (3-(1)))), ((yystack.valueAt (3-(3)))), HAS_SEXPR, yyloc); xxpopMode(((yystack.valueAt (3-(2))))); };
      break;


    case 17:
      if (yyn == 17)

        /* Line 353 of lalr1.java  */
        /* Line 203 of "gramRd.y"  */
      { yyval = xxOptionmarkup(((yystack.valueAt (4-(1)))), ((yystack.valueAt (4-(3)))), ((yystack.valueAt (4-(4)))), STATIC, yyloc); xxpopMode(((yystack.valueAt (4-(2))))); };
      break;


    case 18:
      if (yyn == 18)

        /* Line 353 of lalr1.java  */
        /* Line 204 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), COMMENT, yyloc); };
      break;


    case 19:
      if (yyn == 19)

        /* Line 353 of lalr1.java  */
        /* Line 205 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), TEXT, yyloc); };
      break;


    case 20:
      if (yyn == 20)

        /* Line 353 of lalr1.java  */
        /* Line 206 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 21:
      if (yyn == 21)

        /* Line 353 of lalr1.java  */
        /* Line 208 of "gramRd.y"  */
      { yyval = xxnewlist(((yystack.valueAt (1-(1))))); };
      break;


    case 22:
      if (yyn == 22)

        /* Line 353 of lalr1.java  */
        /* Line 209 of "gramRd.y"  */
      { yyval = xxlist(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2))))); };
      break;


    case 23:
      if (yyn == 23)

        /* Line 353 of lalr1.java  */
        /* Line 211 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), TEXT, yyloc); };
      break;


    case 24:
      if (yyn == 24)

        /* Line 353 of lalr1.java  */
        /* Line 212 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), RCODE, yyloc); };
      break;


    case 25:
      if (yyn == 25)

        /* Line 353 of lalr1.java  */
        /* Line 213 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), VERB, yyloc); };
      break;


    case 26:
      if (yyn == 26)

        /* Line 353 of lalr1.java  */
        /* Line 214 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), COMMENT, yyloc); };
      break;


    case 27:
      if (yyn == 27)

        /* Line 353 of lalr1.java  */
        /* Line 215 of "gramRd.y"  */
      { yyval = yystack.valueStack[0] = xxtag(((yystack.valueAt (1-(1)))), UNKNOWN, yyloc); yyerror(yyunknown); };
      break;


    case 28:
      if (yyn == 28)

        /* Line 353 of lalr1.java  */
        /* Line 216 of "gramRd.y"  */
      { yyval = xxmarkup(R_NilValue, ((yystack.valueAt (1-(1)))), STATIC, yyloc); };
      break;


    case 29:
      if (yyn == 29)

        /* Line 353 of lalr1.java  */
        /* Line 217 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (1-(1)))); };
      break;


    case 30:
      if (yyn == 30)

        /* Line 353 of lalr1.java  */
        /* Line 218 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 31:
      if (yyn == 31)

        /* Line 353 of lalr1.java  */
        /* Line 220 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 32:
      if (yyn == 32)

        /* Line 353 of lalr1.java  */
        /* Line 221 of "gramRd.y"  */
      { yyval = xxmarkup2(((yystack.valueAt (3-(1)))), ((yystack.valueAt (3-(2)))), ((yystack.valueAt (3-(3)))), 2, STATIC, yyloc); };
      break;


    case 33:
      if (yyn == 33)

        /* Line 353 of lalr1.java  */
        /* Line 222 of "gramRd.y"  */
      { yyval = xxmarkup3(((yystack.valueAt (4-(1)))), ((yystack.valueAt (4-(2)))), ((yystack.valueAt (4-(3)))), ((yystack.valueAt (4-(4)))), STATIC, yyloc); };
      break;


    case 34:
      if (yyn == 34)

        /* Line 353 of lalr1.java  */
        /* Line 223 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 35:
      if (yyn == 35)

        /* Line 353 of lalr1.java  */
        /* Line 224 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 36:
      if (yyn == 36)

        /* Line 353 of lalr1.java  */
        /* Line 225 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (3-(1)))), ((yystack.valueAt (3-(3)))), STATIC, yyloc); xxpopMode(((yystack.valueAt (3-(2))))); };
      break;


    case 37:
      if (yyn == 37)

        /* Line 353 of lalr1.java  */
        /* Line 226 of "gramRd.y"  */
      { yyval = xxOptionmarkup(((yystack.valueAt (4-(1)))), ((yystack.valueAt (4-(3)))), ((yystack.valueAt (4-(4)))), STATIC, yyloc); xxpopMode(((yystack.valueAt (4-(2))))); };
      break;


    case 38:
      if (yyn == 38)

        /* Line 353 of lalr1.java  */
        /* Line 227 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 39:
      if (yyn == 39)

        /* Line 353 of lalr1.java  */
        /* Line 228 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (3-(1)))), ((yystack.valueAt (3-(3)))), HAS_SEXPR, yyloc); xxpopMode(((yystack.valueAt (3-(2))))); };
      break;


    case 40:
      if (yyn == 40)

        /* Line 353 of lalr1.java  */
        /* Line 229 of "gramRd.y"  */
      { yyval = xxOptionmarkup(((yystack.valueAt (4-(1)))), ((yystack.valueAt (4-(3)))), ((yystack.valueAt (4-(4)))), HAS_SEXPR, yyloc); xxpopMode(((yystack.valueAt (4-(2))))); };
      break;


    case 41:
      if (yyn == 41)

        /* Line 353 of lalr1.java  */
        /* Line 230 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), STATIC, yyloc); };
      break;


    case 42:
      if (yyn == 42)

        /* Line 353 of lalr1.java  */
        /* Line 231 of "gramRd.y"  */
      { yyval = xxmarkup2(((yystack.valueAt (2-(1)))), ((yystack.valueAt (2-(2)))), R_NilValue, 1, STATIC, yyloc); };
      break;


    case 43:
      if (yyn == 43)

        /* Line 353 of lalr1.java  */
        /* Line 232 of "gramRd.y"  */
      { yyval = xxmarkup2(((yystack.valueAt (3-(1)))), ((yystack.valueAt (3-(2)))), ((yystack.valueAt (3-(3)))), 2, STATIC, yyloc); };
      break;


    case 44:
      if (yyn == 44)

        /* Line 353 of lalr1.java  */
        /* Line 233 of "gramRd.y"  */
      { yyval = xxmarkup(((yystack.valueAt (1-(1)))), R_NilValue, STATIC, yyloc); };
      break;


    case 45:
      if (yyn == 45)

        /* Line 353 of lalr1.java  */
        /* Line 234 of "gramRd.y"  */
      { yyval = xxmarkup2(((yystack.valueAt (4-(1)))), ((yystack.valueAt (4-(2)))), ((yystack.valueAt (4-(3)))), 2, HAS_IFDEF, yyloc); UNPROTECT_PTR(((yystack.valueAt (4-(4))))); };
      break;


    case 46:
      if (yyn == 46)

        /* Line 353 of lalr1.java  */
        /* Line 236 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 47:
      if (yyn == 47)

        /* Line 353 of lalr1.java  */
        /* Line 238 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 48:
      if (yyn == 48)

        /* Line 353 of lalr1.java  */
        /* Line 239 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = xxnewlist(((yystack.valueAt (2-(2))))); 
//      if(wCalls)
        System.out.println(String.format(_("bad markup (extra space?) at %s:%d:%d"), 
            xxBasename, yystack.locationAt (2-(2)).begin.line, yystack.locationAt (2-(2)).begin.column)); 
//      else
//        warningcall(R_NilValue, _("bad markup (extra space?) at %s:%d:%d"), 
//            xxBasename, yystack.locationAt (2-(2)).begin.line, yystack.locationAt (2-(2)).begin.column); 
      };
      break;


    case 49:
      if (yyn == 49)

        /* Line 353 of lalr1.java  */
        /* Line 248 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 50:
      if (yyn == 50)

        /* Line 353 of lalr1.java  */
        /* Line 250 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 51:
      if (yyn == 51)

        /* Line 353 of lalr1.java  */
        /* Line 252 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 52:
      if (yyn == 52)

        /* Line 353 of lalr1.java  */
        /* Line 256 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (4-(2))))); yyval = ((yystack.valueAt (4-(3)))); };
      break;


    case 53:
      if (yyn == 53)

        /* Line 353 of lalr1.java  */
        /* Line 257 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (3-(2))))); yyval = xxnewlist(Null.INSTANCE); };
      break;


    case 54:
      if (yyn == 54)

        /* Line 353 of lalr1.java  */
        /* Line 259 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 55:
      if (yyn == 55)

        /* Line 353 of lalr1.java  */
        /* Line 261 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = ((yystack.valueAt (2-(2)))); };
      break;


    case 56:
      if (yyn == 56)

        /* Line 353 of lalr1.java  */
        /* Line 265 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (4-(2))))); yyval = ((yystack.valueAt (4-(3)))); };
      break;


    case 57:
      if (yyn == 57)

        /* Line 353 of lalr1.java  */
        /* Line 266 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (3-(2))))); yyval = xxnewlist(Null.INSTANCE); };
      break;


    case 58:
      if (yyn == 58)

        /* Line 353 of lalr1.java  */
        /* Line 268 of "gramRd.y"  */
      { xxpopMode(((yystack.valueAt (2-(1))))); yyval = xxnewlist(((yystack.valueAt (2-(2))))); };
      break;


    case 59:
      if (yyn == 59)

        /* Line 353 of lalr1.java  */
        /* Line 271 of "gramRd.y"  */
      { yyval = xxpushMode(LATEXLIKE, UNKNOWN, false); };
      break;


    case 60:
      if (yyn == 60)

        /* Line 353 of lalr1.java  */
        /* Line 273 of "gramRd.y"  */
      { yyval = xxpushMode(RLIKE, UNKNOWN, false); };
      break;


    case 61:
      if (yyn == 61)

        /* Line 353 of lalr1.java  */
        /* Line 275 of "gramRd.y"  */
      { xxbraceDepth--; yyval = xxpushMode(RLIKE, UNKNOWN, false); xxbraceDepth++; };
      break;


    case 62:
      if (yyn == 62)

        /* Line 353 of lalr1.java  */
        /* Line 277 of "gramRd.y"  */
      { yyval = xxpushMode(INOPTION, UNKNOWN, false); };
      break;


    case 63:
      if (yyn == 63)

        /* Line 353 of lalr1.java  */
        /* Line 279 of "gramRd.y"  */
      { yyval = xxpushMode(VERBATIM, UNKNOWN, false); };
      break;


    case 64:
      if (yyn == 64)

        /* Line 353 of lalr1.java  */
        /* Line 281 of "gramRd.y"  */
      { yyval = xxpushMode(VERBATIM, UNKNOWN, true); };
      break;


    case 65:
      if (yyn == 65)

        /* Line 353 of lalr1.java  */
        /* Line 283 of "gramRd.y"  */
      { xxbraceDepth--; yyval = xxpushMode(VERBATIM, UNKNOWN, false); xxbraceDepth++; };
      break;


    case 66:
      if (yyn == 66)

        /* Line 353 of lalr1.java  */
        /* Line 285 of "gramRd.y"  */
      { yyval = xxpushMode(LATEXLIKE, ESCAPE, false); };
      break;


    case 67:
      if (yyn == 67)

        /* Line 353 of lalr1.java  */
        /* Line 287 of "gramRd.y"  */
      { yyval = xxpushMode(LATEXLIKE, LATEXMACRO2, false); };
      break;


    case 68:
      if (yyn == 68)

        /* Line 353 of lalr1.java  */
        /* Line 289 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (3-(2)))); };
      break;


    case 69:
      if (yyn == 69)

        /* Line 353 of lalr1.java  */
        /* Line 290 of "gramRd.y"  */
      { yyval = xxnewlist(Null.INSTANCE); };
      break;


    case 70:
      if (yyn == 70)

        /* Line 353 of lalr1.java  */
        /* Line 291 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (4-(2)))); };
      break;


    case 71:
      if (yyn == 71)

        /* Line 353 of lalr1.java  */
        /* Line 292 of "gramRd.y"  */
      { yyval = xxnewlist(Null.INSTANCE); };
      break;


    case 72:
      if (yyn == 72)

        /* Line 353 of lalr1.java  */
        /* Line 293 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (4-(2)))); };
      break;


    case 73:
      if (yyn == 73)

        /* Line 353 of lalr1.java  */
        /* Line 295 of "gramRd.y"  */
      { yyval = ((yystack.valueAt (3-(2)))); };
      break;




      /* Line 353 of lalr1.java  */
      /* Line 1208 of "gramRd.java"  */
    default: break;
    }

    yy_symbol_print ("-> $$ =", yyr1_[yyn], yyval, yyloc);

    yystack.pop (yylen);
    yylen = 0;

    /* Shift the result of the reduction.  */
    yyn = yyr1_[yyn];
    int yystate = yypgoto_[yyn - yyntokens_] + yystack.stateAt (0);
    if (0 <= yystate && yystate <= yylast_
        && yycheck_[yystate] == yystack.stateAt (0))
      yystate = yytable_[yystate];
    else
      yystate = yydefgoto_[yyn - yyntokens_];

    yystack.push (yystate, yyval, yyloc);
    return YYNEWSTATE;
  }

  /* Return YYSTR after stripping away unnecessary quotes and
     backslashes, so that it's suitable for yyerror.  The heuristic is
     that double-quoting is unnecessary unless the string contains an
     apostrophe, a comma, or backslash (other than backslash-backslash).
     YYSTR is taken from yytname_.  */
  private final String yytname_rr_ (String yystr){
    if (yystr.charAt (0) == '"')
    {
      StringBuffer yyr = new StringBuffer ();
      strip_quotes: for (int i = 1; i < yystr.length (); i++)
        switch (yystr.charAt (i))
        {
        case '\'':
        case ',':
          break strip_quotes;

        case '\\':
          if (yystr.charAt(++i) != '\\')
            break strip_quotes;
          /* Fall through.  */
        default:
          yyr.append (yystr.charAt (i));
          break;

        case '"':
          return yyr.toString ();
        }
    }
    else if (yystr.equals ("$end"))
      return "end of input";

    return yystr;
  }

  /*--------------------------------.
  | Print this symbol on YYOUTPUT.  |
  `--------------------------------*/

  private void yy_symbol_print (String s, int yytype,
      Object yyvaluep				 , Object yylocationp)
  {
    if (yydebug > 0)
      yycdebug (s + (yytype < yyntokens_ ? " token " : " nterm ")
          + yytname__[yytype] + " ("
          + yylocationp + ": "
          + (yyvaluep == null ? "(null)" : yyvaluep.toString ()) + ")");
  }

  /**
   * Parse input from the scanner that was specified at object construction
   * time.  Return whether the end of the input was reached successfully.
   *
   * @return <tt>true</tt> if the parsing succeeds.  Note that this does not
   *          imply that there were no syntax errors.
   */
  public boolean parse () throws java.io.IOException
  {
    /// Lookahead and lookahead in internal form.
    int yychar = yyempty_;
    int yytoken = 0;

    /* State.  */
    int yyn = 0;
    int yylen = 0;
    int yystate = 0;

    YYStack yystack = new YYStack ();

    /* Error handling.  */
    int yynerrs_ = 0;
    /// The location where the error started.
    Location yyerrloc = null;

    /// Location of the lookahead.
    Location yylloc = new Location (null, null);

    /// @$.
    Location yyloc;

    /// Semantic value of the lookahead.
    Object yylval = null;

    int yyresult;

    yycdebug ("Starting parse\n");
    yyerrstatus_ = 0;


    /* Initialize the stack.  */
    yystack.push (yystate, yylval, yylloc);

    int label = YYNEWSTATE;
    for (;;)
      switch (label)
      {
      /* New state.  Unlike in the C/C++ skeletons, the state is already
	   pushed when we come here.  */
      case YYNEWSTATE:
        yycdebug ("Entering state " + yystate + "\n");
        if (yydebug > 0)
          yystack.print (yyDebugStream);

        /* Accept?  */
        if (yystate == yyfinal_)
          return true;

        /* Take a decision.  First try without lookahead.  */
        yyn = yypact_[yystate];
        if (yyn == yypact_ninf_)
        {
          label = YYDEFAULT;
          break;
        }

        /* Read a lookahead token.  */
        if (yychar == yyempty_)
        {
          yycdebug ("Reading a token: ");
          yychar = yylex ();

          yylloc = new Location(yylexer.getStartPos (),
              yylexer.getEndPos ());
          yylval = yylexer.getLVal ();
        }

        /* Convert token to internal form.  */
        if (yychar <= EOF)
        {
          yychar = yytoken = EOF;
          yycdebug ("Now at end of input.\n");
        }
        else
        {
          yytoken = yytranslate_ (yychar);
          yy_symbol_print ("Next token is", yytoken,
              yylval, yylloc);
        }

        /* If the proper action on seeing token YYTOKEN is to reduce or to
           detect an error, take that action.  */
        yyn += yytoken;
        if (yyn < 0 || yylast_ < yyn || yycheck_[yyn] != yytoken)
          label = YYDEFAULT;

        /* <= 0 means reduce or error.  */
        else if ((yyn = yytable_[yyn]) <= 0)
        {
          if (yyn == 0 || yyn == yytable_ninf_)
            label = YYFAIL;
          else
          {
            yyn = -yyn;
            label = YYREDUCE;
          }
        }

        else
        {
          /* Shift the lookahead token.  */
          yy_symbol_print ("Shifting", yytoken,
              yylval, yylloc);

          /* Discard the token being shifted.  */
          yychar = yyempty_;

          /* Count tokens shifted since error; after three, turn off error
               status.  */
          if (yyerrstatus_ > 0)
            --yyerrstatus_;

          yystate = yyn;
          yystack.push (yystate, yylval, yylloc);
          label = YYNEWSTATE;
        }
        break;

        /*-----------------------------------------------------------.
      | yydefault -- do the default action for the current state.  |
      `-----------------------------------------------------------*/
      case YYDEFAULT:
        yyn = yydefact_[yystate];
        if (yyn == 0)
          label = YYFAIL;
        else
          label = YYREDUCE;
        break;

        /*-----------------------------.
      | yyreduce -- Do a reduction.  |
      `-----------------------------*/
      case YYREDUCE:
        yylen = yyr2_[yyn];
        label = yyaction (yyn, yystack, yylen);
        yystate = yystack.stateAt (0);
        break;

        /*------------------------------------.
      | yyerrlab -- here on detecting error |
      `------------------------------------*/
      case YYFAIL:
        /* If not already recovering from an error, report this error.  */
        if (yyerrstatus_ == 0)
        {
          ++yynerrs_;
          yyerror (yylloc, yysyntax_error (yystate, yytoken));
        }

        yyerrloc = yylloc;
        if (yyerrstatus_ == 3)
        {
          /* If just tried and failed to reuse lookahead token after an
	     error, discard it.  */

          if (yychar <= EOF)
          {
            /* Return failure if at end of input.  */
            if (yychar == EOF)
              return false;
          }
          else
            yychar = yyempty_;
        }

        /* Else will try to reuse lookahead token after shifting the error
           token.  */
        label = YYERRLAB1;
        break;

        /*---------------------------------------------------.
      | errorlab -- error raised explicitly by YYERROR.  |
      `---------------------------------------------------*/
      case YYERROR:

        yyerrloc = yystack.locationAt (yylen - 1);
        /* Do not reclaim the symbols of the rule which action triggered
           this YYERROR.  */
        yystack.pop (yylen);
        yylen = 0;
        yystate = yystack.stateAt (0);
        label = YYERRLAB1;
        break;

        /*-------------------------------------------------------------.
      | yyerrlab1 -- common code for both syntax error and YYERROR.  |
      `-------------------------------------------------------------*/
      case YYERRLAB1:
        yyerrstatus_ = 3;	/* Each real token shifted decrements this.  */

        for (;;)
        {
          yyn = yypact_[yystate];
          if (yyn != yypact_ninf_)
          {
            yyn += yyterror_;
            if (0 <= yyn && yyn <= yylast_ && yycheck_[yyn] == yyterror_)
            {
              yyn = yytable_[yyn];
              if (0 < yyn)
                break;
            }
          }

          /* Pop the current state because it cannot handle the error token.  */
          if (yystack.height == 1)
            return false;

          yyerrloc = yystack.locationAt (0);
          yystack.pop ();
          yystate = yystack.stateAt (0);
          if (yydebug > 0)
            yystack.print (yyDebugStream);
        }


        /* Muck with the stack to setup for yylloc.  */
        yystack.push (0, null, yylloc);
        yystack.push (0, null, yyerrloc);
        yyloc = yylloc (yystack, 2);
        yystack.pop (2);

        /* Shift the error token.  */
        yy_symbol_print ("Shifting", yystos_[yyn],
            yylval, yyloc);

        yystate = yyn;
        yystack.push (yyn, yylval, yyloc);
        label = YYNEWSTATE;
        break;

        /* Accept.  */
      case YYACCEPT:
        return true;

        /* Abort.  */
      case YYABORT:
        return false;
      }
  }

  // Generate an error message.
  private String yysyntax_error (int yystate, int tok)
  {
    if (errorVerbose)
    {
      int yyn = yypact_[yystate];
      if (yypact_ninf_ < yyn && yyn <= yylast_)
      {
        StringBuffer res;

        /* Start YYX at -YYN if negative to avoid negative indexes in
	       YYCHECK.  */
        int yyxbegin = yyn < 0 ? -yyn : 0;

        /* Stay within bounds of both yycheck and yytname_.  */
        int yychecklim = yylast_ - yyn + 1;
        int yyxend = yychecklim < yyntokens_ ? yychecklim : yyntokens_;
        int count = 0;
        for (int x = yyxbegin; x < yyxend; ++x)
          if (yycheck_[x + yyn] == x && x != yyterror_)
            ++count;

        // FIXME: This method of building the message is not compatible
        // with internationalization.
        res = new StringBuffer ("syntax error, unexpected ");
        res.append (yytname_rr_ (yytname__[tok]));
        if (count < 5)
        {
          count = 0;
          for (int x = yyxbegin; x < yyxend; ++x)
            if (yycheck_[x + yyn] == x && x != yyterror_)
            {
              res.append (count++ == 0 ? ", expecting " : " or ");
              res.append (yytname_rr_ (yytname__[x]));
            }
        }
        return res.toString ();
      }
    }

    return "syntax error";
  }


  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
  private static final short yypact_ninf_ = -71;
  private static final short yypact_[] =
    {
    19,   -71,   462,   -71,     2,   462,   -71,   -71,   -71,   -71,
    -71,   -71,   -71,   -71,   -71,   -71,     8,   412,   -71,    14,
    382,   -71,   -71,   -71,    -9,   -71,    -9,   -71,    -9,   -71,
    -1,   -71,   -71,    -9,   462,     4,   -71,   -71,   -71,   382,
    -71,   -71,   -71,   -71,   -71,   -71,   -71,   -71,   -71,   -71,
    -71,   -71,   -71,   -71,   -71,   -71,   -71,   117,   324,   -71,
    -71,   -71,   -71,   -71,   -71,   -71,   -13,   -71,   382,   -71,
    -2,   -71,   437,   -71,   -71,   -71,    -1,   -71,   -71,     5,
    -71,    -9,   -71,   -71,     3,    -9,   -71,   382,   146,   -71,
    175,   -71,   -71,   -71,   204,     6,   -71,   -71,   -71,    -2,
    -71,   -71,   -71,   -71,   -71,   -71,   -71,   -71,   353,   -71,
    88,   -71,   -71,   233,   -71,   -71,   -71,   262,   -71,   -71,
    -71,   -71,   -71,   -71,   291,   -71
    };

  /* YYDEFACT[S] -- default rule to reduce with in state S when YYTABLE
     doesn't specify something else to do.  Zero means the default is an
     error.  */
  private static final byte yydefact_[] =
    {
    0,     4,     0,    59,     0,     0,    59,    60,    63,    59,
    62,    63,    67,    59,    19,    18,     0,     0,     7,     0,
    0,     1,    20,    12,     0,    11,     0,     9,     0,    59,
    0,    10,    13,     0,     0,     0,     2,     8,     3,     0,
    60,    62,    59,    63,    62,    44,    66,    67,    59,    64,
    59,    59,    23,    24,    25,    26,    27,     0,     0,    21,
    29,    28,    46,    51,    54,    14,     0,    61,     0,    16,
    0,    50,     0,    58,    30,    38,     0,    31,    41,    59,
    34,     0,    35,    59,    42,     0,    59,     0,     0,    69,
    0,    22,    48,    47,     0,     0,    17,    15,    39,     0,
    36,    59,    49,    32,    65,    43,    55,    59,     0,    71,
    0,    68,    53,     0,    73,    40,    37,     0,    33,    45,
    72,    70,    52,    57,     0,    56
    };

  /* YYPGOTO[NTERM-NUM].  */
  private static final byte yypgoto_[] =
    {
    -71,   -71,   -71,   -71,     9,     1,   -49,   -36,   -71,    -8,
    -70,   -71,    10,    11,   -55,    -7,   -71,   -71,    -5,    -3,
    -71,   -71,   -17,   -71,   -71,   -71,   -71,   -71,   -19,   -51
    };

  /* YYDEFGOTO[NTERM-NUM].  */
  private static final byte
  yydefgoto_[] =
{
    -1,     4,    19,    16,    17,    18,    58,    59,    60,    23,
    65,    80,    32,    25,    69,    27,    84,   105,    34,    24,
    26,    94,    30,    28,    85,   117,    81,    33,    61,    70
};

  /* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule which
     number is the opposite.  If zero, do what YYDEFACT says.  */
  private static final byte yytable_ninf_ = -7;
  private static final byte
  yytable_[] =
{
    20,    29,    21,    74,    31,    62,    22,    63,    90,    64,
    35,    36,    92,   103,    71,    96,   107,    38,    37,    57,
    1,    98,    91,    57,    76,    99,    66,    79,   101,    73,
    67,    67,    95,    68,    77,   104,    78,   118,   108,    68,
    83,   114,    86,    72,   115,   113,    87,    93,    35,     2,
    3,    75,    74,     0,    91,     0,     0,    82,     0,     0,
    0,     0,   102,     0,     0,     0,   106,     0,   124,     0,
    0,   100,    91,    37,    74,     0,     0,    91,     0,     0,
    66,     0,     0,    66,     0,     0,     0,     0,    91,    39,
    0,   120,     0,   116,     0,     0,     0,    40,    41,     0,
    42,    43,    44,    45,    66,    46,    47,     0,    48,    49,
    50,    51,     0,    52,    53,    54,    55,    56,    88,     0,
    57,   121,     0,     0,     0,     0,    40,    41,     0,    42,
    43,    44,    45,     0,    46,    47,     0,    48,    49,    50,
    51,     0,    52,    53,    54,    55,    56,    39,     0,    57,
    89,     0,     0,     0,     0,    40,    41,     0,    42,    43,
    44,    45,     0,    46,    47,     0,    48,    49,    50,    51,
    0,    52,    53,    54,    55,    56,   110,     0,    57,   109,
    0,     0,     0,     0,    40,    41,     0,    42,    43,    44,
    45,     0,    46,    47,     0,    48,    49,    50,    51,     0,
    52,    53,    54,    55,    56,    39,     0,    57,   111,     0,
    0,     0,     0,    40,    41,     0,    42,    43,    44,    45,
    0,    46,    47,     0,    48,    49,    50,    51,     0,    52,
    53,    54,    55,    56,    39,     0,    57,   112,     0,     0,
    0,     0,    40,    41,     0,    42,    43,    44,    45,     0,
    46,    47,     0,    48,    49,    50,    51,     0,    52,    53,
    54,    55,    56,    39,     0,    57,   122,     0,     0,     0,
    0,    40,    41,     0,    42,    43,    44,    45,     0,    46,
    47,     0,    48,    49,    50,    51,     0,    52,    53,    54,
    55,    56,    39,     0,    57,   123,     0,     0,     0,     0,
    40,    41,     0,    42,    43,    44,    45,     0,    46,    47,
    0,    48,    49,    50,    51,     0,    52,    53,    54,    55,
    56,     0,     0,    57,   125,    39,     0,    -5,     0,     0,
    0,     0,     0,    40,    41,     0,    42,    43,    44,    45,
    0,    46,    47,     0,    48,    49,    50,    51,     0,    52,
    53,    54,    55,    56,    39,     0,    57,     0,     0,     0,
    0,     0,    40,    41,     0,    42,    43,    44,    45,     0,
    46,    47,     0,    48,    49,    50,    51,   119,    52,    53,
    54,    55,    56,    39,     0,    57,     0,     0,     0,     0,
    0,    40,    41,     0,    42,    43,    44,    45,     0,    46,
    47,     0,    48,    49,    50,    51,     0,    52,    53,    54,
    55,    56,     0,     5,    57,    -6,     0,     6,     7,     8,
    9,     0,    10,    11,     0,     0,     0,     0,    12,     0,
    0,     0,     0,     0,     0,    13,     0,    14,     5,     0,
    15,     0,     6,     7,     8,     9,     0,    10,    11,     0,
    0,     0,     0,    12,     0,     0,     0,     0,     0,     0,
    13,    97,    14,     5,     0,    15,     0,     6,     7,     8,
    9,     0,    10,    11,     0,     0,     0,     0,    12,     0,
    0,     0,     0,     0,     0,    13,     0,    14,     0,     0,
    15
};

  /* YYCHECK.  */
  private static final byte
  yycheck_[] =
{
    3,     9,     0,    39,    11,    24,     5,    26,    57,    28,
    13,     3,    25,    83,    33,    70,    86,     3,    17,    32,
    1,    76,    58,    32,    41,    76,    29,    44,    79,    25,
    32,    32,    68,    34,    42,    32,    43,   107,    87,    34,
    48,    35,    50,    34,    99,    94,    51,    66,    51,    30,
    31,    40,    88,    -1,    90,    -1,    -1,    47,    -1,    -1,
    -1,    -1,    81,    -1,    -1,    -1,    85,    -1,   117,    -1,
    -1,    79,   108,    72,   110,    -1,    -1,   113,    -1,    -1,
    83,    -1,    -1,    86,    -1,    -1,    -1,    -1,   124,     1,
    -1,     3,    -1,   101,    -1,    -1,    -1,     9,    10,    -1,
    12,    13,    14,    15,   107,    17,    18,    -1,    20,    21,
    22,    23,    -1,    25,    26,    27,    28,    29,     1,    -1,
    32,    33,    -1,    -1,    -1,    -1,     9,    10,    -1,    12,
    13,    14,    15,    -1,    17,    18,    -1,    20,    21,    22,
    23,    -1,    25,    26,    27,    28,    29,     1,    -1,    32,
    33,    -1,    -1,    -1,    -1,     9,    10,    -1,    12,    13,
    14,    15,    -1,    17,    18,    -1,    20,    21,    22,    23,
    -1,    25,    26,    27,    28,    29,     1,    -1,    32,    33,
    -1,    -1,    -1,    -1,     9,    10,    -1,    12,    13,    14,
    15,    -1,    17,    18,    -1,    20,    21,    22,    23,    -1,
    25,    26,    27,    28,    29,     1,    -1,    32,    33,    -1,
    -1,    -1,    -1,     9,    10,    -1,    12,    13,    14,    15,
    -1,    17,    18,    -1,    20,    21,    22,    23,    -1,    25,
    26,    27,    28,    29,     1,    -1,    32,    33,    -1,    -1,
    -1,    -1,     9,    10,    -1,    12,    13,    14,    15,    -1,
    17,    18,    -1,    20,    21,    22,    23,    -1,    25,    26,
    27,    28,    29,     1,    -1,    32,    33,    -1,    -1,    -1,
    -1,     9,    10,    -1,    12,    13,    14,    15,    -1,    17,
    18,    -1,    20,    21,    22,    23,    -1,    25,    26,    27,
    28,    29,     1,    -1,    32,    33,    -1,    -1,    -1,    -1,
    9,    10,    -1,    12,    13,    14,    15,    -1,    17,    18,
    -1,    20,    21,    22,    23,    -1,    25,    26,    27,    28,
    29,    -1,    -1,    32,    33,     1,    -1,     3,    -1,    -1,
    -1,    -1,    -1,     9,    10,    -1,    12,    13,    14,    15,
    -1,    17,    18,    -1,    20,    21,    22,    23,    -1,    25,
    26,    27,    28,    29,     1,    -1,    32,    -1,    -1,    -1,
    -1,    -1,     9,    10,    -1,    12,    13,    14,    15,    -1,
    17,    18,    -1,    20,    21,    22,    23,    24,    25,    26,
    27,    28,    29,     1,    -1,    32,    -1,    -1,    -1,    -1,
    -1,     9,    10,    -1,    12,    13,    14,    15,    -1,    17,
    18,    -1,    20,    21,    22,    23,    -1,    25,    26,    27,
    28,    29,    -1,     1,    32,     3,    -1,     5,     6,     7,
    8,    -1,    10,    11,    -1,    -1,    -1,    -1,    16,    -1,
    -1,    -1,    -1,    -1,    -1,    23,    -1,    25,     1,    -1,
    28,    -1,     5,     6,     7,     8,    -1,    10,    11,    -1,
    -1,    -1,    -1,    16,    -1,    -1,    -1,    -1,    -1,    -1,
    23,    24,    25,     1,    -1,    28,    -1,     5,     6,     7,
    8,    -1,    10,    11,    -1,    -1,    -1,    -1,    16,    -1,
    -1,    -1,    -1,    -1,    -1,    23,    -1,    25,    -1,    -1,
    28
};

  /* STOS_[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
  private static final byte
  yystos_[] =
{
    0,     1,    30,    31,    37,     1,     5,     6,     7,     8,
    10,    11,    16,    23,    25,    28,    39,    40,    41,    38,
    55,     0,    41,    45,    55,    49,    56,    51,    59,    45,
    58,    51,    48,    63,    54,    55,     3,    41,     3,     1,
    9,    10,    12,    13,    14,    15,    17,    18,    20,    21,
    22,    23,    25,    26,    27,    28,    29,    32,    42,    43,
    44,    64,    64,    64,    64,    46,    55,    32,    34,    50,
    65,    64,    40,    25,    43,    49,    58,    45,    51,    58,
    47,    62,    48,    45,    52,    60,    45,    54,     1,    33,
    42,    43,    25,    64,    57,    43,    50,    24,    50,    65,
    45,    65,    64,    46,    32,    53,    64,    46,    42,    33,
    1,    33,    33,    42,    35,    50,    45,    61,    46,    24,
    3,    33,    33,    33,    42,    33
};

  /* TOKEN_NUMBER_[YYLEX-NUM] -- Internal symbol number corresponding
     to YYLEX-NUM.  */
  private static final short
  yytoken_number_[] =
{
    0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
    265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
    275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
    285,   286,   123,   125,    91,    93
};

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
  private static final byte
  yyr1_[] =
{
    0,    36,    37,    37,    37,    38,    39,    40,    40,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    42,    42,    43,    43,    43,    43,    43,    43,    43,
    43,    44,    44,    44,    44,    44,    44,    44,    44,    44,
    44,    44,    44,    44,    44,    44,    45,    46,    46,    47,
    48,    49,    50,    50,    51,    52,    53,    53,    54,    55,
    56,    57,    58,    59,    60,    61,    62,    63,    64,    64,
    64,    64,    64,    65
};

  /* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
  private static final byte
  yyr2_[] =
{
    0,     2,     3,     3,     1,     2,     1,     1,     2,     2,
    2,     2,     2,     2,     3,     4,     3,     4,     1,     1,
    2,     1,     2,     1,     1,     1,     1,     1,     1,     1,
    2,     2,     3,     4,     2,     2,     3,     4,     2,     3,
    4,     2,     2,     3,     1,     4,     2,     2,     2,     2,
    2,     2,     4,     3,     2,     2,     4,     3,     2,     0,
    0,     0,     0,     0,     0,     0,     0,     0,     3,     2,
    4,     3,     4,     3
};

  /* yytname_[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
     First, the terminals, then, starting at \a yyntokens_, nonterminals.  */
  private static final String yytname__[] =
    {
    "$end", "error", "$undefined", "END_OF_INPUT", "ERROR", "SECTIONHEADER",
    "RSECTIONHEADER", "VSECTIONHEADER", "SECTIONHEADER2", "RCODEMACRO",
    "SEXPR", "RDOPTS", "LATEXMACRO", "VERBMACRO", "OPTMACRO", "ESCAPE",
    "LISTSECTION", "ITEMIZE", "DESCRIPTION", "NOITEM", "LATEXMACRO2",
    "VERBMACRO2", "LATEXMACRO3", "IFDEF", "ENDIF", "TEXT", "RCODE", "VERB",
    "COMMENT", "UNKNOWN", "STARTFILE", "STARTFRAGMENT", "'{'", "'}'", "'['",
    "']'", "$accept", "Init", "RdFragment", "RdFile", "SectionList",
    "Section", "ArgItems", "Item", "Markup", "LatexArg", "LatexArg2",
    "Item0Arg", "Item2Arg", "RLikeArg", "RLikeArg2", "VerbatimArg",
    "VerbatimArg1", "VerbatimArg2", "IfDefTarget", "goLatexLike", "goRLike",
    "goRLike2", "goOption", "goVerbatim", "goVerbatim1", "goVerbatim2",
    "goItem0", "goItem2", "Arg", "Option", null
    };

  /* YYRHS -- A `-1'-separated list of the rules' RHS.  */
  private static final byte yyrhs_[] =
    {
    37,     0,    -1,    30,    39,     3,    -1,    31,    38,     3,
    -1,     1,    -1,    55,    42,    -1,    40,    -1,    41,    -1,
    40,    41,    -1,     7,    51,    -1,    11,    51,    -1,     6,
    49,    -1,     5,    45,    -1,    16,    48,    -1,     8,    45,
    46,    -1,    23,    54,    40,    24,    -1,    10,    58,    50,
    -1,    10,    58,    65,    50,    -1,    28,    -1,    25,    -1,
    1,    41,    -1,    43,    -1,    42,    43,    -1,    25,    -1,
    26,    -1,    27,    -1,    28,    -1,    29,    -1,    64,    -1,
    44,    -1,     1,    43,    -1,    12,    45,    -1,    20,    45,
    46,    -1,    22,    45,    46,    46,    -1,    17,    47,    -1,
    18,    48,    -1,    14,    58,    45,    -1,    14,    58,    65,
    45,    -1,     9,    49,    -1,    10,    58,    50,    -1,    10,
    58,    65,    50,    -1,    13,    51,    -1,    21,    52,    -1,
    21,    52,    53,    -1,    15,    -1,    23,    54,    42,    24,
    -1,    55,    64,    -1,    55,    64,    -1,    55,    25,    -1,
    62,    64,    -1,    63,    64,    -1,    56,    64,    -1,    32,
    57,    42,    33,    -1,    32,    57,    33,    -1,    59,    64,
    -1,    60,    64,    -1,    32,    61,    42,    33,    -1,    32,
    61,    33,    -1,    55,    25,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    32,    42,    33,    -1,    32,
    33,    -1,    32,    42,     1,    33,    -1,    32,     1,    33,
    -1,    32,    42,     1,     3,    -1,    34,    43,    35,    -1
    };

  /* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
     YYRHS.  */
  private static final short yyprhs_[] =
    {
    0,     0,     3,     7,    11,    13,    16,    18,    20,    23,
    26,    29,    32,    35,    38,    42,    47,    51,    56,    58,
    60,    63,    65,    68,    70,    72,    74,    76,    78,    80,
    82,    85,    88,    92,    97,   100,   103,   107,   112,   115,
    119,   124,   127,   130,   134,   136,   141,   144,   147,   150,
    153,   156,   159,   164,   168,   171,   174,   179,   183,   186,
    187,   188,   189,   190,   191,   192,   193,   194,   195,   199,
    202,   207,   211,   216
    };

  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final short yyrline_[] =
    {
    0,   181,   181,   182,   183,   186,   189,   192,   193,   195,
    196,   197,   198,   199,   200,   201,   202,   203,   204,   205,
    206,   208,   209,   211,   212,   213,   214,   215,   216,   217,
    218,   220,   221,   222,   223,   224,   225,   226,   227,   228,
    229,   230,   231,   232,   233,   234,   236,   238,   239,   248,
    250,   252,   256,   257,   259,   261,   265,   266,   268,   271,
    273,   275,   277,   279,   281,   283,   285,   287,   289,   290,
    291,   292,   293,   295
    };

  // Report on the debug stream that the rule yyrule is going to be reduced.
  private void yy_reduce_print (int yyrule, YYStack yystack)
  {
    if (yydebug == 0)
      return;

    int yylno = yyrline_[yyrule];
    int yynrhs = yyr2_[yyrule];
    /* Print the symbols being reduced, and their result.  */
    yycdebug ("Reducing stack by rule " + (yyrule - 1)
        + " (line " + yylno + "), ");

    /* The symbols being reduced.  */
    for (int yyi = 0; yyi < yynrhs; yyi++)
      yy_symbol_print ("   $" + (yyi + 1) + " =",
          yyrhs_[yyprhs_[yyrule] + yyi],
          ((yystack.valueAt (yynrhs-(yyi + 1)))), 
          yystack.locationAt (yynrhs-(yyi + 1)));
  }

  /* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
  private static final byte yytranslate_table_[] =
    {
    0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,    34,     2,    35,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,    32,     2,    33,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
    2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
    5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
    15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
    25,    26,    27,    28,    29,    30,    31
    };

  private static final byte yytranslate_ (int t)
  {
    if (t >= 0 && t <= yyuser_token_number_max_)
      return yytranslate_table_[t];
    else
      return yyundef_token_;
  }

  private static final int yylast_ = 490;
  private static final int yynnts_ = 30;
  private static final int yyempty_ = -2;
  private static final int yyfinal_ = 21;
  private static final int yyterror_ = 1;
  private static final int yyerrcode_ = 256;
  private static final int yyntokens_ = 36;

  private static final int yyuser_token_number_max_ = 286;
  private static final int yyundef_token_ = 2;

  /* User implementation code.  */

  private static final boolean DEBUGVALS = false;   /* 1 causes detailed internal state output to R console */  
  private static final boolean DEBUGMODE = false;   /* 1 causes Bison output of parse state, to stdout or stderr */

  private boolean wCalls = true;

  private int YYERROR_VERBOSE = 0;
  //
  //static void yyerror(const char *);
  //static int yylex();
  //static int yyparse(void);



  private void YYLLOC_DEFAULT(Location Current, Object Rhs, int N) {      
    do {                 
      if (N != 0)             
      {               
        (Current).begin.line   = YYRHSLOC (Rhs, 1).begin.line;  
        (Current).begin.column = YYRHSLOC (Rhs, 1).begin.column;  
        (Current).begin.byteIndex   = YYRHSLOC (Rhs, 1).begin.byteIndex;  
        (Current).end.line    = YYRHSLOC (Rhs, N).end.line;   
        (Current).end.column  = YYRHSLOC (Rhs, N).end.column; 
        (Current).end.byteIndex    = YYRHSLOC (Rhs, N).end.byteIndex;   
      } else {               
        (Current).begin.line   = (Current).end.line   =    
            YYRHSLOC (Rhs, 0).end.line;        
        (Current).begin.column = (Current).end.column =    
            YYRHSLOC (Rhs, 0).end.column;        
        (Current).begin.byteIndex   = (Current).end.byteIndex =    
            YYRHSLOC (Rhs, 0).end.byteIndex;        
      }               
    } while (false);

  }

  /* Useful defines so editors don't get confused ... */

  private Location YYRHSLOC(Object rhs, int i) {
    // TODO Auto-generated method stub
    return null;
  }

  private static final char LBRACE =  '{';
  private static final char RBRACE  = '}';

  /* Functions used in the parsing process */
  //
  //  static SEXP GrowList(SEXP, SEXP);
  //static int  KeywordLookup(const char *);
  //static SEXP NewList(void);
  //static SEXP     makeSrcref(Location *, SEXP);

  /* Flags used to mark presence of IFDEF or Sexpr in the dynamicFlag attribute */

  private static final int STATIC = 0;
  private static final int HAS_IFDEF = 1;
  private static final int HAS_SEXPR = 2;

  private static final int RLIKE = 1;   /* Includes R strings; xxinRString holds the opening quote char, or 0 outside a string */
  private static final int LATEXLIKE = 2;
  private static final int VERBATIM = 3;
  private static final int INOPTION = 4;
  private static final int COMMENTMODE = 5;   /* only used in deparsing */
  private static final int UNKNOWNMODE = 6;   /* ditto */

  private static final int PARSE_CONTEXT_SIZE = 32;

  private SEXP     SrcFile;  /* parse_Rd will *always* supply a srcfile */


  /* Internal lexer / parser state variables */

  private int xxinRString;
  private int  xxQuoteLine, xxQuoteCol;
  private boolean  xxinEqn;
  private boolean  xxNewlineInString;
  private int  xxlineno, xxbyteno, xxcolno;
  private int  xxmode, xxitemType, xxbraceDepth;  /* context for lexer */
  private boolean  xxDebugTokens = false;  /* non-zero causes debug output to R console */
  private String xxBasename;     /* basename of file for error messages */
  private SEXP Value;
  private int  xxinitvalue;
  private String yyunknown = "unknown macro"; /* our message, not bison's */


  /* Line 879 of lalr1.java  */
  /* Line 297 of "gramRd.y"  */


  private SEXP xxpushMode(int newmode, int newitem, boolean neweqn) {
    IntArrayVector.Builder ans = new IntArrayVector.Builder();

    ans.add(xxmode);		/* Lexer mode */
    ans.add(xxitemType);	/* What is \item? */
    ans.add(xxbraceDepth);	/* Brace depth used in RCODE and VERBATIM */
    ans.add(xxinRString);      /* Quote char that started a string */
    ans.add(xxQuoteLine);      /* Where the quote was */
    ans.add(xxQuoteCol);       /*           "         */
    ans.add(xxinEqn ? 1 : 0);          /* In the first arg to \eqn or \deqn:  no escapes */

    if(DEBUGMODE) {
      Rprintf("xxpushMode(%d, %s) pushes %d, %s, %d\n", newmode, yytname__[yytranslate_(newitem)], 
          xxmode, yytname__[yytranslate_(xxitemType)], xxbraceDepth);
    }
    xxmode = newmode;
    xxitemType = newitem;
    xxbraceDepth = 0;
    xxinRString = 0;
    xxinEqn = neweqn;

    return ans.build();
  }

  void xxpopMode(SEXP oldmodeExp) {

    IntVector oldmode = (IntVector) oldmodeExp;

    //    if(DEBUGVALS) {
    //      Rprintf("xxpopMode(%d, %s, %d) replaces %d, %s, %d\n", 
    //          INTEGER(oldmode)[0], yytname_[YYTRANSLATE(INTEGER(oldmode)[1])], INTEGER(oldmode)[2], 
    //          xxmode, yytname_[YYTRANSLATE(xxitemType)], xxbraceDepth);
    //    }

    xxmode = oldmode.getElementAsInt(0);
    xxitemType = oldmode.getElementAsInt(1); 
    xxbraceDepth = oldmode.getElementAsInt(2);
    xxinRString = oldmode.getElementAsInt(3);
    xxQuoteLine = oldmode.getElementAsInt(4);
    xxQuoteCol  = oldmode.getElementAsInt(5);
    xxinEqn	= oldmode.getElementAsInt(6) != 0;


  }

  private int getDynamicFlag(SEXP item) {
    SEXP flag = getAttrib(item, install("dynamicFlag"));
    if (isNull(flag)) {
      return 0;
    } else  {
      return ((IntVector)flag).getElementAsInt(0);
    }
  }

  private void setDynamicFlag(ListVector.Builder item, int flag) {
    if (flag != 0) {
      CDefines.setAttrib(item, Symbol.get("dynamicFlag"), new IntArrayVector(flag));
    }
  }

  private SEXP setDynamicFlag(SEXP item, int flag) {
    if (flag != 0) {
      return item.setAttribute(Symbol.get("dynamicFlag"), new IntArrayVector(flag));
    } else {
      return item;
    }
  }

  private SEXP xxnewlist(SEXP item) {
    SEXP ans, tmp;
    if(DEBUGVALS) {
      Rprintf("xxnewlist(item=%p)", item);
    }    
    PROTECT(tmp = NewList());
    if (item != null) {
      int flag = getDynamicFlag(item);
      PROTECT(ans = GrowList(tmp, item));
      ans = setDynamicFlag(ans, flag);
      UNPROTECT_PTR(tmp);
      UNPROTECT_PTR(item);
    } else {
      ans = tmp;
    }
    if(DEBUGVALS) {
      Rprintf(" result: %p is length %d\n", ans, length(ans));
    }
    return ans;
  }

  private SEXP xxlist(SEXP oldlist, SEXP item) {
    SEXP ans;
    int flag = getDynamicFlag(oldlist) | getDynamicFlag(item);
    if(DEBUGVALS) {
      Rprintf("xxlist(oldlist=%p, item=%p)", oldlist, item);
    }
    PROTECT(ans = GrowList(oldlist, item));
    UNPROTECT_PTR(item);
    UNPROTECT_PTR(oldlist);
    setDynamicFlag(ans, flag);
    if(DEBUGVALS) {
      Rprintf(" result: %p is length %d\n", ans, length(ans));
    }
    return ans;
  }

  private SEXP xxmarkup(SEXP header, SEXP body, int flag, Location lloc) {
    SEXP ans;
    if(DEBUGVALS) {
      Rprintf("xxmarkup(header=%p, body=%p)", header, body);    
    }
    if (isNull(body)) {
      ans = new ListVector();
    } else {
      flag |= getDynamicFlag(body);
      PROTECT(ans = PairToVectorList(CDR(body)));
      UNPROTECT_PTR(body);	
    }
    if (isNull(header)) {
      PROTECT(header = new StringArrayVector("LIST"));
    }

    ans = ans.setAttribute(install("Rd_tag"), header);
    ans = ans.setAttribute(Symbols.SRC_REF, makeSrcref(lloc, SrcFile));
    ans = setDynamicFlag(ans, flag);
    if(DEBUGVALS) {
      Rprintf(" result: %p\n", ans);    
    }
    return ans;
  }

  private SEXP xxOptionmarkup(SEXP header, SEXP option, SEXP body, int flag, Location lloc) {
    SEXP ans;
    if(DEBUGVALS) {
      Rprintf("xxOptionmarkup(header=%p, option=%p, body=%p)", header, option, body);    
    }
    flag |= getDynamicFlag(body);
    PROTECT(ans = PairToVectorList(CDR(body)));
    UNPROTECT_PTR(body);	
    ans = setAttrib(ans, install("Rd_tag"), header);
    UNPROTECT_PTR(header);
    flag |= getDynamicFlag(option);
    ans = setAttrib(ans, install("Rd_option"), option);
    UNPROTECT_PTR(option);
    ans = setAttrib(ans, Symbols.SRC_REF, makeSrcref(lloc, SrcFile));
    ans = setDynamicFlag(ans, flag);    
    if(DEBUGVALS) {
      Rprintf(" result: %p\n", ans);    
    }
    return ans;
  }

  private SEXP setAttrib(SEXP ans, Symbol install, SEXP header) {
    return ans.setAttribute(install, header);
  }

  private SEXP xxmarkup2(SEXP header, SEXP body1, SEXP body2, int argcount, int flag, Location lloc) {
    if(DEBUGVALS) {
      Rprintf("xxmarkup2(header=%p, body1=%p, body2=%p)", header, body1, body2);        
    }

    ListVector.Builder ans = new ListVector.Builder();
    if (!isNull(body1)) {
      int flag1 = getDynamicFlag(body1);
      ans.set(0, setDynamicFlag(PairToVectorList(CDR(body1)), flag1));
      flag |= flag1;
    }
    if (!isNull(body2)) {
      if (argcount < 2) error("internal error: inconsistent argument count");
      int flag2 = getDynamicFlag(body2);
      ans.set(1, setDynamicFlag(PairToVectorList(CDR(body2)), flag2));
      flag |= flag2;
    }
    ans.setAttribute(install("Rd_tag"), header);
    ans.setAttribute(Symbols.SRC_REF, makeSrcref(lloc, SrcFile));
    setDynamicFlag(ans, flag);
    if(DEBUGVALS) {
      Rprintf(" result: %p\n", ans.build());    
    }
    return ans.build();
  }

  private SEXP xxmarkup3(SEXP header, SEXP body1, SEXP body2, SEXP body3, int flag, Location lloc) {
    if(DEBUGVALS) {
      Rprintf("xxmarkup2(header=%p, body1=%p, body2=%p, body3=%p)", header, body1, body2, body3);        
    }

    ListVector.Builder ans = new ListVector.Builder(3);

    if (!isNull(body1)) {
      int flag1 = getDynamicFlag(body1);
      ans.set(0, setDynamicFlag(PairToVectorList(CDR(body1)), flag1));
    
      flag |= flag1;
    }
    if (!isNull(body2)) {
      int flag2;
      flag2 = getDynamicFlag(body2);
      ans.set(1, setDynamicFlag(PairToVectorList(CDR(body2)), flag2));    
      flag |= flag2;
    }
    if (!isNull(body3)) {
      int flag3;
      flag3 = getDynamicFlag(body3);
      ans.set(2, setDynamicFlag(PairToVectorList(CDR(body3)), flag3));    
      flag |= flag3;
    }    
    ans.setAttribute(install("Rd_tag"), header);
    ans.setAttribute(Symbols.SRC_REF, makeSrcref(lloc, SrcFile));
    setDynamicFlag(ans, flag);
    if(DEBUGVALS) {
      Rprintf(" result: %p\n", ans.build());    
    }
    return ans.build();
  }

  private SEXP PairToVectorList(SEXP exp) {
    PairList pairList = (PairList) exp;
    return pairList.toVector(); 
  }

  private void xxsavevalue(SEXP Rd, Location lloc) {
    int flag = getDynamicFlag(Rd);
    if(CDR(Rd).length() == 0) {
      Value = Null.INSTANCE;
    } else {
      ListVector.NamedBuilder valueBuilder = new ListVector.NamedBuilder();
      PairList pairList = (PairList)CDR(Rd);
      for(PairList.Node node : pairList.nodes()) {
        if(node.hasTag()) {
          valueBuilder.add(node.getTag().getPrintName(), node.getValue());
        } else {
          valueBuilder.add(node.getValue());
        }
      }
      valueBuilder.setAttribute(Symbols.CLASS, new StringArrayVector("Rd"));
      valueBuilder.setAttribute(Symbols.SRC_REF, makeSrcref(lloc, SrcFile));
      setDynamicFlag(valueBuilder, flag);
      Value = valueBuilder.build();
    }
  }

  private SEXP xxtag(SEXP item, int type, Location lloc) {
    item = setAttrib(item, install("Rd_tag"), new StringArrayVector(yytname__[yytranslate_(type)]));
    item = setAttrib(item, Symbols.SRC_REF, makeSrcref(lloc, SrcFile));
    return item;
  }

  private void xxWarnNewline() {
    if (xxNewlineInString) {
      //if(wCalls)
        System.out.println(String.format(_("newline within quoted string at %s:%d"), 
            xxBasename, xxNewlineInString));
//      else
//        System.out.println(R_NilValue,
//            _("newline within quoted string at %s:%d"), 
//            xxBasename, xxNewlineInString);
    }
  }


  /*----------------------------------------------------------------------------*/



  /* Private pushback, since file ungetc only guarantees one byte.
   We need up to one MBCS-worth and one failed #ifdef or one numeric
   garbage markup match */

  private int PUSHBACK_BUFSIZE = 30;

  private int pushback[] = new int[PUSHBACK_BUFSIZE];
  private int npush = 0;

  private int prevpos = 0;
  private int prevlines[] = new int[PUSHBACK_BUFSIZE];
  private int prevcols[] = new int[PUSHBACK_BUFSIZE];
  private int prevbytes[] = new int[PUSHBACK_BUFSIZE];

  private int R_ParseContextLine;

  private int xxgetc() {
    int c;

    if(npush!=0) {
      c = pushback[--npush];
    }  else {
      c = con_getc();
    }

    prevpos = (prevpos + 1) % PUSHBACK_BUFSIZE;
    prevcols[prevpos] = xxcolno;
    prevbytes[prevpos] = xxbyteno;
    prevlines[prevpos] = xxlineno;    

    if (c == EOF) return R_EOF;

    R_ParseContextLast = (R_ParseContextLast + 1) % PARSE_CONTEXT_SIZE;
    R_ParseContext[R_ParseContextLast] = c;

    if (c == '\n') {
      xxlineno += 1;
      xxcolno = 1;
      xxbyteno = 1;
    } else {
      xxcolno++;
      xxbyteno++;
    }
//    /* only advance column for 1st byte in UTF-8 */
//    if (0x80 <= (unsigned char)c && (unsigned char)c <= 0xBF)
//      xxcolno--;

    if (c == '\t') {
      xxcolno = ((xxcolno + 6) & ~7) + 1;
    }

    R_ParseContextLine = xxlineno;

    return c;
  }

  private int xxungetc(int c) {
    /* this assumes that c was the result of xxgetc; if not, some edits will be needed */
    xxlineno = prevlines[prevpos];
    xxbyteno = prevbytes[prevpos];
    xxcolno  = prevcols[prevpos];
    prevpos = (prevpos + PUSHBACK_BUFSIZE - 1) % PUSHBACK_BUFSIZE;

    R_ParseContextLine = xxlineno;

    R_ParseContext[R_ParseContextLast] = '\0';
    /* Mac OS X requires us to keep this non-negative */
    R_ParseContextLast = (R_ParseContextLast + PARSE_CONTEXT_SIZE - 1) 
        % PARSE_CONTEXT_SIZE;
    if(npush >= PUSHBACK_BUFSIZE - 2) return EOF;
    pushback[npush++] = c;
    return c;
  }

  private SEXP makeSrcref(Location lloc, SEXP srcfile) {
    if(lloc.begin != null && lloc.end != null) {
      IntArrayVector.Builder val = new IntArrayVector.Builder();
      val.add(lloc.begin.line);
      val.add(lloc.begin.byteIndex);
      val.add(lloc.end.line);
      val.add(lloc.end.byteIndex);
      val.add(lloc.begin.column);
      val.add(lloc.end.column);
      val.setAttribute(Symbols.SRC_FILE, srcfile);
      val.setAttribute(Symbols.CLASS, new StringArrayVector("srcref"));
      return val.build();
    } else {
      return Null.INSTANCE;
    }
  }


  /* Stretchy List Structures : Lists are created and grown using a special */
  /* dotted pair.  The CAR of the list points to the last cons-cell in the */
  /* list and the CDR points to the first.  The list can be extracted from */
  /* the pair by taking its CDR, while the CAR gives fast access to the end */
  /* of the list. */


  /* Create a stretchy-list dotted pair */

  private SEXP NewList() {
    SEXP s = CONS(R_NilValue, R_NilValue);
    SETCAR(s, s);
    return s;
  }

  /* Add a new element at the end of a stretchy list */

  private SEXP GrowList(SEXP l, SEXP s) {
    SEXP tmp;
    PROTECT(s);
    tmp = CONS(s, R_NilValue);
    UNPROTECT(1);
    SETCDR(CAR(l), tmp);
    SETCAR(l, tmp);
    return l;
  }

  /*--------------------------------------------------------------------------*/

  /*
   *  Parsing Entry Points:
   *
   *  The Following entry points provide Rd parsing facilities.
   *
   *	SEXP R_ParseRd(Rconnection con, ParseStatus *status, SEXP srcfile)
   *
   */

  public enum ParseStatus {
    PARSE_OK,
    PARSE_ERR
  }

  private ParseStatus status;

  private int R_ParseContextLast;

  private int[] R_ParseContext = new int[PARSE_CONTEXT_SIZE];


  private SEXP ParseRd(SEXP srcfile, boolean fragment) throws IOException {
    R_ParseContextLast = 0;
    R_ParseContext[0] = '\0';

    xxlineno = 1;
    xxcolno = 1; 
    xxbyteno = 1;

    SrcFile = srcfile;

    npush = 0;
    xxmode = LATEXLIKE; 
    xxitemType = UNKNOWN;
    xxbraceDepth = 0;
    xxinRString = 0;
    xxNewlineInString = false;
    xxinEqn = false;
    if (fragment) xxinitvalue = STARTFRAGMENT;
    else	  xxinitvalue = STARTFILE;

    Value = R_NilValue;

    if (parse()) {
      status = ParseStatus.PARSE_ERR;
    } else {
      status = ParseStatus.PARSE_OK;
    }

    if(DEBUGVALS) {
      Rprintf("ParseRd result: %p\n", Value);    
    }    
    UNPROTECT_PTR(Value);
    return Value;
  }

  private Reader con_parse;

  /* need to handle incomplete last line */
  private int con_getc() {
    int c;
    int last=-1000;

    try {
      c = con_parse.read();
      if (c == EOF && last != '\n') c = '\n';
      return (last = c);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
   
  }


  public SEXP R_ParseRd(Reader con, SEXP srcfile, boolean fragment) throws IOException {
    con_parse = con;
    return ParseRd(srcfile, fragment);
  }

  /*----------------------------------------------------------------------------
   *
   *  The Lexical Analyzer:
   *
   *  Basic lexical analysis is performed by the following
   *  routines.  
   *
   *  The function yylex() scans the input, breaking it into
   *  tokens which are then passed to the parser.  
   *
   */


  /* Special Symbols */
  /* Section and R code headers */



  private static class Keyword {
    public String name;
    public int token;

    public Keyword(String name, int token) {
      this.name = name;
      this.token = token;
    }
  }

  private Keyword keywords[] = new Keyword[] {
      /* These sections contain Latex-like text */

      new Keyword( "\\author",  SECTIONHEADER ),
      new Keyword( "\\concept", SECTIONHEADER ),
      new Keyword( "\\description",SECTIONHEADER ),
      new Keyword( "\\details", SECTIONHEADER ),
      new Keyword( "\\docType", SECTIONHEADER ),

      new Keyword( "\\encoding",SECTIONHEADER ),
      new Keyword( "\\format",  SECTIONHEADER ),
      new Keyword( "\\keyword", SECTIONHEADER ),
      new Keyword( "\\note",    SECTIONHEADER ),    
      new Keyword( "\\references", SECTIONHEADER ),

      new Keyword( "\\section", SECTIONHEADER2 ),    
      new Keyword( "\\seealso", SECTIONHEADER ),
      new Keyword( "\\source",  SECTIONHEADER ),
      new Keyword( "\\title",   SECTIONHEADER ),

      /* These sections contain R-like text */

      new Keyword( "\\examples",RSECTIONHEADER ),
      new Keyword( "\\usage",   RSECTIONHEADER ),

      /* These sections contain verbatim text */

      new Keyword( "\\alias",   VSECTIONHEADER ), 
      new Keyword( "\\name",    VSECTIONHEADER ),
      new Keyword( "\\synopsis",VSECTIONHEADER ), 
      new Keyword( "\\Rdversion",VSECTIONHEADER ),

      /* These macros take no arguments.  One character non-alpha escapes get the
       same token value */

      new Keyword( "\\cr",      ESCAPE ),
      new Keyword( "\\dots",    ESCAPE ),
      new Keyword( "\\ldots",   ESCAPE ),
      new Keyword( "\\R",       ESCAPE ),    
      new Keyword( "\\tab",     ESCAPE ),

      /* These macros take one LaTeX-like argument. */

      new Keyword( "\\acronym", LATEXMACRO ),
      new Keyword( "\\bold",    LATEXMACRO ),
      new Keyword( "\\cite",    LATEXMACRO ),
      new Keyword( "\\command", LATEXMACRO ),
      new Keyword( "\\dfn",     LATEXMACRO ),
      new Keyword( "\\dQuote",  LATEXMACRO ),
      new Keyword( "\\email",   LATEXMACRO ),

      new Keyword( "\\emph",    LATEXMACRO ),    
      new Keyword( "\\file",    LATEXMACRO ),
      new Keyword( "\\linkS4class", LATEXMACRO ),
      new Keyword( "\\pkg",	   LATEXMACRO ),
      new Keyword( "\\sQuote",  LATEXMACRO ),

      new Keyword( "\\strong",  LATEXMACRO ),

      new Keyword( "\\var",     LATEXMACRO ),

      /* These are like SECTIONHEADER/LATEXMACRO, but they change the interpretation of \item */

      new Keyword( "\\arguments",LISTSECTION ),
      new Keyword( "\\value",   LISTSECTION ),

      new Keyword( "\\describe",DESCRIPTION ),
      new Keyword( "\\enumerate",ITEMIZE ),
      new Keyword( "\\itemize", ITEMIZE ),

      new Keyword( "\\item",    NOITEM ), /* will change to UNKNOWN, ESCAPE, or LATEXMACRO2 depending on context */

      /* These macros take two LaTeX-like arguments. */

      new Keyword( "\\enc",     LATEXMACRO2 ),
      new Keyword( "\\if",      LATEXMACRO2 ),
      new Keyword( "\\method",  LATEXMACRO2 ),
      new Keyword( "\\S3method",LATEXMACRO2 ),
      new Keyword( "\\S4method",LATEXMACRO2 ),
      new Keyword( "\\tabular", LATEXMACRO2 ),

      /* This macro takes three LaTeX-like arguments. */

      new Keyword( "\\ifelse",  LATEXMACRO3 ),

      /* These macros take one optional bracketed option and always take 
       one LaTeX-like argument */

      new Keyword( "\\link",    OPTMACRO ),

      /* These markup macros require an R-like text argument */

      new Keyword( "\\code",    RCODEMACRO ),
      new Keyword( "\\dontshow",RCODEMACRO ),
      new Keyword( "\\donttest",RCODEMACRO ),
      new Keyword( "\\testonly",RCODEMACRO ),

      /* This macro take one optional bracketed option and one R-like argument */

      new Keyword( "\\Sexpr",   SEXPR ),

      /* This is just like a VSECTIONHEADER, but it needs SEXPR processing */

      new Keyword( "\\RdOpts",   RDOPTS ),

      /* These macros take one verbatim arg and ignore everything except braces */

      new Keyword( "\\dontrun", VERBMACRO ), /* at least for now */    
      new Keyword( "\\env",     VERBMACRO ),
      new Keyword( "\\kbd", 	   VERBMACRO ),	
      new Keyword( "\\option",  VERBMACRO ),
      new Keyword( "\\out",     VERBMACRO ),
      new Keyword( "\\preformatted", VERBMACRO ),

      new Keyword( "\\samp",    VERBMACRO ),
      new Keyword( "\\special", VERBMACRO ),
      new Keyword( "\\url",     VERBMACRO ),
      new Keyword( "\\verb",    VERBMACRO ),

      /* These ones take one or two verbatim args */

      new Keyword( "\\eqn",     VERBMACRO2 ),
      new Keyword( "\\deqn",    VERBMACRO2 ),

      /* We parse IFDEF/IFNDEF as markup, not as a separate preprocessor step */

      new Keyword( "#ifdef",    IFDEF ),
      new Keyword( "#ifndef",   IFDEF ),
      new Keyword( "}",    ENDIF )
      /* All other markup macros are rejected. */
  };

  /* Record the longest # directive here */
  private static final int DIRECTIVE_LEN = 7;   

  private int KeywordLookup(String s) {
    int i;
    for (i = 0; i!=keywords.length; i++) {
      if (keywords[i].name.equals(s)) {
        return keywords[i].token;
      }
    }
    return UNKNOWN;
  }
  
  String yytname__translations[] =
    {
      /* the left column are strings coming from bison, the right
           column are translations for users.
           The first YYENGLISH from the right column are English to be translated,
           the rest are to be copied literally.  The #if 0 block below allows xgettext
           to see these.
       */    
      "$undefined", "input",  
      "SECTIONHEADER","section header",
      "RSECTIONHEADER","section header",
      "VSECTIONHEADER","section header",
      "LISTSECTION",  "section header",

      "LATEXMACRO", "macro",
      "LATEXMACRO2",  "macro",
      "LATEXMACRO3",  "macro",
      "RCODEMACRO", "macro",
      "VERBMACRO",    "macro",
      "VERBMACRO2", "macro",

      "ESCAPE", "macro",
      "ITEMIZE",  "macro",
      "IFDEF",  "conditional",
      "SECTIONHEADER2","section header",
      "OPTMACRO", "macro",

      "DESCRIPTION",  "macro",
      "VERB",   "VERBATIM TEXT"
    };

  private void setfirstloc() {
    yylloc.begin.line = xxlineno;
    yylloc.begin.column = xxcolno;
    yylloc.begin.byteIndex = xxbyteno;
  }

  private void setlastloc() {
    yylloc.end.line = prevlines[prevpos];
    yylloc.end.column = prevcols[prevpos];
    yylloc.end.byteIndex = prevbytes[prevpos];
  }
  public static final int INITBUFSIZE = 128;

  private int mkText(int c) {
    StringBuilder text = new StringBuilder();
    int lookahead;

    read:while(true) {
      switch (c) {
      case '\\': 
        lookahead = xxgetc();
        if (lookahead == LBRACE || lookahead == RBRACE ||
            lookahead == '%' || lookahead == '\\') {
          c = lookahead;
          break;
        }
        xxungetc(lookahead);
        if (Character.isLetter(lookahead)) {
          break read;
        }
      case ']':
        if (xxmode == INOPTION) {
          break read;
        }
        break;
      case '%':
      case LBRACE:
      case RBRACE:
      case R_EOF:
        break read;
      }
      text.appendCodePoint(c);
      if (c == '\n') {
        break read;
      }
      c = xxgetc();
    };
    
    if (c != '\n') xxungetc(c); /* newline causes a break, but we keep it */
    yylval = new StringArrayVector(correctCrLf(text.toString()));
    return TEXT;
  }

  private String correctCrLf(String s) {
    // i think this must be handled  by R at the character stream level...
    return s.replace("\r\n", "\n");
  }

  private int mkComment(int c) {
    StringBuilder text = new StringBuilder();
    
    do {
      text.appendCodePoint(c);
    } while ((c = xxgetc()) != '\n' && c != R_EOF);

    xxungetc(c);
    
    yylval = new StringArrayVector(removeTrailingCR(text.toString()));
    return COMMENT;
  }

  private String removeTrailingCR(String string) {
    if(string.endsWith("\r")) {
      return string.substring(0, string.length()-1);
    } else {
      return string;
    }
  }

  private int mkCode(int c) {
    StringBuilder text = new StringBuilder();
    
    /* Avoid double counting initial braces */
    if (c == LBRACE && xxinRString==0) {
      xxbraceDepth--;
    }
    if (c == RBRACE && xxinRString==0) {
      xxbraceDepth++; 
    }

    while(true) {
      boolean escaped = false;
      if (c == '\\') {
        int lookahead = xxgetc();
        if (lookahead == '\\' || lookahead == '%') {
          c = lookahead;
          escaped = true;
        } else xxungetc(lookahead);
      }
      if ((!escaped && c == '%') || c == R_EOF) break;
      if (xxinRString!=0) {
        /* This stuff is messy, because there are two levels of escaping:
    	       The Rd escaping and the R code string escaping. */
        if (c == '\\') {
          int lookahead = xxgetc();
          if (lookahead == '\\') { /* This must be the 3rd backslash */
            lookahead = xxgetc();
            if (lookahead == xxinRString || lookahead == '\\') {	
              text.appendCodePoint(c);
              c = lookahead;
              escaped = true;
            } else {
              xxungetc(lookahead); /* put back the 4th char */
              xxungetc('\\');	     /* and the 3rd */
            }
          } else if (lookahead == xxinRString) { /* There could be one or two before this */
            text.appendCodePoint(c);
            c = lookahead;
            escaped = true;
          } else if (!escaped && (lookahead == 'l' || lookahead == 'v')) { 
            /* assume \link or \var; this breaks vertical tab, but does anyone ever use that? */
            xxungetc(lookahead);
            break;
          } else xxungetc(lookahead);
        }
        if (!escaped && c == xxinRString)
          xxinRString = 0;
      } else {
        if (c == '#') {
          do {
            escaped = false;
            text.appendCodePoint(c);
            c = xxgetc();
            if (c == '\\') {
              int lookahead = xxgetc();
              if (lookahead == '\\' || lookahead == '%' || lookahead == LBRACE || lookahead == RBRACE) {
                c = lookahead;
                escaped = true;
              } else xxungetc(lookahead);
            }
            if (c == LBRACE && !escaped) xxbraceDepth++;
            else if (c == RBRACE && !escaped) xxbraceDepth--;
          } while (c != '\n' && c != R_EOF && xxbraceDepth > 0);
          if (c == RBRACE && !escaped) xxbraceDepth++; /* avoid double counting */
        }
        if (c == '\'' || c == '"' || c == '`') {
          xxinRString = c;
          xxQuoteLine = xxlineno;
          xxQuoteCol  = xxcolno;
        } else if (c == '\\' && !escaped) {
          int lookahead = xxgetc();
          if (lookahead == LBRACE || lookahead == RBRACE) {
            c = lookahead;
          } else if (Character.isLetter(lookahead)) {
            xxungetc(lookahead);
            c = '\\';
            break;
          } else {
            text.append('\\');
            c = lookahead;
          }
        } else if (c == LBRACE) {
          xxbraceDepth++;
        } else if (c == RBRACE) {
          if (xxbraceDepth == 1) break;
          else xxbraceDepth--;
        } else if (c == R_EOF) break;
      }
      text.appendCodePoint(c);
      if (c == '\n') {
        if (xxinRString != 0 && !xxNewlineInString) { 
          xxNewlineInString = (xxlineno-1) != 0;
        }
        break;
      }
      c = xxgetc();
    }
    if (c != '\n') {
      xxungetc(c);
    }
    yylval = new StringArrayVector(correctCrLf(text.toString()));
    return RCODE; 
  }

  private int mkMarkup(int c) {
    StringBuilder text = new StringBuilder();
    int retval = 0, attempt = 0;

    text.appendCodePoint(c);
    while (isalnum((c = xxgetc()))) {
      text.appendCodePoint(c);
    }
    
    while (attempt++ < 2) {
      /* character escapes are processed as text, not markup */
      if (text.length() == 1) {
        text.appendCodePoint(c);
        retval = TEXT;
        c = xxgetc();
        break;
      } else {
        retval = KeywordLookup(text.toString());
        if (retval == UNKNOWN && attempt == 1) { /* try again, non-digits only */
          
          while (isdigit(text.codePointAt(text.length()-1))) {
            xxungetc(c);
            c = text.codePointAt(text.length()-1); /* pop the last letter into c */
          }
        } else {
          if (retval == NOITEM) 
            retval = xxitemType;
          break;
        }
      }
    }
    PROTECT(yylval = new StringArrayVector(text.toString()));
    xxungetc(c);
    return retval;
  }

  private boolean isdigit(int cp) {
    return Character.isDigit(cp);
  }

  private boolean isalnum(int cp) {
    return Character.isLetter(cp) || Character.isDigit(cp);
  }

  private int mkIfdef(int c) {
    StringBuilder text = new StringBuilder();
    int retval;

    text.appendCodePoint(c);
    while (Character.isDigit((c = xxgetc())) && text.length() <= DIRECTIVE_LEN) {
      text.appendCodePoint(c);
    }
    xxungetc(c);
    retval = KeywordLookup(text.toString());
    yylval = new StringArrayVector(text.toString());

    switch (retval) {
    case ENDIF:  /* eat chars to the end of the line */
      do { 
        c = xxgetc(); 
      } while (c != '\n' && c != R_EOF);
      break;
    case UNKNOWN:
      while(text.length() > 1) {
        xxungetc(text.codePointAt(text.length()-1));
        text.setLength(text.length()-1);
      }
      switch (xxmode) {
      case RLIKE:     
        retval = mkCode(text.codePointAt(0));
        break;
      case INOPTION:
      case LATEXLIKE:
        retval = mkText(text.codePointAt(0));
        break;
      case VERBATIM:
        retval = mkVerb(text.codePointAt(0));
        break;
      }
      break;
    }
    return retval;
  }

  private int mkVerb(int c) {
    StringBuilder text = new StringBuilder();

    /* Avoid double counting initial braces */
    if (c == LBRACE) {
      xxbraceDepth--;
    }
    if (c == RBRACE) {
      xxbraceDepth++;     
    }

    while(true) {
      int escaped = 0;
      if (c == '\\') {
        int lookahead = xxgetc();
        if (lookahead == '\\' || lookahead == '%' || lookahead == LBRACE || lookahead == RBRACE) {
          escaped = 1;
          if (xxinEqn) {
            text.appendCodePoint(c);
          }
          c = lookahead;
        } else {
          xxungetc(lookahead);
        }
      }
      if (c == R_EOF) {
        break;
      }
      if (escaped==0) {
        if (c == '%' && !xxinEqn) {
          break;
        } else if (c == LBRACE) {
          xxbraceDepth++;
        } else if (c == RBRACE) {
          if (xxbraceDepth == 1) {
            break;
          } else {
            xxbraceDepth--;
          }
        }
      }
      text.appendCodePoint(c);
      if (c == '\n') {
        break;
      }
      c = xxgetc();
    };
    if (c != '\n') {
      xxungetc(c);
    }
    yylval = new StringArrayVector(text.toString());
    return VERB;  
  }

  private Location yylloc = new Location(new Position());
  private SEXP yylval;
  
  private static final int R_EOF = -1;

  
  private class RdLexer implements Lexer {



    @Override
    public int yylex() {
      int tok = token();

      if (xxDebugTokens) {
        Rprintf("%d:%d: %s", yylloc.begin.line, yylloc.begin.column, yytname__[yytranslate_(tok)]);
        if (xxinRString != 0) Rprintf("(in %c%c)", xxinRString, xxinRString);
        if (tok > 255 && tok != END_OF_INPUT) 
          Rprintf(": %s", CHAR(STRING_ELT(yylval, 0)));
        Rprintf("\n");
      }
      setlastloc();
      return tok;
    }

    /* Split the input stream into tokens. */
    /* This is the lowest of the parsing levels. */

    private int token()
    {
      int c, lookahead;
      boolean outsideLiteral = xxmode == LATEXLIKE || xxmode == INOPTION || xxbraceDepth == 0;

      if (xxinitvalue != 0) {
        yylloc.begin.line = 0;
        yylloc.begin.column = 0;
        yylloc.begin.byteIndex = 0;
        yylloc.end.line = 0;
        yylloc.end.column = 0;
        yylloc.end.byteIndex = 0;
        PROTECT(yylval = new StringArrayVector(""));
        c = xxinitvalue;
        xxinitvalue = 0;
        return(c);
      }

      setfirstloc();    
      c = xxgetc();

      switch (c) {
      case '%': if (!xxinEqn) return mkComment(c);
      break;
      case '\\':
        if (!xxinEqn) {
          lookahead = xxungetc(xxgetc());
          if (Character.isLetter(lookahead) && xxmode != VERBATIM 
              /* In R strings, only link or var is allowed as markup */
              && (lookahead == 'l' || lookahead == 'v' || xxinRString==0)) 
            return mkMarkup(c);
        }
        break;
      case R_EOF:
        if (xxinRString!=0) {
          xxWarnNewline();
          error(_("Unexpected end of input (in %c quoted string opened at %s:%d:%d)"), 
              xxinRString, xxBasename, xxQuoteLine, xxQuoteCol);
        }
        return END_OF_INPUT; 
      case '#':
        if (!xxinEqn && yylloc.begin.column == 1) return mkIfdef(c);
        break;
      case LBRACE:
        if (xxinRString==0) {
          xxbraceDepth++;
          if (outsideLiteral) return c;
        }
        break;
      case RBRACE:
        if (xxinRString==0) {
          xxbraceDepth--;
          if (outsideLiteral || xxbraceDepth == 0) return c;
        }
        break;
      case '[':
      case ']':
        if (xxmode == INOPTION ) return c; 
        break;
      }       

      switch (xxmode) {
      case RLIKE:     return mkCode(c);
      case INOPTION:
      case LATEXLIKE: return mkText(c);
      case VERBATIM:  return mkVerb(c);
      }

      return ERROR; /* We shouldn't get here. */
    }


    private void yyerror(String s)  {
      throw new RuntimeException(s);

      //      String yyunexpected = "syntax error, unexpected ";
      //      String yyexpecting = ", expecting ";
      //      String yyshortunexpected = "unexpected %s";
      //      String yylongunexpected = "unexpected %s '%s'";
      //      String expecting;
      //      String ParseErrorMsg;
      //      SEXP filename;
      //      String ParseErrorFilename;
      //
      //      xxWarnNewline();  /* post newline warning if necessary */
      //
      //      /*
      //      R_ParseError     = yylloc.begin.line;
      //      R_ParseErrorCol  = yylloc.begin.column;
      //      R_ParseErrorFile = SrcFile;
      //       */
      //
      //      if (s.equals(yyunexpected)) {
      //        int i, translated = FALSE;
      //        /* Edit the error message */    
      ////        expecting = strstr(s + sizeof yyunexpected -1, yyexpecting);
      ////        if (expecting) *expecting = '\0';
      ////        for (i = 0; yytname__translations[i]; i += 2) {
      ////          if (!strcmp(s + sizeof yyunexpected - 1, yytname__translations[i])) {
      ////            sprintf(ParseErrorMsg, yychar < 256 ? _(yyshortunexpected): _(yylongunexpected), 
      ////                i/2 < YYENGLISH ? _(yytname__translations[i+1])
      ////                    : yytname__translations[i+1], CHAR(STRING_ELT(yylval, 0)));
      ////            translated = TRUE;
      ////            break;
      ////          }
      //        }
      //        if (!translated)
      //          sprintf(ParseErrorMsg, yychar < 256 ? _(yyshortunexpected) : _(yylongunexpected),
      //              s + sizeof yyunexpected - 1, CHAR(STRING_ELT(yylval, 0)));
      //        if (expecting) {
      //          translated = FALSE;
      //          for (i = 0; yytname__translations[i]; i += 2) {
      //            if (!strcmp(expecting + sizeof yyexpecting - 1, yytname__translations[i])) {
      //              strcat(ParseErrorMsg, _(yyexpecting));
      //              strcat(ParseErrorMsg, i/2 < YYENGLISH ? _(yytname__translations[i+1])
      //                  : yytname__translations[i+1]);
      //              translated = TRUE;
      //              break;
      //            }
      //          }
      //          if (!translated) {
      //            strcat(ParseErrorMsg, _(yyexpecting));
      //            strcat(ParseErrorMsg, expecting + sizeof yyexpecting - 1);
      //          }
      //        }
      //      } else if (!strncmp(s, yyunknown, sizeof yyunknown-1)) {
      //        sprintf(ParseErrorMsg, "%s '%s'", s, CHAR(STRING_ELT(yylval, 0)));
      //      } else {
      //        sprintf(ParseErrorMsg, "%s", s);
      //      }
      //      filename = findVar(install("filename"), SrcFile);
      //      if (!isNull(filename))
      //        strncpy(ParseErrorFilename, CHAR(STRING_ELT(filename, 0)), PARSE_ERROR_SIZE - 1);
      //      else
      //        ParseErrorFilename[0] = '\0';
      //      if (wCalls) {
      //        if (yylloc.begin.line != yylloc.end.line)
      //          warning("%s:%d-%d: %s", 
      //              ParseErrorFilename, yylloc.begin.line, yylloc.end.line, ParseErrorMsg);
      //        else
      //          warning("%s:%d: %s", 
      //              ParseErrorFilename, yylloc.begin.line, ParseErrorMsg);
      //      } else {
      //        if (yylloc.begin.line != yylloc.end.line)
      //          warningcall(R_NilValue, "%s:%d-%d: %s", 
      //              ParseErrorFilename, yylloc.begin.line, yylloc.end.line, ParseErrorMsg);
      //        else
      //          warningcall(R_NilValue, "%s:%d: %s", 
      //              ParseErrorFilename, yylloc.begin.line, ParseErrorMsg);
      //      }
    }

    @Override
    public Position getStartPos() {
      return yylloc.begin;
    }

    @Override
    public Position getEndPos() {
      return yylloc.end;
    }

    @Override
    public Object getLVal() {
      return yylval;
    }

    @Override
    public void yyerror(Location loc, String s) {
      // TODO Auto-generated method stub
      throw new RuntimeException(s);
    }

  }

  private void Rprintf(String message, Object... arguments) {
    // quick hack to support %p :
    for(int i = 0 ;i!=arguments.length;++i) {
      if(arguments[i] instanceof SEXP) {
        arguments[i] = System.identityHashCode(arguments[i]);
      }
    }
    message = message.replace("%p", "0x%x");
    System.out.println( String.format(message, arguments) );
  }
}

