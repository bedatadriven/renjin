/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.parser;

import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.repackaged.guava.io.CharStreams;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.renjin.util.CDefines.*;


/* A Bison parser, made by GNU Bison 2.4.2.  */

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
/* Line 1 of "gram.y"  */


/**
 * A Bison parser, automatically generated from <tt>gram.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
public class RParser {

  public static ExpressionVector parseSource(Reader reader, SEXP srcFile) throws IOException {
    ParseState parseState = new ParseState();
    parseState.srcFile = srcFile;
    parseState.setKeepSrcRefs(srcFile instanceof Environment);
    ParseOptions parseOptions = ParseOptions.defaults();
    RLexer lexer = new RLexer(parseOptions, parseState, reader);
    RParser parser = new RParser(parseOptions, parseState, lexer);
    return parser.parseAll();
  }

  public static ExpressionVector parseWithSrcref(String source) throws IOException {
    if(!source.endsWith("\n")) {
      source = source + "\n";
    }
    Reader reader = new StringReader(source);
    ParseState parseState = new ParseState();
    ParseOptions parseOptions = ParseOptions.defaults();
    parseState.setKeepSrcRefs(true);
    RLexer lexer = new RLexer(parseOptions, parseState, reader);
    RParser parser = new RParser(parseOptions, parseState, lexer);
    return parser.parseAll();
  }

  /**
   * Parses the source and adds a terminator of the stream if it does not exist.
   */
  public static ExpressionVector parseAllSource(Reader reader, SEXP srcFile) throws IOException {
    String source = CharStreams.toString(reader);
    if(!source.endsWith("\n")) {
      source = source + "\n";
    }
    return parseSource(source, srcFile);
  }
  
  public static ExpressionVector parseAllSource(Reader reader) throws IOException {
    return parseAllSource(reader, Null.INSTANCE);
  }


  public static ExpressionVector parseSource(Reader reader) throws IOException {
    return parseAllSource(reader);
  }


  public static ExpressionVector parseSource(CharSource source, SEXP srcFile) throws IOException {
    try(Reader reader = source.openStream()) {
      return parseAllSource(reader, srcFile);
    }
  }
  
  public static ExpressionVector parseSource(String source) {
    try {
      return parseAllSource(new StringReader(source));
    } catch (IOException e) {
      throw new RuntimeException(e); // shouldn't happen when reading from a string
    }
  }

  public static ExpressionVector parseSource(String source, String sourceFile) {
    return parseSource(source, sourceFile(sourceFile));
  }

  public static ExpressionVector parseSource(CharSource reader, String sourceFile) throws IOException {
    return parseSource(reader, sourceFile(sourceFile));
  }

  private static Environment sourceFile(String sourceFile) {
    Environment environment = new DynamicEnvironment(Environment.EMPTY);
    environment.setParent(Environment.EMPTY);
    environment.setVariableUnsafe("filename", StringVector.valueOf(sourceFile));
    environment.setAttribute("class", new StringArrayVector("srcfilecopy", "srcfile"));
    return environment;
  }

  public static ExpressionVector parseSource(String source, SEXP srcFile) {
    try {
      return parseSource(new StringReader(source), srcFile);
    } catch (IOException e) {
      throw new RuntimeException(e); // shouldn't happen when reading from a string.
    }
  }

  private ExpressionVector parseAll() throws IOException {
    List<SEXP> exprList = Lists.newArrayList();

    while (true) {
      
      // check to see if we are at the end of the file
      if(yylexer.isEof()) {
        return attachSrcrefs(new ExpressionVector(exprList), state.srcFile);
      }

      if (!parse()) {
        if (yylexer.errorEncountered()) {
          throw new ParseException("Syntax error at "
              + yylexer.getErrorLocation() + ": " + yylexer.getErrorMessage()
              + "\n");
        }
      }

      StatusResult status = getResultStatus();
      switch (status) {
      case EMPTY:
        break;
      case INCOMPLETE:
      case OK:
        exprList.add(getResult());
        break;
      case ERROR:
        throw new ParseException(getResultStatus().toString());
      case EOF:
        return attachSrcrefs(new ExpressionVector(exprList), state.srcFile);
      }
    }
  }

  public enum StatusResult {
    EMPTY,
    OK,
    INCOMPLETE,
    ERROR,
    EOF
  }

  private ParseState state;
  private ParseOptions options;
  

  /**
   * Version number for the Bison executable that generated this parser.
   */
  public static final String bisonVersion = "2.4.2";

  /**
   * Name of the skeleton that generated this parser.
   */
  public static final String bisonSkeleton = "lalr1.java";


  /**
   * True if verbose error messages are enabled.
   */
  public boolean errorVerbose = true;
  private StatusResult extendedParseResult;

  private SEXP result;
 
  /**
   * list of srcRefs (analog of SrcRefs in original R code (src/main/gram.y)
   * each element contains SrcRef SEXP. When parsing list of expressions, 
   * src-refs of list element collected here, than attachSrcRef attach those 
   * objects.
   */
  private SEXP srcRefs = NewList();

  /**
   * Unused, here is too keep 'REPROTECT(..)' calls the same, as in original R code (in C).
   */
  private int srindex = 0; 

  /**
   * A class defining a pair of positions.  Positions, defined by the
   * <code>Position</code> class, denote a point in the input.
   * Locations represent a part of the input through the beginning
   * and ending positions.
   */
  public class Location {

    private Position begin;

    private Position end;

    /**
     * Create a <code>Location</code> denoting an empty range located at
     * a given point.
     *
     * @param loc The position at which the range is anchored.
     */
    public Location(Position loc) {
      this.begin = loc; this.end = loc;
    }

    /**
     * Create a <code>Location</code> from the endpoints of the range.
     *
     * @param begin The first position included in the range.
     * @param end   The first position beyond the range.
     */
    public Location(Position begin, Position end) {
      this.begin = begin;
      this.end = end;
    }


    /**
     * Print a representation of the location.  For this to be correct,
     * <code>Position</code> should override the <code>equals</code>
     * method.
     */
    public String toString() {
      if (getBegin() == null && getEnd() == null) {
        return toString(getBegin());
      } else {
        return "" +
            (getBegin().getLine() + 1)      + " " +
            (getBegin().getCharIndex() + 1) + " " +
            (getEnd().getLine() + 1)        + " " +
            (getEnd().getCharIndex() + 1)   + " " +
            (getBegin().getColumn() + 1)    + " " +
            (getEnd().getColumn() + 1);
      }
    }

    private String toString(Position p) {
      return p == null ? "NULL" : p.toString();
    }

    /**
     * The first, inclusive, position in the range.
     */
    public Position getBegin() {
      return begin;
    }

    /**
     * The first position beyond the range.
     */
    public Position getEnd() {
      return end;
    }

  }


  /**
   * Token returned by the scanner to signal the end of its input.
   */
  public static final int EOF = 0;

/* Tokens.  */
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int END_OF_INPUT = 258;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int ERROR = 259;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int STR_CONST = 260;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NUM_CONST = 261;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NULL_CONST = 262;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int SYMBOL = 263;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int FUNCTION = 264;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int LEFT_ASSIGN = 265;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int EQ_ASSIGN = 266;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int RIGHT_ASSIGN = 267;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int LBB = 268;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int FOR = 269;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int IN = 270;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int IF = 271;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int ELSE = 272;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int WHILE = 273;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NEXT = 274;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int BREAK = 275;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int REPEAT = 276;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int GT = 277;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int GE = 278;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int LT = 279;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int LE = 280;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int EQ = 281;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NE = 282;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int AND = 283;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int OR = 284;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int AND2 = 285;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int OR2 = 286;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NS_GET = 287;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NS_GET_INT = 288;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int LOW = 289;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int TILDE = 290;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int NOT = 291;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int UNOT = 292;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int SPECIAL = 293;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int UPLUS = 294;
  /**
   * Token number, to be returned by the scanner.
   */
  public static final int UMINUS = 295;


  private Location yylloc(YYStack rhs, int n) {
    if (n > 0) {
      return new Location(rhs.locationAt(n-1).getBegin(), rhs.locationAt(0).getEnd());
    } else {
      return new Location(rhs.locationAt(0).getEnd());
    }
  }

  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>YYParser</tt>.
   */
  public interface Lexer {
    /**
     * Method to retrieve the beginning position of the last scanned token.
     *
     * @return the position at which the last scanned token starts.
     */
    Position getStartPos();

    /**
     * Method to retrieve the ending position of the last scanned token.
     *
     * @return the first position beyond the last scanned token.
     */
    Position getEndPos();

    /**
     * Method to retrieve the semantic value of the last scanned token.
     *
     * @return the semantic value of the last scanned token.
     */
    Object getLVal();

    /**
     * Entry point for the scanner.  Returns the token identifier corresponding
     * to the next token and prepares to return the semantic value
     * and beginning/ending positions of the token.
     *
     * @return the token identifier corresponding to the next token.
     */
    int yylex() throws java.io.IOException;

    /**
     * Entry point for error reporting.  Emits an error
     * referring to the given location in a user-defined way.
     *
     * @param loc The location of the element to which the
     *            error message is related
     * @param s   The string for the error message.
     */
    void yyerror(Location loc, String s);
  }

  /**
   * The object doing lexical analysis for us.
   */
  private RLexer yylexer;


  /**
   * Instantiates the Bison-generated parser.
   *
   * @param yylexer The scanner that will supply tokens to the parser.
   */
  public RParser(ParseOptions options, ParseState state, RLexer yylexer) {
    this.yylexer = yylexer;
    this.options = options;
    this.state = state;
  }
  
  public RParser(Reader reader) {
    this.state = new ParseState();
    this.options = new ParseOptions();
    this.yylexer = new RLexer(options, state, reader);
  }
  
  

  private java.io.PrintStream yyDebugStream = System.err;

  /**
   * Return the <tt>PrintStream</tt> on which the debugging output is
   * printed.
   */
  public final java.io.PrintStream getDebugStream() {
    return yyDebugStream;
  }

  /**
   * Set the <tt>PrintStream</tt> on which the debug output is printed.
   *
   * @param s The stream that is used for debugging output.
   */
  public final void setDebugStream(java.io.PrintStream s) {
    yyDebugStream = s;
  }

  private int yydebug = 0;

  /**
   * Answer the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   */
  public final int getDebugLevel() {
    return yydebug;
  }

  /**
   * Set the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   *
   * @param level The verbosity level for debugging output.
   */
  public final void setDebugLevel(int level) {
    yydebug = level;
  }

  private final int yylex() throws java.io.IOException {
    return yylexer.yylex();
  }

  protected final void yyerror(Location loc, String s) {
    yylexer.yyerror(loc, s);
  }


  protected final void yyerror(String s) {
    yylexer.yyerror((Location) null, s);
  }

  protected final void yyerror(Position loc, String s) {
    yylexer.yyerror(new Location(loc), s);
  }

  protected final void yycdebug(String s) {
    if (yydebug > 0) {
      yyDebugStream.println(s);
    }
  }

  private final class YYStack {
    private int[] stateStack = new int[16];
    private Location[] locStack = new Location[16];
    private Object[] valueStack = new Object[16];

    public int size = 16;
    public int height = -1;

    public final void push(int state, Object value, Location loc) {
      height++;
      if (size == height) {
        int[] newStateStack = new int[size * 2];
        System.arraycopy(stateStack, 0, newStateStack, 0, height);
        stateStack = newStateStack;

        Location[] newLocStack = new Location[size * 2];
        System.arraycopy(locStack, 0, newLocStack, 0, height);
        locStack = newLocStack;

        Object[] newValueStack = new Object[size * 2];
        System.arraycopy(valueStack, 0, newValueStack, 0, height);
        valueStack = newValueStack;

        size *= 2;
      }

      stateStack[height] = state;
      locStack[height] = loc;
      valueStack[height] = value;
    }

    public final void pop() {
      height--;
    }

    public final void pop(int num) {
      // Avoid memory leaks... garbage collection is a white lie!
      if (num > 0) {
        java.util.Arrays.fill(valueStack, height - num + 1, height, null);
        java.util.Arrays.fill(locStack, height - num + 1, height, null);
      }
      height -= num;
    }

    public final int stateAt(int i) {
      return stateStack[height - i];
    }

    public final Location locationAt(int i) {
      return locStack[height - i];
    }

    public final SEXP valueAt(int i) {
      return (SEXP) valueStack[height - i];
    }

    // Print the state stack on the debug stream.

    public void print(java.io.PrintStream out) {
      out.print("Stack now");

      for (int i = 0; i < height; i++) {
        out.print(' ');
        out.print(stateStack[i]);
        out.print( "("+locStack[i]+")");
      }
      out.println();
    }
  }

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return success (<tt>true</tt>).
   */
  public static final int YYACCEPT = 0;

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return failure (<tt>false</tt>).
   */
  public static final int YYABORT = 1;

  /**
   * Returned by a Bison action in order to start error recovery without
   * printing an error message.
   */
  public static final int YYERROR = 2;

  /**
   * Returned by a Bison action in order to print an error message and start
   * error recovery.  Formally deprecated in Bison 2.4.2's NEWS entry, where
   * a plan to phase it out is discussed.
   */
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
   * operation.
   */
  public final boolean recovering() {
    return yyerrstatus_ == 0;
  }

  private int yyaction(int yyn, YYStack yystack, int yylen) {
    Object yyval;
    Location yyloc = yylloc(yystack, yylen);

    /* If YYLEN is nonzero, implement the default value of the action:
       `$$ = $1'.  Otherwise, use the top of the stack.

       Otherwise, the following line sets YYVAL to garbage.
       This behavior is undocumented and Bison
       users should not rely upon it.  */
    if (yylen > 0) {
      yyval = yystack.valueAt(yylen - 1);
    } else {
      yyval = yystack.valueAt(0);
    }

    yy_reduce_print(yyn, yystack);

    switch (yyn) {
      case 2:
        if (yyn == 2)

/* Line 354 of lalr1.java  */
/* Line 248 of "gram.y"  */ {
          return YYFAIL;
        }
        ;
        break;


      case 3:
        if (yyn == 3)

/* Line 354 of lalr1.java  */
/* Line 249 of "gram.y"  */ {
          return xxvalue(null, StatusResult.EMPTY, null);
        }
        ;
        break;


      case 4:
        if (yyn == 4)

/* Line 354 of lalr1.java  */
/* Line 250 of "gram.y"  */ {
          return xxvalue(((yystack.valueAt(2 - (1)))), StatusResult.INCOMPLETE, yystack.locationAt(2 - (1)));
        }
        ;
        break;


      case 5:
        if (yyn == 5)

/* Line 354 of lalr1.java  */
/* Line 251 of "gram.y"  */ {
          return xxvalue(((yystack.valueAt(2 - (1)))), StatusResult.OK, yystack.locationAt(2 - (1)));
        }
        ;
        break;


      case 6:
        if (yyn == 6)

/* Line 354 of lalr1.java  */
/* Line 252 of "gram.y"  */ {
          return YYABORT;
        }
        ;
        break;


      case 7:
        if (yyn == 7)

/* Line 354 of lalr1.java  */
/* Line 255 of "gram.y"  */ {
          yyval = ((yystack.valueAt(1 - (1))));
        }
        ;
        break;


      case 8:
        if (yyn == 8)

/* Line 354 of lalr1.java  */
/* Line 256 of "gram.y"  */ {
          yyval = ((yystack.valueAt(1 - (1))));
        }
        ;
        break;


      case 9:
        if (yyn == 9)

/* Line 354 of lalr1.java  */
/* Line 259 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 10:
        if (yyn == 10)

/* Line 354 of lalr1.java  */
/* Line 262 of "gram.y"  */ {
          yyval = ((yystack.valueAt(1 - (1))));
        }
        ;
        break;


      case 11:
        if (yyn == 11)

/* Line 354 of lalr1.java  */
/* Line 263 of "gram.y"  */ {
          yyval = ((yystack.valueAt(1 - (1))));
        }
        ;
        break;


      case 12:
        if (yyn == 12)

/* Line 354 of lalr1.java  */
/* Line 264 of "gram.y"  */ {
          yyval = ((yystack.valueAt(1 - (1))));
        }
        ;
        break;


      case 13:
        if (yyn == 13)

/* Line 354 of lalr1.java  */
/* Line 265 of "gram.y"  */ {
          yyval = ((yystack.valueAt(1 - (1))));
        }
        ;
        break;


      case 14:
        if (yyn == 14)

/* Line 354 of lalr1.java  */
/* Line 267 of "gram.y"  */ {
          yyval = xxexprlist(((yystack.valueAt(3 - (1)))), yystack.locationAt(3 - (1)), ((yystack.valueAt(3 - (2)))));
        }
        ;
        break;


      case 15:
        if (yyn == 15)

/* Line 354 of lalr1.java  */
/* Line 268 of "gram.y"  */ {
          yyval = xxparen(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (2)))));
        }
        ;
        break;


      case 16:
        if (yyn == 16)

/* Line 354 of lalr1.java  */
/* Line 270 of "gram.y"  */ {
          yyval = xxunary(((yystack.valueAt(2 - (1)))), ((yystack.valueAt(2 - (2)))));
        }
        ;
        break;


      case 17:
        if (yyn == 17)

/* Line 354 of lalr1.java  */
/* Line 271 of "gram.y"  */ {
          yyval = xxunary(((yystack.valueAt(2 - (1)))), ((yystack.valueAt(2 - (2)))));
        }
        ;
        break;


      case 18:
        if (yyn == 18)

/* Line 354 of lalr1.java  */
/* Line 272 of "gram.y"  */ {
          yyval = xxunary(((yystack.valueAt(2 - (1)))), ((yystack.valueAt(2 - (2)))));
        }
        ;
        break;


      case 19:
        if (yyn == 19)

/* Line 354 of lalr1.java  */
/* Line 273 of "gram.y"  */ {
          yyval = xxunary(((yystack.valueAt(2 - (1)))), ((yystack.valueAt(2 - (2)))));
        }
        ;
        break;


      case 20:
        if (yyn == 20)

/* Line 354 of lalr1.java  */
/* Line 274 of "gram.y"  */ {
          yyval = xxunary(((yystack.valueAt(2 - (1)))), ((yystack.valueAt(2 - (2)))));
        }
        ;
        break;


      case 21:
        if (yyn == 21)

/* Line 354 of lalr1.java  */
/* Line 276 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 22:
        if (yyn == 22)

/* Line 354 of lalr1.java  */
/* Line 277 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 23:
        if (yyn == 23)

/* Line 354 of lalr1.java  */
/* Line 278 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 24:
        if (yyn == 24)

/* Line 354 of lalr1.java  */
/* Line 279 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 25:
        if (yyn == 25)

/* Line 354 of lalr1.java  */
/* Line 280 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 26:
        if (yyn == 26)

/* Line 354 of lalr1.java  */
/* Line 281 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 27:
        if (yyn == 27)

/* Line 354 of lalr1.java  */
/* Line 282 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 28:
        if (yyn == 28)

/* Line 354 of lalr1.java  */
/* Line 283 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 29:
        if (yyn == 29)

/* Line 354 of lalr1.java  */
/* Line 284 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 30:
        if (yyn == 30)

/* Line 354 of lalr1.java  */
/* Line 285 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 31:
        if (yyn == 31)

/* Line 354 of lalr1.java  */
/* Line 286 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(1))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 32:
        if (yyn == 32)

/* Line 354 of lalr1.java  */
/* Line 287 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 33:
        if (yyn == 33)

/* Line 354 of lalr1.java  */
/* Line 288 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 34:
        if (yyn == 34)

/* Line 354 of lalr1.java  */
/* Line 289 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 35:
        if (yyn == 35)

/* Line 354 of lalr1.java  */
/* Line 290 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 36:
        if (yyn == 36)

/* Line 354 of lalr1.java  */
/* Line 291 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 37:
        if (yyn == 37)

/* Line 354 of lalr1.java  */
/* Line 292 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 38:
        if (yyn == 38)

/* Line 354 of lalr1.java  */
/* Line 293 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 39:
        if (yyn == 39)

/* Line 354 of lalr1.java  */
/* Line 294 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 40:
        if (yyn == 40)

/* Line 354 of lalr1.java  */
/* Line 295 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 41:
        if (yyn == 41)

/* Line 354 of lalr1.java  */
/* Line 297 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 42:
        if (yyn == 42)

/* Line 354 of lalr1.java  */
/* Line 298 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (3)))), ((yystack.valueAt(3 - (1)))));
        }
        ;
        break;


      case 43:
        if (yyn == 43)

/* Line 354 of lalr1.java  */
/* Line 300 of "gram.y"  */ {
          yyval = xxdefun(((yystack.valueAt(6 - (1)))), ((yystack.valueAt(6 - (3)))), ((yystack.valueAt(6 - (6)))),yystack.locationAt(0));
        }
        ;
        break;


      case 44:
        if (yyn == 44)

/* Line 354 of lalr1.java  */
/* Line 301 of "gram.y"  */ {
          yyval = xxfuncall(((yystack.valueAt(4 - (1)))), ((yystack.valueAt(4 - (3)))));
        }
        ;
        break;


      case 45:
        if (yyn == 45)

/* Line 354 of lalr1.java  */
/* Line 302 of "gram.y"  */ {
          yyval = xxif(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 46:
        if (yyn == 46)

/* Line 354 of lalr1.java  */
/* Line 303 of "gram.y"  */ {
          yyval = xxifelse(((yystack.valueAt(5 - (1)))), ((yystack.valueAt(5 - (2)))), ((yystack.valueAt(5 - (3)))), ((yystack.valueAt(5 - (5)))));
        }
        ;
        break;


      case 47:
        if (yyn == 47)

/* Line 354 of lalr1.java  */
/* Line 304 of "gram.y"  */ {
          yyval = xxfor(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 48:
        if (yyn == 48)

/* Line 354 of lalr1.java  */
/* Line 305 of "gram.y"  */ {
          yyval = xxwhile(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 49:
        if (yyn == 49)

/* Line 354 of lalr1.java  */
/* Line 306 of "gram.y"  */ {
          yyval = xxrepeat(((yystack.valueAt(2 - (1)))), ((yystack.valueAt(2 - (2)))));
        }
        ;
        break;


      case 50:
        if (yyn == 50)

/* Line 354 of lalr1.java  */
/* Line 307 of "gram.y"  */ {
          yyval = xxsubscript(((yystack.valueAt(5 - (1)))), ((yystack.valueAt(5 - (2)))), ((yystack.valueAt(5 - (3)))));
        }
        ;
        break;


      case 51:
        if (yyn == 51)

/* Line 354 of lalr1.java  */
/* Line 308 of "gram.y"  */ {
          yyval = xxsubscript(((yystack.valueAt(4 - (1)))), ((yystack.valueAt(4 - (2)))), ((yystack.valueAt(4 - (3)))));
        }
        ;
        break;


      case 52:
        if (yyn == 52)

/* Line 354 of lalr1.java  */
/* Line 309 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 53:
        if (yyn == 53)

/* Line 354 of lalr1.java  */
/* Line 310 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 54:
        if (yyn == 54)

/* Line 354 of lalr1.java  */
/* Line 311 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 55:
        if (yyn == 55)

/* Line 354 of lalr1.java  */
/* Line 312 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 56:
        if (yyn == 56)

/* Line 354 of lalr1.java  */
/* Line 313 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 57:
        if (yyn == 57)

/* Line 354 of lalr1.java  */
/* Line 314 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 58:
        if (yyn == 58)

/* Line 354 of lalr1.java  */
/* Line 315 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 59:
        if (yyn == 59)

/* Line 354 of lalr1.java  */
/* Line 316 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 60:
        if (yyn == 60)

/* Line 354 of lalr1.java  */
/* Line 317 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 61:
        if (yyn == 61)

/* Line 354 of lalr1.java  */
/* Line 318 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 62:
        if (yyn == 62)

/* Line 354 of lalr1.java  */
/* Line 319 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 63:
        if (yyn == 63)

/* Line 354 of lalr1.java  */
/* Line 320 of "gram.y"  */ {
          yyval = xxbinary(((yystack.valueAt(3 - (2)))), ((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 64:
        if (yyn == 64)

/* Line 354 of lalr1.java  */
/* Line 321 of "gram.y"  */ {
          yyval = xxnxtbrk(((yystack.valueAt(1 - (1)))));
        }
        ;
        break;


      case 65:
        if (yyn == 65)

/* Line 354 of lalr1.java  */
/* Line 322 of "gram.y"  */ {
          yyval = xxnxtbrk(((yystack.valueAt(1 - (1)))));
        }
        ;
        break;


      case 66:
        if (yyn == 66)

/* Line 354 of lalr1.java  */
/* Line 326 of "gram.y"  */ {
          yyval = xxcond(((yystack.valueAt(3 - (2)))));
        }
        ;
        break;


      case 67:
        if (yyn == 67)

/* Line 354 of lalr1.java  */
/* Line 329 of "gram.y"  */ {
          yyval = xxifcond(((yystack.valueAt(3 - (2)))));
        }
        ;
        break;


      case 68:
        if (yyn == 68)

/* Line 354 of lalr1.java  */
/* Line 332 of "gram.y"  */ {
          yyval = xxforcond(((yystack.valueAt(5 - (2)))), ((yystack.valueAt(5 - (4)))));
        }
        ;
        break;


      case 69:
        if (yyn == 69)

/* Line 354 of lalr1.java  */
/* Line 336 of "gram.y"  */ {
          yyval = xxexprlist0();
        }
        ;
        break;


      case 70:
        if (yyn == 70)

/* Line 354 of lalr1.java  */
/* Line 337 of "gram.y"  */ {
          yyval = xxexprlist1(((yystack.valueAt(1 - (1)))), yystack.locationAt(1 - (1)));
        }
        ;
        break;


      case 71:
        if (yyn == 71)

/* Line 354 of lalr1.java  */
/* Line 338 of "gram.y"  */ {
          yyval = xxexprlist2(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))), yystack.locationAt(3 - (3)));
        }
        ;
        break;


      case 72:
        if (yyn == 72)

/* Line 354 of lalr1.java  */
/* Line 339 of "gram.y"  */ {
          yyval = ((yystack.valueAt(2 - (1))));
        }
        ;
        break;


      case 73:
        if (yyn == 73)

/* Line 354 of lalr1.java  */
/* Line 340 of "gram.y"  */ {
          yyval = xxexprlist2(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))), yystack.locationAt(3 - (3)));
        }
        ;
        break;


      case 74:
        if (yyn == 74)

/* Line 354 of lalr1.java  */
/* Line 341 of "gram.y"  */ {
          yyval = ((yystack.valueAt(2 - (1))));
        }
        ;
        break;


      case 75:
        if (yyn == 75)

/* Line 354 of lalr1.java  */
/* Line 344 of "gram.y"  */ {
          yyval = xxsublist1(((yystack.valueAt(1 - (1)))));
        }
        ;
        break;


      case 76:
        if (yyn == 76)

/* Line 354 of lalr1.java  */
/* Line 345 of "gram.y"  */ {
          yyval = xxsublist2(((yystack.valueAt(4 - (1)))), ((yystack.valueAt(4 - (4)))));
        }
        ;
        break;


      case 77:
        if (yyn == 77)

/* Line 354 of lalr1.java  */
/* Line 348 of "gram.y"  */ {
          yyval = xxsub0();
        }
        ;
        break;


      case 78:
        if (yyn == 78)

/* Line 354 of lalr1.java  */
/* Line 349 of "gram.y"  */ {
          yyval = xxsub1(((yystack.valueAt(1 - (1)))), yystack.locationAt(1 - (1)));
        }
        ;
        break;


      case 79:
        if (yyn == 79)

/* Line 354 of lalr1.java  */
/* Line 350 of "gram.y"  */ {
          yyval = xxsymsub0(((yystack.valueAt(2 - (1)))), yystack.locationAt(2 - (1)));
        }
        ;
        break;


      case 80:
        if (yyn == 80)

/* Line 354 of lalr1.java  */
/* Line 351 of "gram.y"  */ {
          yyval = xxsymsub1(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))), yystack.locationAt(3 - (1)));
        }
        ;
        break;


      case 81:
        if (yyn == 81)

/* Line 354 of lalr1.java  */
/* Line 352 of "gram.y"  */ {
          yyval = xxsymsub0(((yystack.valueAt(2 - (1)))), yystack.locationAt(2 - (1)));
        }
        ;
        break;


      case 82:
        if (yyn == 82)

/* Line 354 of lalr1.java  */
/* Line 353 of "gram.y"  */ {
          yyval = xxsymsub1(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))), yystack.locationAt(3 - (1)));
        }
        ;
        break;


      case 83:
        if (yyn == 83)

/* Line 354 of lalr1.java  */
/* Line 354 of "gram.y"  */ {
          yyval = xxnullsub0(yystack.locationAt(2 - (1)));
        }
        ;
        break;


      case 84:
        if (yyn == 84)

/* Line 354 of lalr1.java  */
/* Line 355 of "gram.y"  */ {
          yyval = xxnullsub1(((yystack.valueAt(3 - (3)))), yystack.locationAt(3 - (1)));
        }
        ;
        break;


      case 85:
        if (yyn == 85)

/* Line 354 of lalr1.java  */
/* Line 358 of "gram.y"  */ {
          yyval = xxnullformal();
        }
        ;
        break;


      case 86:
        if (yyn == 86)

/* Line 354 of lalr1.java  */
/* Line 359 of "gram.y"  */ {
          yyval = xxfirstformal0(((yystack.valueAt(1 - (1)))));
        }
        ;
        break;


      case 87:
        if (yyn == 87)

/* Line 354 of lalr1.java  */
/* Line 360 of "gram.y"  */ {
          yyval = xxfirstformal1(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))));
        }
        ;
        break;


      case 88:
        if (yyn == 88)

/* Line 354 of lalr1.java  */
/* Line 361 of "gram.y"  */ {
          yyval = xxaddformal0(((yystack.valueAt(3 - (1)))), ((yystack.valueAt(3 - (3)))), yystack.locationAt(3 - (3)));
        }
        ;
        break;


      case 89:
        if (yyn == 89)

/* Line 354 of lalr1.java  */
/* Line 362 of "gram.y"  */ {
          yyval = xxaddformal1(((yystack.valueAt(5 - (1)))), ((yystack.valueAt(5 - (3)))), ((yystack.valueAt(5 - (5)))), yystack.locationAt(5 - (3)));
        }
        ;
        break;


      case 90:
        if (yyn == 90)

/* Line 354 of lalr1.java  */
/* Line 365 of "gram.y"  */ {
          state.setEatLines(true);
        }
        ;
        break;


/* Line 354 of lalr1.java  */
/* Line 1440 of "gram.java"  */
      default:
        break;
    }

    yy_symbol_print("-> $$ =", yyr1_[yyn], yyval, yyloc);

    yystack.pop(yylen);
    yylen = 0;

    /* Shift the result of the reduction.  */
    yyn = yyr1_[yyn];
    int yystate = yypgoto_[yyn - yyntokens_] + yystack.stateAt(0);
    if (0 <= yystate && yystate <= yylast_
        && yycheck_[yystate] == yystack.stateAt(0)) {
      yystate = yytable_[yystate];
    } else {
      yystate = yydefgoto_[yyn - yyntokens_];
    }

    yystack.push(yystate, yyval, yyloc);
    return YYNEWSTATE;
  }

  /* Return YYSTR after stripping away unnecessary quotes and
     backslashes, so that it's suitable for yyerror.  The heuristic is
     that double-quoting is unnecessary unless the string contains an
     apostrophe, a comma, or backslash (other than backslash-backslash).
     YYSTR is taken from yytname.  */

  private final String yytnamerr_(String yystr) {
    if (yystr.charAt(0) == '"') {
      StringBuffer yyr = new StringBuffer();
      strip_quotes:
      for (int i = 1; i < yystr.length(); i++) {
        switch (yystr.charAt(i)) {
          case '\'':
          case ',':
            break strip_quotes;

          case '\\':
            if (yystr.charAt(++i) != '\\') {
              break strip_quotes;
            }
            /* Fall through.  */
          default:
            yyr.append(yystr.charAt(i));
            break;

          case '"':
            return yyr.toString();
        }
      }
    } else if (yystr.equals("$end")) {
      return "end of input";
    }

    return yystr;
  }

  /*--------------------------------.
  | Print this symbol on YYOUTPUT.  |
  `--------------------------------*/

  private void yy_symbol_print(String s, int yytype,
                               Object yyvaluep, Object yylocationp) {
    if (yydebug > 0) {
      yycdebug(s + (yytype < yyntokens_ ? " token " : " nterm ")
          + yytname_[yytype] + " ("
          + yylocationp + ": "
          + (yyvaluep == null ? "(null)" : yyvaluep.toString()) + ")");
    }
  }

  /**
   * Parse input from the scanner that was specified at object construction
   * time.  Return whether the end of the input was reached successfully.
   *
   * @return <tt>true</tt> if the parsing succeeds.  Note that this does not
   *         imply that there were no syntax errors.
   */
  public boolean parse() throws java.io.IOException {
    /// Lookahead and lookahead in internal form.
    int yychar = yyempty_;
    int yytoken = 0;

    /* State.  */
    int yyn = 0;
    int yylen = 0;
    int yystate = 0;

    YYStack yystack = new YYStack();

    /* Error handling.  */
    int yynerrs_ = 0;
    /// The location where the error started.
    Location yyerrloc = null;

    /// Location of the lookahead.
    Location yylloc = new Location(new Position());

    /// @$.
    Location yyloc;

    /// Semantic value of the lookahead.
    Object yylval = null;

    int yyresult;

    yycdebug("Starting parse\n");
    yyerrstatus_ = 0;


    /* Initialize the stack.  */
    yystack.push(yystate, yylval, yylloc);

    int label = YYNEWSTATE;
    for (; ;) {
      switch (label) {
        /* New state.  Unlike in the C/C++ skeletons, the state is already
     pushed when we come here.  */
        case YYNEWSTATE:
          yycdebug("Entering state " + yystate + "\n");
          if (yydebug > 0) {
            yystack.print(yyDebugStream);
          }

          /* Accept?  */
          if (yystate == yyfinal_) {
            return true;
          }

          /* Take a decision.  First try without lookahead.  */
          yyn = yypact_[yystate];
          if (yyn == yypact_ninf_) {
            label = YYDEFAULT;
            break;
          }

          /* Read a lookahead token.  */
          if (yychar == yyempty_) {
            yycdebug("Reading a token: ");
            yychar = yylex();

            yylloc = new Location(yylexer.getStartPos(),
                yylexer.getEndPos());
            yylval = yylexer.getLVal();
          }

          /* Convert token to internal form.  */
          if (yychar <= EOF) {
            yychar = yytoken = EOF;
            yycdebug("Now at end of input.\n");
          } else {
            yytoken = yytranslate_(yychar);
            yy_symbol_print("Next token is", yytoken,
                yylval, yylloc);
          }

          /* If the proper action on seeing token YYTOKEN is to reduce or to
   detect an error, take that action.  */
          yyn += yytoken;
          if (yyn < 0 || yylast_ < yyn || yycheck_[yyn] != yytoken) {
            label = YYDEFAULT;
          }/* <= 0 means reduce or error.  */ else if ((yyn = yytable_[yyn]) <= 0) {
            if (yyn == 0 || yyn == yytable_ninf_) {
              label = YYFAIL;
            } else {
              yyn = -yyn;
              label = YYREDUCE;
            }
          } else {
            /* Shift the lookahead token.  */
            yy_symbol_print("Shifting", yytoken,
                yylval, yylloc);

            /* Discard the token being shifted.  */
            yychar = yyempty_;

            /* Count tokens shifted since error; after three, turn off error
               status.  */
            if (yyerrstatus_ > 0) {
              --yyerrstatus_;
            }

            yystate = yyn;
            yystack.push(yystate, yylval, yylloc);
            label = YYNEWSTATE;
          }
          break;

        /*-----------------------------------------------------------.
        | yydefault -- do the default action for the current state.  |
        `-----------------------------------------------------------*/
        case YYDEFAULT:
          yyn = yydefact_[yystate];
          if (yyn == 0) {
            label = YYFAIL;
          } else {
            label = YYREDUCE;
          }
          break;

        /*-----------------------------.
        | yyreduce -- Do a reduction.  |
        `-----------------------------*/
        case YYREDUCE:
          yylen = yyr2_[yyn];
          label = yyaction(yyn, yystack, yylen);
          yystate = yystack.stateAt(0);
          break;

        /*------------------------------------.
        | yyerrlab -- here on detecting error |
        `------------------------------------*/
        case YYFAIL:
          /* If not already recovering from an error, report this error.  */
          if (yyerrstatus_ == 0) {
            ++yynerrs_;
            yyerror(yylloc, yysyntax_error(yystate, yytoken));
          }

          yyerrloc = yylloc;
          if (yyerrstatus_ == 3) {
            /* If just tried and failed to reuse lookahead token after an
             error, discard it.  */

            if (yychar <= EOF) {
              /* Return failure if at end of input.  */
              if (yychar == EOF) {
                return false;
              }
            } else {
              yychar = yyempty_;
            }
          }

          /* Else will try to reuse lookahead token after shifting the error
        token.  */
          label = YYERRLAB1;
          break;

        /*---------------------------------------------------.
        | errorlab -- error raised explicitly by YYERROR.  |
        `---------------------------------------------------*/
        case YYERROR:

          yyerrloc = yystack.locationAt(yylen - 1);
          /* Do not reclaim the symbols of the rule which action triggered
       this YYERROR.  */
          yystack.pop(yylen);
          yylen = 0;
          yystate = yystack.stateAt(0);
          label = YYERRLAB1;
          break;

        /*-------------------------------------------------------------.
        | yyerrlab1 -- common code for both syntax error and YYERROR.  |
        `-------------------------------------------------------------*/
        case YYERRLAB1:
          yyerrstatus_ = 3;  /* Each real token shifted decrements this.  */

          for (; ; ) {
            yyn = yypact_[yystate];
            if (yyn != yypact_ninf_) {
              yyn += yyterror_;
              if (0 <= yyn && yyn <= yylast_ && yycheck_[yyn] == yyterror_) {
                yyn = yytable_[yyn];
                if (0 < yyn) {
                  break;
                }
              }
            }

            /* Pop the current state because it cannot handle the error token.  */
            if (yystack.height == 1) {
              return false;
            }

            yyerrloc = yystack.locationAt(0);
            yystack.pop();
            yystate = yystack.stateAt(0);
            if (yydebug > 0) {
              yystack.print(yyDebugStream);
            }
          }


          /* Muck with the stack to setup for yylloc.  */
          yystack.push(0, null, yylloc);
          yystack.push(0, null, yyerrloc);
          yyloc = yylloc(yystack, 2);
          yystack.pop(2);

          /* Shift the error token.  */
          yy_symbol_print("Shifting", yystos_[yyn],
              yylval, yyloc);

          yystate = yyn;
          yystack.push(yyn, yylval, yyloc);
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
  }

  // Generate an error message.

  private String yysyntax_error(int yystate, int tok) {
    if (errorVerbose) {
      int yyn = yypact_[yystate];
      if (yypact_ninf_ < yyn && yyn <= yylast_) {
        StringBuffer res;

        /* Start YYX at -YYN if negative to avoid negative indexes in
             YYCHECK.  */
        int yyxbegin = yyn < 0 ? -yyn : 0;

        /* Stay within bounds of both yycheck and yytname.  */
        int yychecklim = yylast_ - yyn + 1;
        int yyxend = yychecklim < yyntokens_ ? yychecklim : yyntokens_;
        int count = 0;
        for (int x = yyxbegin; x < yyxend; ++x) {
          if (yycheck_[x + yyn] == x && x != yyterror_) {
            ++count;
          }
        }

        // FIXME: This method of building the message is not compatible
        // with internationalization.
        res = new StringBuffer("syntax error, unexpected ");
        res.append(yytnamerr_(yytname_[tok]));
        if (count < 5) {
          count = 0;
          for (int x = yyxbegin; x < yyxend; ++x) {
            if (yycheck_[x + yyn] == x && x != yyterror_) {
              res.append(count++ == 0 ? ", expecting " : " or ");
              res.append(yytnamerr_(yytname_[x]));
            }
          }
        }
        return res.toString();
      }
    }

    return "syntax error";
  }


  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
  private static final short yypact_ninf_ = -53;
  private static final short yypact_[] =
      {
          83, -53, -53, 37, -53, -53, 61, -35, -33, -29,
          -27, -53, -53, 144, 144, 144, 144, 144, 144, -53,
          144, 144, 8, 56, -53, 235, 7, 9, 15, 24,
          58, 70, 144, 144, 144, 144, 144, -53, 450, 530,
          141, 141, 2, -53, -43, 610, -53, -53, -53, 144,
          144, 144, 198, 144, 144, 144, 144, 144, 144, 144,
          144, 144, 144, 144, 144, 144, 144, 144, 144, 144,
          144, 144, 25, 26, 198, 198, 144, -53, -53, -53,
          -53, -53, -53, -53, -53, 53, -52, 65, -53, 278,
          66, 321, -53, -53, 144, 144, -53, 450, -53, 490,
          -7, 74, -5, 407, 27, -53, 650, 650, 650, 650,
          650, 650, 610, 570, 610, 570, 450, 530, 63, 63,
          23, 23, 122, 141, 141, -53, -53, -53, -53, 39,
          38, 407, 144, -53, 92, 144, -53, 144, -53, -53,
          -53, 144, 144, 144, 60, 55, -53, -53, 407, 144,
          119, 364, -53, 407, 407, 407, -53, 198, -53, 144,
          -53, -53, 407
      };

  /* YYDEFACT[S] -- default rule to reduce with in state S when YYTABLE
     doesn't specify something else to do.  Zero means the default is an
     error.  */
  private static final byte yydefact_[] =
      {
          0, 6, 2, 11, 10, 12, 13, 0, 0, 0,
          0, 64, 65, 0, 0, 0, 0, 0, 0, 3,
          69, 0, 0, 0, 8, 7, 0, 0, 0, 0,
          85, 0, 0, 0, 0, 0, 0, 49, 20, 19,
          17, 16, 0, 70, 0, 18, 1, 4, 5, 0,
          0, 0, 77, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 77, 77, 0, 55, 54, 59,
          58, 53, 52, 57, 56, 86, 0, 0, 47, 0,
          45, 0, 48, 15, 74, 72, 14, 41, 9, 42,
          11, 12, 13, 78, 90, 75, 36, 35, 31, 32,
          33, 34, 37, 38, 39, 40, 30, 29, 22, 23,
          24, 25, 27, 21, 26, 61, 60, 63, 62, 90,
          90, 28, 0, 90, 0, 0, 67, 0, 66, 73,
          71, 81, 83, 79, 0, 0, 44, 51, 87, 0,
          88, 0, 46, 82, 84, 80, 50, 77, 43, 0,
          68, 76, 89
      };

  /* YYPGOTO[NTERM-NUM].  */
  private static final byte yypgoto_[] =
      {
          -53, -53, 45, -53, -14, -53, -53, -53, -53, 51,
          -26, -53, -1
      };

  /* YYDEFGOTO[NTERM-NUM].  */
  private static final short
      yydefgoto_[] =
      {
          -1, 22, 23, 24, 25, 36, 34, 32, 44, 104,
          105, 86, 145
      };

  /* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule which
     number is the opposite.  If zero, do what YYDEFACT says.  */
  private static final short yytable_ninf_ = -1;
  private static final short
      yytable_[] =
      {
          38, 39, 40, 41, 141, 133, 143, 45, 46, 134,
          94, 95, 77, 96, 79, 78, 30, 80, 31, 89,
          81, 91, 33, 82, 35, 26, 27, 28, 29, 83,
          125, 127, 84, 126, 128, 97, 52, 99, 103, 106,
          107, 108, 109, 110, 111, 112, 113, 114, 115, 116,
          117, 118, 119, 120, 121, 122, 123, 124, 37, 93,
          103, 103, 131, 42, 132, 43, 85, 69, 70, 26,
          27, 71, 72, 73, 74, 75, 52, 88, 87, 90,
          135, 92, 76, 137, 1, 142, 2, 144, 3, 4,
          5, 6, 7, 28, 29, 98, 146, 8, 147, 9,
          150, 10, 11, 12, 13, 67, 68, 69, 70, 47,
          48, 71, 72, 73, 74, 75, 157, 14, 148, 15,
          156, 151, 76, 16, 17, 129, 130, 153, 154, 155,
          159, 161, 149, 0, 18, 52, 19, 0, 20, 139,
          140, 21, 0, 103, 0, 162, 0, 0, 0, 3,
          4, 5, 6, 7, 52, 0, 0, 0, 8, 0,
          9, 0, 10, 11, 12, 13, 0, 70, 0, 0,
          71, 72, 73, 74, 75, 0, 0, 0, 14, 0,
          15, 76, 152, 0, 16, 17, 0, 0, 0, 71,
          72, 73, 74, 75, 158, 18, 0, 0, 0, 20,
          76, 0, 21, 100, 4, 101, 102, 7, 0, 0,
          0, 0, 8, 0, 9, 0, 10, 11, 12, 13,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 14, 0, 15, 0, 0, 0, 16, 17,
          0, 0, 0, 0, 0, 49, 50, 51, 52, 18,
          0, 0, 0, 20, 0, 0, 21, 53, 54, 55,
          56, 57, 58, 59, 60, 61, 62, 0, 0, 63,
          0, 64, 0, 0, 0, 65, 66, 67, 68, 69,
          70, 0, 0, 71, 72, 73, 74, 75, 49, 0,
          51, 52, 0, 0, 76, 0, 0, 0, 0, 0,
          53, 54, 55, 56, 57, 58, 59, 60, 61, 62,
          0, 0, 63, 0, 64, 0, 0, 0, 65, 66,
          67, 68, 69, 70, 0, 0, 71, 72, 73, 74,
          75, 49, 0, 51, 52, 136, 0, 76, 0, 0,
          0, 0, 0, 53, 54, 55, 56, 57, 58, 59,
          60, 61, 62, 0, 0, 63, 0, 64, 0, 0,
          0, 65, 66, 67, 68, 69, 70, 0, 0, 71,
          72, 73, 74, 75, 49, 0, 51, 52, 138, 0,
          76, 0, 0, 0, 0, 0, 53, 54, 55, 56,
          57, 58, 59, 60, 61, 62, 0, 0, 63, 0,
          64, 0, 0, 0, 65, 66, 67, 68, 69, 70,
          0, 0, 71, 72, 73, 74, 75, 49, 0, 51,
          52, 160, 0, 76, 0, 0, 0, 0, 0, 53,
          54, 55, 56, 57, 58, 59, 60, 61, 62, 0,
          0, 63, 0, 64, 0, 0, 0, 65, 66, 67,
          68, 69, 70, 0, 0, 71, 72, 73, 74, 75,
          49, 0, 51, 52, 0, 0, 76, 0, 0, 0,
          0, 0, 53, 54, 55, 56, 57, 58, 59, 60,
          61, 62, 0, 0, 0, 0, 64, 0, 0, 0,
          65, 66, 67, 68, 69, 70, 0, 0, 71, 72,
          73, 74, 75, 52, 0, 0, 0, 0, 0, 76,
          0, 0, 53, 54, 55, 56, 57, 58, 59, 60,
          61, 62, 0, 0, 0, 0, 64, 0, 0, 0,
          65, 66, 67, 68, 69, 70, 0, 0, 71, 72,
          73, 74, 75, 52, 0, 0, 0, 0, 0, 76,
          0, 0, 53, 54, 55, 56, 57, 58, 59, 60,
          61, 62, 0, 0, 0, 0, 0, 0, 0, 0,
          65, 66, 67, 68, 69, 70, 0, 0, 71, 72,
          73, 74, 75, 52, 0, 0, 0, 0, 0, 76,
          0, 0, 53, 54, 55, 56, 57, 58, 59, 0,
          61, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          65, 66, 67, 68, 69, 70, 0, 0, 71, 72,
          73, 74, 75, 52, 0, 0, 0, 0, 0, 76,
          0, 0, 53, 54, 55, 56, 57, 58, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          65, 66, 67, 68, 69, 70, 0, 0, 71, 72,
          73, 74, 75, 52, 0, 0, 0, 0, 0, 76,
          0, 0, -1, -1, -1, -1, -1, -1, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          65, 66, 67, 68, 69, 70, 0, 0, 71, 72,
          73, 74, 75, 0, 0, 0, 0, 0, 0, 76
      };

  /* YYCHECK.  */
  private static final short
      yycheck_[] =
      {
          14, 15, 16, 17, 11, 57, 11, 21, 0, 61,
          53, 54, 5, 56, 5, 8, 51, 8, 51, 33,
          5, 35, 51, 8, 51, 32, 33, 32, 33, 5,
          5, 5, 8, 8, 8, 49, 13, 51, 52, 53,
          54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
          64, 65, 66, 67, 68, 69, 70, 71, 13, 57,
          74, 75, 76, 18, 11, 20, 8, 44, 45, 32,
          33, 48, 49, 50, 51, 52, 13, 32, 8, 34,
          15, 36, 59, 17, 1, 11, 3, 60, 5, 6,
          7, 8, 9, 32, 33, 50, 57, 14, 60, 16,
          8, 18, 19, 20, 21, 42, 43, 44, 45, 53,
          54, 48, 49, 50, 51, 52, 61, 34, 132, 36,
          60, 135, 59, 40, 41, 74, 75, 141, 142, 143,
          11, 157, 133, -1, 51, 13, 53, -1, 55, 94,
          95, 58, -1, 157, -1, 159, -1, -1, -1, 5,
          6, 7, 8, 9, 13, -1, -1, -1, 14, -1,
          16, -1, 18, 19, 20, 21, -1, 45, -1, -1,
          48, 49, 50, 51, 52, -1, -1, -1, 34, -1,
          36, 59, 137, -1, 40, 41, -1, -1, -1, 48,
          49, 50, 51, 52, 149, 51, -1, -1, -1, 55,
          59, -1, 58, 5, 6, 7, 8, 9, -1, -1,
          -1, -1, 14, -1, 16, -1, 18, 19, 20, 21,
          -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
          -1, -1, 34, -1, 36, -1, -1, -1, 40, 41,
          -1, -1, -1, -1, -1, 10, 11, 12, 13, 51,
          -1, -1, -1, 55, -1, -1, 58, 22, 23, 24,
          25, 26, 27, 28, 29, 30, 31, -1, -1, 34,
          -1, 36, -1, -1, -1, 40, 41, 42, 43, 44,
          45, -1, -1, 48, 49, 50, 51, 52, 10, -1,
          12, 13, -1, -1, 59, -1, -1, -1, -1, -1,
          22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
          -1, -1, 34, -1, 36, -1, -1, -1, 40, 41,
          42, 43, 44, 45, -1, -1, 48, 49, 50, 51,
          52, 10, -1, 12, 13, 57, -1, 59, -1, -1,
          -1, -1, -1, 22, 23, 24, 25, 26, 27, 28,
          29, 30, 31, -1, -1, 34, -1, 36, -1, -1,
          -1, 40, 41, 42, 43, 44, 45, -1, -1, 48,
          49, 50, 51, 52, 10, -1, 12, 13, 57, -1,
          59, -1, -1, -1, -1, -1, 22, 23, 24, 25,
          26, 27, 28, 29, 30, 31, -1, -1, 34, -1,
          36, -1, -1, -1, 40, 41, 42, 43, 44, 45,
          -1, -1, 48, 49, 50, 51, 52, 10, -1, 12,
          13, 57, -1, 59, -1, -1, -1, -1, -1, 22,
          23, 24, 25, 26, 27, 28, 29, 30, 31, -1,
          -1, 34, -1, 36, -1, -1, -1, 40, 41, 42,
          43, 44, 45, -1, -1, 48, 49, 50, 51, 52,
          10, -1, 12, 13, -1, -1, 59, -1, -1, -1,
          -1, -1, 22, 23, 24, 25, 26, 27, 28, 29,
          30, 31, -1, -1, -1, -1, 36, -1, -1, -1,
          40, 41, 42, 43, 44, 45, -1, -1, 48, 49,
          50, 51, 52, 13, -1, -1, -1, -1, -1, 59,
          -1, -1, 22, 23, 24, 25, 26, 27, 28, 29,
          30, 31, -1, -1, -1, -1, 36, -1, -1, -1,
          40, 41, 42, 43, 44, 45, -1, -1, 48, 49,
          50, 51, 52, 13, -1, -1, -1, -1, -1, 59,
          -1, -1, 22, 23, 24, 25, 26, 27, 28, 29,
          30, 31, -1, -1, -1, -1, -1, -1, -1, -1,
          40, 41, 42, 43, 44, 45, -1, -1, 48, 49,
          50, 51, 52, 13, -1, -1, -1, -1, -1, 59,
          -1, -1, 22, 23, 24, 25, 26, 27, 28, -1,
          30, -1, -1, -1, -1, -1, -1, -1, -1, -1,
          40, 41, 42, 43, 44, 45, -1, -1, 48, 49,
          50, 51, 52, 13, -1, -1, -1, -1, -1, 59,
          -1, -1, 22, 23, 24, 25, 26, 27, -1, -1,
          -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
          40, 41, 42, 43, 44, 45, -1, -1, 48, 49,
          50, 51, 52, 13, -1, -1, -1, -1, -1, 59,
          -1, -1, 22, 23, 24, 25, 26, 27, -1, -1,
          -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
          40, 41, 42, 43, 44, 45, -1, -1, 48, 49,
          50, 51, 52, -1, -1, -1, -1, -1, -1, 59
      };

  /* STOS_[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
  private static final byte
      yystos_[] =
      {
          0, 1, 3, 5, 6, 7, 8, 9, 14, 16,
          18, 19, 20, 21, 34, 36, 40, 41, 51, 53,
          55, 58, 63, 64, 65, 66, 32, 33, 32, 33,
          51, 51, 69, 51, 68, 51, 67, 64, 66, 66,
          66, 66, 64, 64, 70, 66, 0, 53, 54, 10,
          11, 12, 13, 22, 23, 24, 25, 26, 27, 28,
          29, 30, 31, 34, 36, 40, 41, 42, 43, 44,
          45, 48, 49, 50, 51, 52, 59, 5, 8, 5,
          8, 5, 8, 5, 8, 8, 73, 8, 64, 66,
          64, 66, 64, 57, 53, 54, 56, 66, 64, 66,
          5, 7, 8, 66, 71, 72, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 5, 8, 5, 8, 71,
          71, 66, 11, 57, 61, 15, 57, 17, 57, 64,
          64, 11, 11, 11, 60, 74, 57, 60, 66, 74,
          8, 66, 64, 66, 66, 66, 60, 61, 64, 11,
          57, 72, 66
      };

  /* TOKEN_NUMBER_[YYLEX-NUM] -- Internal symbol number corresponding
     to YYLEX-NUM.  */
  private static final short
      yytoken_number_[] =
      {
          0, 256, 257, 258, 259, 260, 261, 262, 263, 264,
          265, 266, 267, 268, 269, 270, 271, 272, 273, 274,
          275, 276, 277, 278, 279, 280, 281, 282, 283, 284,
          285, 286, 287, 288, 63, 289, 126, 290, 291, 292,
          43, 45, 42, 47, 293, 58, 294, 295, 94, 36,
          64, 40, 91, 10, 59, 123, 125, 41, 33, 37,
          93, 44
      };

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
  private static final byte
      yyr1_[] =
      {
          0, 62, 63, 63, 63, 63, 63, 64, 64, 65,
          66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
          66, 66, 66, 66, 66, 66, 67, 68, 69, 70,
          70, 70, 70, 70, 70, 71, 71, 72, 72, 72,
          72, 72, 72, 72, 72, 73, 73, 73, 73, 73,
          74
      };

  /* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
  private static final byte
      yyr2_[] =
      {
          0, 2, 1, 1, 2, 2, 1, 1, 1, 3,
          1, 1, 1, 1, 3, 3, 2, 2, 2, 2,
          2, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
          3, 3, 3, 6, 4, 3, 5, 3, 3, 2,
          5, 4, 3, 3, 3, 3, 3, 3, 3, 3,
          3, 3, 3, 3, 1, 1, 3, 3, 5, 0,
          1, 3, 2, 3, 2, 1, 4, 0, 1, 2,
          3, 2, 3, 2, 3, 0, 1, 3, 3, 5,
          0
      };

  /* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
     First, the terminals, then, starting at \a yyntokens_, nonterminals.  */
  private static final String yytname_[] =
      {
          "$end", "error", "$undefined", "END_OF_INPUT", "ERROR", "STR_CONST",
          "NUM_CONST", "NULL_CONST", "SYMBOL", "FUNCTION", "LEFT_ASSIGN",
          "EQ_ASSIGN", "RIGHT_ASSIGN", "LBB", "FOR", "IN", "IF", "ELSE", "WHILE",
          "NEXT", "BREAK", "REPEAT", "GT", "GE", "LT", "LE", "EQ", "NE", "AND",
          "OR", "AND2", "OR2", "NS_GET", "NS_GET_INT", "'?'", "LOW", "'~'",
          "TILDE", "NOT", "UNOT", "'+'", "'-'", "'*'", "'/'", "SPECIAL", "':'",
          "UPLUS", "UMINUS", "'^'", "'$'", "'@'", "'('", "'['", "'\\n'", "';'",
          "'{'", "'}'", "')'", "'!'", "'%'", "']'", "','", "$accept", "prog",
          "expr_or_assign", "equal_assign", "expr", "cond", "ifcond", "forcond",
          "exprlist", "sublist", "sub", "formlist", "cr", null
      };

  /* YYRHS -- A `-1'-separated list of the rules' RHS.  */
  private static final byte yyrhs_[] =
      {
          63, 0, -1, 3, -1, 53, -1, 64, 53, -1,
          64, 54, -1, 1, -1, 66, -1, 65, -1, 66,
          11, 64, -1, 6, -1, 5, -1, 7, -1, 8,
          -1, 55, 70, 56, -1, 51, 64, 57, -1, 41,
          66, -1, 40, 66, -1, 58, 66, -1, 36, 66,
          -1, 34, 66, -1, 66, 45, 66, -1, 66, 40,
          66, -1, 66, 41, 66, -1, 66, 42, 66, -1,
          66, 43, 66, -1, 66, 48, 66, -1, 66, 44,
          66, -1, 66, 59, 66, -1, 66, 36, 66, -1,
          66, 34, 66, -1, 66, 24, 66, -1, 66, 25,
          66, -1, 66, 26, 66, -1, 66, 27, 66, -1,
          66, 23, 66, -1, 66, 22, 66, -1, 66, 28,
          66, -1, 66, 29, 66, -1, 66, 30, 66, -1,
          66, 31, 66, -1, 66, 10, 66, -1, 66, 12,
          66, -1, 9, 51, 73, 57, 74, 64, -1, 66,
          51, 71, 57, -1, 16, 68, 64, -1, 16, 68,
          64, 17, 64, -1, 14, 69, 64, -1, 18, 67,
          64, -1, 21, 64, -1, 66, 13, 71, 60, 60,
          -1, 66, 52, 71, 60, -1, 8, 32, 8, -1,
          8, 32, 5, -1, 5, 32, 8, -1, 5, 32,
          5, -1, 8, 33, 8, -1, 8, 33, 5, -1,
          5, 33, 8, -1, 5, 33, 5, -1, 66, 49,
          8, -1, 66, 49, 5, -1, 66, 50, 8, -1,
          66, 50, 5, -1, 19, -1, 20, -1, 51, 66,
          57, -1, 51, 66, 57, -1, 51, 8, 15, 66,
          57, -1, -1, 64, -1, 70, 54, 64, -1, 70,
          54, -1, 70, 53, 64, -1, 70, 53, -1, 72,
          -1, 71, 74, 61, 72, -1, -1, 66, -1, 8,
          11, -1, 8, 11, 66, -1, 5, 11, -1, 5,
          11, 66, -1, 7, 11, -1, 7, 11, 66, -1,
          -1, 8, -1, 8, 11, 66, -1, 73, 61, 8,
          -1, 73, 61, 8, 11, 66, -1, -1
      };

  /* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
     YYRHS.  */
  private static final short yyprhs_[] =
      {
          0, 0, 3, 5, 7, 10, 13, 15, 17, 19,
          23, 25, 27, 29, 31, 35, 39, 42, 45, 48,
          51, 54, 58, 62, 66, 70, 74, 78, 82, 86,
          90, 94, 98, 102, 106, 110, 114, 118, 122, 126,
          130, 134, 138, 142, 149, 154, 158, 164, 168, 172,
          175, 181, 186, 190, 194, 198, 202, 206, 210, 214,
          218, 222, 226, 230, 234, 236, 238, 242, 246, 252,
          253, 255, 259, 262, 266, 269, 271, 276, 277, 279,
          282, 286, 289, 293, 296, 300, 301, 303, 307, 311,
          317
      };

  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final short yyrline_[] =
      {
          0, 248, 248, 249, 250, 251, 252, 255, 256, 259,
          262, 263, 264, 265, 267, 268, 270, 271, 272, 273,
          274, 276, 277, 278, 279, 280, 281, 282, 283, 284,
          285, 286, 287, 288, 289, 290, 291, 292, 293, 294,
          295, 297, 298, 299, 301, 302, 303, 304, 305, 306,
          307, 308, 309, 310, 311, 312, 313, 314, 315, 316,
          317, 318, 319, 320, 321, 322, 326, 329, 332, 336,
          337, 338, 339, 340, 341, 344, 345, 348, 349, 350,
          351, 352, 353, 354, 355, 358, 359, 360, 361, 362,
          365
      };

  // Report on the debug stream that the rule yyrule is going to be reduced.

  private void yy_reduce_print(int yyrule, YYStack yystack) {
    if (yydebug == 0) {
      return;
    }

    int yylno = yyrline_[yyrule];
    int yynrhs = yyr2_[yyrule];
    /* Print the symbols being reduced, and their result.  */
    yycdebug("Reducing stack by rule " + (yyrule - 1)
        + " (line " + yylno + "), ");

    /* The symbols being reduced.  */
    for (int yyi = 0; yyi < yynrhs; yyi++) {
      yy_symbol_print("   $" + (yyi + 1) + " =",
          yyrhs_[yyprhs_[yyrule] + yyi],
          ((yystack.valueAt(yynrhs - (yyi + 1)))),
          yystack.locationAt(yynrhs - (yyi + 1)));
    }
  }

  /* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
  private static final byte yytranslate_table_[] =
      {
          0, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          53, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 58, 2, 2, 49, 59, 2, 2,
          51, 57, 42, 40, 61, 41, 2, 43, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 45, 54,
          2, 2, 2, 34, 50, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 52, 2, 60, 48, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 55, 2, 56, 36, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
          2, 2, 2, 2, 2, 2, 1, 2, 3, 4,
          5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
          15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
          25, 26, 27, 28, 29, 30, 31, 32, 33, 35,
          37, 38, 39, 44, 46, 47
      };

  private static final byte yytranslate_(int t) {
    if (t >= 0 && t <= yyuser_token_number_max_) {
      return yytranslate_table_[t];
    } else {
      return yyundef_token_;
    }
  }

  private static final int yylast_ = 709;
  private static final int yynnts_ = 13;
  private static final int yyempty_ = -2;
  private static final int yyfinal_ = 46;
  private static final int yyterror_ = 1;
  private static final int yyerrcode_ = 256;
  private static final int yyntokens_ = 62;

  private static final int yyuser_token_number_max_ = 295;
  private static final int yyundef_token_ = 2;

/* User implementation code.  */


/* Line 880 of lalr1.java  */
/* Line 367 of "gram.y"  */


/*----------------------------------------------------------------------------*/

//static int (*ptr_getc)(void);


  public SEXP getResult() {
    return result;
  }

  public StatusResult getResultStatus() {
    return extendedParseResult;
  }

  static SEXP makeSrcref(Location lloc, SEXP srcfile) {

    int values[] = new int[6];
    values[0] = lloc.getBegin().getLine() + 1;
    values[1] = lloc.getBegin().getCharIndex() + 1;
    values[2] = lloc.getEnd().getLine() + 1;
    values[3] = lloc.getEnd().getCharIndex() + 1;
    values[4] = lloc.getBegin().getColumn() + 1;
    values[5] = lloc.getEnd().getColumn() + 1;

    if (srcfile==null) {
        srcfile=Null.INSTANCE;
    }

    PairList attributes = PairList.Node.newBuilder()
        .add(Symbols.SRC_FILE, srcfile)
        .add(Symbols.CLASS, new StringArrayVector("srcref"))
        .build();

    return new IntArrayVector(values, AttributeMap.fromPairList(attributes));
  }

  <T extends AbstractSEXP> T attachSrcrefs(T val, SEXP srcfile) {
    if (state.keepSrcRefs) {
      SEXP t;
      Vector.Builder srval;
      int n;

      t = CDR(srcRefs);
      int tlen = length(t);
      srval = allocVector(VECSXP, tlen);
      for (n = 0 ; n < tlen; n++, t = CDR(t)) {
        srval.set(n, CAR(t));
      }

      val.unsafeSetAttributes(
          AttributeMap.newBuilder().
              set(R_SrcrefSymbol, srval.build()).
              set(R_SrcfileSymbol, srcfile).
              build()
      );
      srcRefs = NewList();
    }
    return val;
  }

  private int xxvalue(SEXP v, StatusResult result, Location lloc) {
    if (result != StatusResult.EMPTY && result != StatusResult.OK) {
      if (state.keepSrcRefs) {
        if (v == Null.INSTANCE) {
          StringArrayVector.Builder sexp = new StringArrayVector.Builder();
          v = sexp.build();
        }
        if (lloc == null) {
          lloc = new Location(yylexer.getStartPos(), yylexer.getEndPos());
        }
        SEXP srcRef = makeSrcref(lloc, state.srcFile);
        REPROTECT(srcRefs = GrowList(srcRefs, srcRef), srindex);
      }
    }
    this.result = v;
    this.extendedParseResult = result;
    return YYACCEPT;
  }

  private SEXP xxnullformal() {
    SEXP ans;
    PROTECT(ans = R_NilValue);
    return ans;
  }

  private SEXP xxfirstformal0(SEXP sym) {
    SEXP ans;
    UNPROTECT_PTR(sym);
    if (options.isGenerateCode()) {
      PROTECT(ans = FirstArg(R_MissingArg, sym));
    } else {
      PROTECT(ans = R_NilValue);
    }
    return ans;
  }

  private SEXP xxfirstformal1(SEXP sym, SEXP expr) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = FirstArg(expr, sym));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    UNPROTECT_PTR(sym);
    return ans;
  }

  private SEXP xxaddformal0(SEXP formlist, SEXP sym, Location lloc) {
    SEXP ans;
    if (options.isGenerateCode()) {
      CheckFormalArgs(formlist, sym, lloc);
      PROTECT(ans = NextArg(formlist, R_MissingArg, sym));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(sym);
    UNPROTECT_PTR(formlist);
    return ans;
  }

  private SEXP xxaddformal1(SEXP formlist, SEXP sym, SEXP expr, Location lloc) {
    SEXP ans;
    if (options.isGenerateCode()) {
      CheckFormalArgs(formlist, sym, lloc);
      PROTECT(ans = NextArg(formlist, expr, sym));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    UNPROTECT_PTR(sym);
    UNPROTECT_PTR(formlist);
    return ans;
  }

  private SEXP xxexprlist0() {
    SEXP ans;
    if (options.isGenerateCode()) {
      if (state.keepSrcRefs) {
        //setAttrib(ans, R_SrcrefSymbol, srcRefs);
        PROTECT(ans = NewList(
                          AttributeMap.newBuilder().set(R_SrcrefSymbol,srcRefs).build()
               )      );
        REPROTECT(srcRefs = NewList(), srindex);
      } else {
        PROTECT(ans = NewList());
      }
    } else {
      PROTECT(ans = R_NilValue);
    }
    return ans;
  }

  private SEXP xxexprlist1(SEXP expr, Location lloc) {
    SEXP ans, tmp;
    if (options.isGenerateCode()) {
      AttributeMap attrs = AttributeMap.EMPTY;
      if (state.keepSrcRefs) {
         attrs = AttributeMap.newBuilder().set(R_SrcrefSymbol, srcRefs).build();
         REPROTECT(srcRefs = NewList(), srindex);
         REPROTECT(srcRefs = GrowList(srcRefs, makeSrcref(lloc, state.srcFile)), srindex);
      } 
      PROTECT(tmp = NewList(attrs));
      PROTECT(ans = GrowList(tmp, expr));
      UNPROTECT_PTR(tmp);
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    return ans;
  }

  private SEXP xxexprlist2(SEXP exprlist, SEXP expr, Location lloc) {
    SEXP ans;
    if (options.isGenerateCode()) {
      if (state.keepSrcRefs) {
        REPROTECT(srcRefs = GrowList(srcRefs, makeSrcref(lloc, state.srcFile)), srindex);
      }
      PROTECT(ans = GrowList(exprlist, expr));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    UNPROTECT_PTR(exprlist);
    return ans;
  }

  private SEXP xxsub0() {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang2(R_MissingArg, R_NilValue));
    } else {
      PROTECT(ans = R_NilValue);
    }
    return ans;
  }

  private SEXP xxsub1(SEXP expr, Location lloc) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = TagArg(expr, R_NilValue, lloc));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    return ans;
  }

  private SEXP xxsymsub0(SEXP sym, Location lloc) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = TagArg(R_MissingArg, sym, lloc));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(sym);
    return ans;
  }

  private SEXP xxsymsub1(SEXP sym, SEXP expr, Location lloc) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = TagArg(expr, sym, lloc));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    UNPROTECT_PTR(sym);
    return ans;
  }

  private SEXP xxnullsub0(Location lloc) {
    SEXP ans;
    UNPROTECT_PTR(R_NilValue);
    if (options.isGenerateCode()) {
      PROTECT(ans = TagArg(R_MissingArg, Symbol.get("NULL"), lloc));
    } else {
      PROTECT(ans = R_NilValue);
    }
    return ans;
  }

  private SEXP xxnullsub1(SEXP expr, Location lloc) {
    SEXP ans = Symbol.get("NULL");
    UNPROTECT_PTR(R_NilValue);
    if (options.isGenerateCode()) {
      PROTECT(ans = TagArg(expr, ans, lloc));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    return ans;
  }


  private SEXP xxsublist1(SEXP sub) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = FirstArg(CAR(sub), CADR(sub)));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(sub);
    return ans;
  }

  private SEXP xxsublist2(SEXP sublist, SEXP sub) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = NextArg(sublist, CAR(sub), CADR(sub)));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(sub);
    UNPROTECT_PTR(sublist);
    return ans;
  }

  private SEXP xxcond(SEXP expr) {
    state.setEatLines(true);
    return expr;
  }

  private SEXP xxifcond(SEXP expr) {
    state.setEatLines(true);
    return expr;
  }

  private SEXP xxif(SEXP ifsym, SEXP cond, SEXP expr) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang3(ifsym, cond, expr));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    UNPROTECT_PTR(cond);
    return ans;
  }

  private SEXP xxifelse(SEXP ifsym, SEXP cond, SEXP ifexpr, SEXP elseexpr) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang4(ifsym, cond, ifexpr, elseexpr));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(elseexpr);
    UNPROTECT_PTR(ifexpr);
    UNPROTECT_PTR(cond);
    return ans;
  }

  private SEXP xxforcond(SEXP sym, SEXP expr) {
    SEXP ans;
    state.setEatLines(true);
    if (options.isGenerateCode()) {
      PROTECT(ans = lang2(sym, expr));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(expr);
    UNPROTECT_PTR(sym);
    return ans;
  }

  private SEXP xxfor(SEXP forsym, SEXP forcond, SEXP body) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang4(forsym, CAR(forcond), CAR(CDR(forcond)), body));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(body);
    UNPROTECT_PTR(forcond);
    return ans;
  }

  private SEXP xxwhile(SEXP whilesym, SEXP cond, SEXP body) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang3(whilesym, cond, body));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(body);
    UNPROTECT_PTR(cond);
    return ans;
  }

  private SEXP xxrepeat(SEXP repeatsym, SEXP body) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang2(repeatsym, body));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(body);
    return ans;
  }

  private SEXP xxnxtbrk(SEXP keyword) {
    if (options.isGenerateCode()) {
      PROTECT(keyword = lang1(keyword));
    } else {
      PROTECT(keyword = R_NilValue);
    }
    return keyword;
  }

  private SEXP xxfuncall(SEXP expr, SEXP args) {
    SEXP ans, sav_expr = expr;
    if (options.isGenerateCode()) {
      if (isString(expr)) {
        expr = Symbol.get(CHAR(STRING_ELT(expr, 0)));
      }
      PROTECT(expr);
      if (length(CDR(args)) == 1 && CADR(args) == R_MissingArg && TAG(CDR(args)) == R_NilValue) {
        ans = lang1(expr);
      } else {
        ans = LCONS(expr, CDR(args));
      }
      UNPROTECT(1);
      PROTECT(ans);
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(args);
    UNPROTECT_PTR(sav_expr);
    return ans;
  }


  private SEXP xxdefun(SEXP fname, SEXP formals, SEXP body, Location loc) {

    SEXP ans;
    SEXP source;

    if (options.isGenerateCode()) {
      if (!state.keepSrcRefs) {
         PROTECT(source = R_NilValue);
      } else {
         source = makeSrcref(loc,state.srcFile);
      }
      if(formals == Null.INSTANCE) {
        ans = lang4(fname, Null.INSTANCE, body, source);
      } else {
         PROTECT(ans = lang4(fname, CDR(formals), body, source));
      }
      UNPROTECT_PTR(source);
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(body);
    UNPROTECT_PTR(formals);
    state.getFunctionSource().ascend();
    return ans;
  }

  private SEXP xxunary(SEXP op, SEXP arg) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang2(op, arg));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(arg);
    return ans;
  }

  private SEXP xxbinary(SEXP n1, SEXP n2, SEXP n3) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang3(n1, n2, n3));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(n2);
    UNPROTECT_PTR(n3);
    return ans;
  }

  private SEXP xxparen(SEXP n1, SEXP n2) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = lang2(n1, n2));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(n2);
    return ans;
  }


/* This should probably use CONS rather than LCONS, but
   it shouldn't matter and we would rather not meddle
   See PR#7055 */

  private SEXP xxsubscript(SEXP a1, SEXP a2, SEXP a3) {
    SEXP ans;
    if (options.isGenerateCode()) {
      PROTECT(ans = LCONS(a2, CONS(a1, CDR(a3))));
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(a3);
    UNPROTECT_PTR(a1);
    return ans;
  }

  private SEXP xxexprlist(SEXP a1, Location lloc, SEXP a2) {
    SEXP ans;
    SEXP prevSrcrefs;

    state.setEatLines(false);
    if (options.isGenerateCode()) {
      SEXP prevA2 = a2;
      a2 = FunctionCall.fromListExp((PairList.Node) a2);
      SETCAR(a2, a1);
      if (state.keepSrcRefs) {
        PROTECT(prevSrcrefs = getAttrib(prevA2, R_SrcrefSymbol));
        REPROTECT(srcRefs = Insert(srcRefs, makeSrcref(lloc, state.srcFile)), srindex);
        PROTECT(ans = attachSrcrefs((PairList.Node)a2, state.srcFile));
        if (isNull(prevSrcrefs)) {
           prevSrcrefs = NewList();
        }
        REPROTECT(srcRefs = prevSrcrefs, srindex);
        /* SrcRefs got NAMED by being an attribute... */
        //  this is related to memory managing in R, in java gc is work of jvm, so ignore
        //  named flags.
        //SET_NAMED(srcRefs, 0);
        UNPROTECT_PTR(prevSrcrefs);
      }
      else {
        PROTECT(ans = a2);
      }
    } else {
      PROTECT(ans = R_NilValue);
    }
    UNPROTECT_PTR(a2);
    return ans;
  }

/*--------------------------------------------------------------------------*/

  private SEXP TagArg(SEXP arg, SEXP tag, Location lloc) {

    if(tag instanceof StringVector) {
      tag = Symbol.get(translateChar(STRING_ELT(tag, 0)));
    }

    if(tag instanceof Symbol || tag instanceof Null) {
        return lang2(arg, tag);
    } else {
        error("incorrect tag type at line %d", lloc.getBegin().getLine());
        return R_NilValue/* -Wall */;
    }
  }


/* Stretchy List Structures : Lists are created and grown using a special */
/* dotted pair.  The CAR of the list points to the last cons-cell in the */
/* list and the CDR points to the first.  The list can be extracted from */
/* the pair by taking its CDR, while the CAR gives fast access to the end */
/* of the list. */


/* Create a stretchy-list dotted pair */

  static SEXP NewList() {
    return NewList(AttributeMap.EMPTY);
  }

  static SEXP NewList(AttributeMap attributes) {
    SEXP s = CONS(R_NilValue, R_NilValue, attributes);
    SETCAR(s, s);
    return s;
  }

/* Add a new element at the end of a stretchy list */

  static SEXP GrowList(SEXP l, SEXP s) {
    SEXP tmp;
    PROTECT(s);
    tmp = CONS(s, R_NilValue);
    UNPROTECT(1);
    SETCDR(CAR(l), tmp);
    SETCAR(l, tmp);
    return l;
  }

/* Insert a new element at the head of a stretchy list */

  static SEXP Insert(SEXP l, SEXP s) {
    SEXP tmp;
    PROTECT(s);
    tmp = CONS(s, CDR(l));
    UNPROTECT(1);
    SETCDR(l, tmp);
    return l;
  }

  static SEXP FirstArg(SEXP s, SEXP tag) {
    SEXP tmp;
    PROTECT(s);
    PROTECT(tag);
    PROTECT(tmp = NewList());
    tmp = GrowList(tmp, s);
    SET_TAG(CAR(tmp), tag);
    UNPROTECT(3);
    return tmp;
  }

  static SEXP NextArg(SEXP l, SEXP s, SEXP tag) {
    PROTECT(tag);
    PROTECT(l);
    l = GrowList(l, s);
    SET_TAG(CAR(l), tag);
    UNPROTECT(2);
    return l;
  }

  private void CheckFormalArgs(SEXP formlist, SEXP _new, Location lloc) {
    while (formlist != R_NilValue) {
      if (TAG(formlist) == _new) {
        error("Repeated formal argument '%s' on line %d", CHAR(PRINTNAME(_new)),
            lloc.getBegin().getLine());
      }
      formlist = CDR(formlist);
    }
  }


}


