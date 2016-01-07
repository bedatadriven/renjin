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

package org.renjin.parser;

import org.apache.commons.math.complex.Complex;
import org.renjin.parser.RParser.*;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

import static java.lang.Character.isDigit;
import static org.renjin.parser.RParser.*;
import static org.renjin.parser.Tokens.*;
import static org.renjin.util.CDefines.*;

public class RLexer implements RParser.Lexer {


  /**
   * Line in file of the above
   */
  public static int R_ParseContextLine = 0;
  
  private static Logger logger = Logger.getLogger("R.Lexer");

  private int savedToken;
  private SEXP savedLVal;
  private Position savedTokenPos = null;

  private SEXP yylval;

  private final ParseState parseState;

  private final ParseOptions parseOptions;

  private Position tokenBegin = new Position();
  private Position tokenEnd = new Position();

  private RLexerReader reader;

  private final LexerContextStack contextStack = new LexerContextStack();


  private static class Keyword {
    public String name;
    public int token;

    public Keyword(String name, int token) {
      this.name = name;
      this.token = token;
    }
  }

  private Keyword keywords[] = new Keyword[]{
      new Keyword("NULL", NULL_CONST),
      new Keyword("NA", NUM_CONST),
      new Keyword("TRUE", NUM_CONST),
      new Keyword("FALSE", NUM_CONST),
      new Keyword("Inf", NUM_CONST),
      new Keyword("NaN", NUM_CONST),
      new Keyword("NA_integer_", NUM_CONST),
      new Keyword("NA_real_", NUM_CONST),
      new Keyword("NA_character_", NUM_CONST),
      new Keyword("NA_complex_", NUM_CONST),
      new Keyword("function", FUNCTION),
      new Keyword("while", WHILE),
      new Keyword("repeat", REPEAT),
      new Keyword("for", FOR),
      new Keyword("if", IF),
      new Keyword("in", IN),
      new Keyword("else", ELSE),
      new Keyword("next", NEXT),
      new Keyword("break", BREAK),
      new Keyword("...", SYMBOL)};

  private Location errorLocation;

  private String errorMessage;


  public RLexer(ParseOptions options, ParseState state, Reader reader) {
    this.reader = new RLexerReader(reader);
    this.parseOptions = options;
    this.parseState = state;
  }


  public Position getStartPos() {
    return tokenBegin.clone();
  }

  public Position getEndPos() {
    return tokenEnd.clone();
  }

  public SEXP getLVal() {
    return yylval;
  }

  public int yylex() {

    int token;

    do {
      // again:
      token = consumeNextToken();

      /* Newlines must be handled in a context */
      /* sensitive way.  The following block of */
      /* deals directly with newlines in the */
      /* body of "if" statements. */

      if (token == '\n') {

        if (parseState.getEatLines() || contextStack.peek() == '[' ||
            contextStack.peek() == '(') {
          continue;
        }

        /* The essence of this is that in the body of */
        /* an "if", any newline must be checked to */
        /* see if it is followed by an "else". */
        /* such newlines are discarded. */

        if (contextStack.peek() == LexerContextStack.IF_BLOCK) {

          /* Find the next non-newline token */

          while (token == '\n')
            token = consumeNextToken();

          /* If we encounter "}", ")" or "]" then */
          /* we know that all immediately preceding */
          /* "if" bodies have been terminated. */
          /* The corresponding "i" values are */
          /* popped off the context stack. */

          if (token == RBRACE || token == ')' || token == ']') {
            while (contextStack.peek() == LexerContextStack.IF_BLOCK) {
              contextStack.ifPop();
            }
            contextStack.pop();
            setlastloc();
            return token;
          }

          /* When a "," is encountered, it terminates */
          /* just the immediately preceding "if" body */
          /* so we pop just a single "i" of the */
          /* context stack. */

          if (token == ',') {
            contextStack.ifPop();
            setlastloc();
            return token;
          }

          /* Tricky! If we find an "else" we must */
          /* ignore the preceding newline.  Any other */
          /* token means that we must return the newline */
          /* to terminate the "if" and "push back" that */
          /* token so that we will obtain it on the next */
          /* call to token.  In either case sensitivity */
          /* is lost, so we pop the "i" from the context */
          /* stack. */

          if (token == ELSE) {
            parseState.setEatLines(true);
            contextStack.ifPop();
            setlastloc();
            return ELSE;
          } else {
            contextStack.ifPop();
            savedToken = token;
            savedTokenPos = tokenBegin.clone();
            savedLVal = yylval;
            setlastloc();
            return '\n';
          }
        } else {
          setlastloc();
          return '\n';
        }
      }
      break;
    } while (true);

    /* Additional context sensitivities */

    switch (token) {

      /* Any newlines immediately following the */
      /* the following tokens are discarded. The */
      /* expressions are clearly incomplete. */

      case '+':
      case '-':
      case '*':
      case '/':
      case '^':
      case LT:
      case LE:
      case GE:
      case GT:
      case EQ:
      case NE:
      case OR:
      case AND:
      case OR2:
      case AND2:
      case SPECIAL:
      case FUNCTION:
      case WHILE:
      case REPEAT:
      case FOR:
      case IN:
      case '?':
      case '!':
      case '=':
      case ':':
      case '~':
      case '$':
      case '@':
      case LEFT_ASSIGN:
      case RIGHT_ASSIGN:
      case EQ_ASSIGN:
        parseState.setEatLines(true);
        break;

      /* Push any "if" statements found and */
      /* discard any immediately following newlines. */

      case IF:
        contextStack.ifPush();
        parseState.setEatLines(false);
        break;

      /* Terminate any immediately preceding "if" */
      /* statements and discard any immediately */
      /* following newlines. */

      case ELSE:
        contextStack.ifPop();
        parseState.setEatLines(true);
        break;

      /* These tokens terminate any immediately */
      /* preceding "if" statements. */

      case ';':
      case ',':
        contextStack.ifPop();
        break;

      /* Any newlines following these tokens can */
      /* indicate the end of an expression. */

      case SYMBOL:
      case STR_CONST:
      case NUM_CONST:
      case NULL_CONST:
      case NEXT:
      case BREAK:
        parseState.setEatLines(false);
        break;

      /* Handle brackets, braces and parentheses */

      case LBB:
        contextStack.push('[');
        contextStack.push('[');
        break;

      case '[':
        contextStack.push((char) token);
        break;

      case LBRACE:
        contextStack.push(token);
        parseState.setEatLines(true);
        break;

      case '(':
        contextStack.push(token);
        break;

      case ']':
        while (contextStack.peek() == LexerContextStack.IF_BLOCK) {
          contextStack.ifPop();
        }
        contextStack.pop();

        parseState.setEatLines(false);
        break;

      case RBRACE:
        while (contextStack.peek() == LexerContextStack.IF_BLOCK) {
          contextStack.ifPop();
        }
        contextStack.pop();
        break;

      case ')':
        while (contextStack.peek() == LexerContextStack.IF_BLOCK) {
          contextStack.ifPop();
        }
        contextStack.pop();
        parseState.setEatLines(false);
        break;

    }
    setlastloc();
    return token;
  }


  /* Split the input stream into tokens.
   * This is the lowest of the parsing levels. 
   */
  private int consumeNextToken() {
    int c;

    if (savedToken != 0) {
      c = savedToken;
      yylval = savedLVal;
      savedLVal = Null.INSTANCE;
      savedToken = 0;
      tokenBegin = savedTokenPos;
      return c;
    }
    //xxcharsave = xxcharcount; /* want to be able to go back one token */

    c = skipSpace();

    if (c == '#') c = skipComment();

    tokenBegin.line = reader.getLineNumber();
    tokenBegin.column = reader.getColumnNumber();
    tokenBegin.charIndex = reader.getCharacterIndex();

    if (c == R_EOF) return END_OF_INPUT;

    /* Either digits or symbols can start with a "." */
    /* so we need to decide which it is and jump to  */
    /* the correct spot. */

    if (c == '.' && typeofnext() >= 2) {
      return consumeSymbolValue(c);
    }

    /* literal numbers */

    if (c == '.') {
      return consumeNumericValue(c);
    }
    /* We don't care about other than ASCII digits */
    if (isDigit(c)) {
      return consumeNumericValue(c);
    }

    /* literal strings */

    if (c == '\"' || c == '\'') {
      return consumeStringValue(c, false);
    }

    /* special functions */

    if (c == '%')
      return consumeSpecialValue(c);

    /* functions, constants and variables */

    if (c == '`') {
      return consumeStringValue(c, true);
    }

    if (Character.isLetter(c)) {
      return consumeSymbolValue(c);
    }

    /* compound tokens */

    switch (c) {
      case '<':
        if (isNextChar('=')) {
          yylval = Symbol.get("<=");
          return LE;
        }
        if (isNextChar('-')) {
          yylval = Symbol.get("<-");
          return LEFT_ASSIGN;
        }
        if (isNextChar('<')) {
          if (isNextChar('-')) {
            yylval = Symbol.get("<<-");
            return LEFT_ASSIGN;
          } else
            return ERROR;
        }
        yylval = Symbol.get("<");
        return LT;
      case '-':
        if (isNextChar('>')) {
          if (isNextChar('>')) {
            yylval = Symbol.get("<<-");
            return RIGHT_ASSIGN;
          } else {
            yylval = Symbol.get("<-");
            return RIGHT_ASSIGN;
          }
        }
        yylval = Symbol.get("-");
        return '-';
      case '>':
        if (isNextChar('=')) {
          yylval = Symbol.get(">=");
          return GE;
        }
        yylval = Symbol.get(">");
        return GT;
      case '!':
        if (isNextChar('=')) {
          yylval = Symbol.get("!=");
          return NE;
        }
        yylval = Symbol.get("!");
        return '!';
      case '=':
        if (isNextChar('=')) {
          yylval = Symbol.get("==");
          return EQ;
        }
        yylval = Symbol.get("=");
        return EQ_ASSIGN;
      case ':':
        if (isNextChar(':')) {
          if (isNextChar(':')) {
            yylval = Symbol.get(":::");
            return NS_GET_INT;
          } else {
            yylval = Symbol.get("::");
            return NS_GET;
          }
        }
        if (isNextChar('=')) {
          yylval = Symbol.get(":=");
          return LEFT_ASSIGN;
        }
        yylval = Symbol.get(":");
        return ':';
      case '&':
        if (isNextChar('&')) {
          yylval = Symbol.get("&&");
          return AND2;
        }
        yylval = Symbol.get("&");
        return AND;
      case '|':
        if (isNextChar('|')) {
          yylval = Symbol.get("||");
          return OR2;
        }
        yylval = Symbol.get("|");
        return OR;
      case LBRACE:
        yylval = Symbol.get("{");
        return c;
      case RBRACE:
        return c;
      case '(':
        yylval = Symbol.get("(");
        return c;
      case ')':
        return c;
      case '[':
        if (isNextChar('[')) {
          yylval = Symbol.get("[[");
          return LBB;
        }
        yylval = Symbol.get("[");
        return c;
      case ']':
        return c;
      case '?':
        yylval = Symbol.get("?");
        return c;
      case '*':
        /* Replace ** by ^.  This has been here since 1998, but is
         undocumented (at least in the obvious places).  It is in
         the index of the Blue Book with a reference to p. 431, the
         help for 'Deprecated'.  S-PLUS 6.2 still allowed this, so
         presumably it was for compatibility with S. */
        if (isNextChar('*')) {
          c = '^';
        }
        yylval = Symbol.get(codePointToString(c));
        return c;
      case '+':
      case '/':
      case '^':
      case '~':
      case '$':
      case '@':
        yylval = Symbol.get(codePointToString(c));
        return c;
      default:
        return c;
    }
  }

  private String codePointToString(int c) {
    return new String(new int[] { c }, 0, 1);
  }


/* This is only called following ., so we only care if it is
an ANSI digit or not */

  private int typeofnext() {
    int k, c;

    c = xxgetc();
    if (isDigit(c)) {
      k = 1;
    } else {
      k = 2;
    }
    xxungetc(c);
    return k;
  }

  private boolean isNextChar(int expect) {
    int c = xxgetc();
    if (c == expect) {
      return true;
    } else {
      xxungetc(c);
      return false;
    }
  }

  public void yyerror(RParser.Location loc, String s) {
    this.errorLocation = loc;
    this.errorMessage = s;
  }
  
  public boolean errorEncountered() {
    return errorLocation != null;
  }
  
  public Location getErrorLocation() {
    return errorLocation;
  }
  
  public String getErrorMessage() {
    return errorMessage;
  }

  /*
  *  The fact that if statements need to parse differently
 *  depending on whether the statement is being interpreted or
 *  part of the body of a function causes the need for ifpop
 *  and IfPush.  When an if statement is encountered an 'i' is
 *  pushed on a stack (provided there are parentheses active).
 *  At later points this 'i' needs to be popped off of the if
 *  stack.
 */

  void setlastloc() {
    tokenEnd.line = reader.getLineNumber();
    tokenEnd.column = reader.getColumnNumber();
    tokenEnd.charIndex = reader.getCharacterIndex();
  }


/* Note that with interactive use, EOF cannot occur inside */
/* a comment.  However, semicolons inside comments make it */
/* appear that this does happen.  For this reason we use the */
/* special assignment EndOfFile=2 to indicate that this is */
/* going on.  This is detected and dealt with in Parse1Buffer. */

  private int skipComment() {
    int c = '#', i;
    boolean maybeLine = (reader.getColumnNumber() == 1);
    if (maybeLine) {
      String lineDirective = "#line";
      for (i = 1; i < 5; i++) {
        c = xxgetc();
        if (c != (int) (lineDirective.charAt(i))) {
          maybeLine = false;
          break;
        }
      }
      if (maybeLine) {
        c = processLineDirective();
      }
    }
    while (c != '\n' && c != R_EOF) {
      c = xxgetc();
    }
    return c;
  }


  private int xxgetc() {
 
    int c;
    try {
      c = reader.read();
      if(c == '\r') {
        c = reader.read();
        if(c != '\n') {
          reader.unread(c);
          c = '\n';
        }
      }
    } catch (IOException e) {
      throw new RLexException(e);
    }
    
    if (c == -1) {
      return R_EOF;
    }
    // R_ParseContextLast = (R_ParseContextLast + 1) % PARSE_CONTEXT_SIZE;
    // R_ParseContext[R_ParseContextLast] = c;

    R_ParseContextLine = reader.getLineNumber();

    if (parseOptions.isKeepSource() && parseOptions.isGenerateCode()) {
      parseState.getFunctionSource().maybeAppendSourceCodePoint(c);
    }

    return c;
  }

  private int processLineDirective() {
    int c, tok, linenumber;
    c = skipSpace();
    if (!isDigit(c)) return (c);
    tok = consumeNumericValue(c);
    if (parseOptions.isGenerateCode()) {
       // TODO: can we receive incorrect value here ? need to rethink.
       linenumber = (int)(yylval.asReal());
    } else {
       // ignored. 
       linenumber = 0;
    }
    c = skipSpace();
    if (c == '"')
      tok = consumeStringValue(c, false);
    if (tok == STR_CONST)
      setParseFilename(yylval);
      do {
        c = xxgetc();
      } while (c != '\n' && c != R_EOF);
      reader.setLineNumber(linenumber);
      //R_ParseContext[R_ParseContextLast] = '\0';  /* Context report shouldn't show the directive */s
    return (c);
  }

  private int xxungetc(int c) {
    return reader.unread(c);
  }

  private void setParseFilename(SEXP newname) {
    if (isEnvironment(parseState.srcFile)) {
        Environment env = (Environment)parseState.srcFile;
    	SEXP oldname = env.findVariable(Symbol.get("filename"));
    	if (isString(oldname) && oldname.length() > 0 &&
            oldname.asString().equals(newname.asString())) return;
        REPROTECT(parseState.srcFile = new Environment(
                                         AttributeMap.newBuilder().set(R_ClassSymbol,mkString("srcfile")).
                                         build()
                                       ), 
                  parseState.srcFileProt);
        env.setVariable(Symbol.get("filename"), newname);
        env.setVariable(Symbol.get("original"), oldname);
    } else {
        REPROTECT(parseState.srcFile = /*duplicate(*/newname/*)*/, parseState.srcFileProt);
    }
    UNPROTECT_PTR(newname);
  }

  private int consumeNumericValue(int c) {
    StringBuilder buffer = new StringBuilder();
    buffer.appendCodePoint(c);

    int seendot = c == '.' ? 1 : 0;
    boolean seenexp = false;
    int last = c;
    int nd = 0;
    int asNumeric = 0;

    /* We don't care about other than ASCII digits */
    while (isDigit(c = xxgetc()) || c == '.' || c == 'e' || c == 'E'
        || c == 'x' || c == 'X' || c == 'L') {
      if (c == 'L') /* must be at the end.  Won't allow 1Le3 (at present). */
        break;

      if (c == 'x' || c == 'X') {
        if (last != '0') {
          break;
        }
        buffer.appendCodePoint(c);
        while (isDigit(c = xxgetc()) || ('a' <= c && c <= 'f') ||
            ('A' <= c && c <= 'F') || c == '.') {
          buffer.appendCodePoint(c);
          nd++;
        }
        if (nd == 0) {
          return ERROR;
        }
        if (c == 'p' || c == 'P') {
          buffer.appendCodePoint(c);
          c = xxgetc();
          if (!isDigit(c) && c != '+' && c != '-') {
            return ERROR;
          }
          if (c == '+' || c == '-') {
            buffer.appendCodePoint(c);
            c = xxgetc();
          }
          for (nd = 0; isDigit(c); c = xxgetc(), nd++) {
            buffer.appendCodePoint(c);
          }
          if (nd == 0) {
            return ERROR;
          }
        }
        break;
      }
      if (c == 'E' || c == 'e') {
        if (seenexp) {
          break;
        }
        seenexp = true;
        seendot = seendot == 1 ? seendot : 2;
        buffer.appendCodePoint(c);
        c = xxgetc();
        if (!isDigit(c) && c != '+' && c != '-') {
          return ERROR;
        }
        if (c == '+' || c == '-') {
          buffer.appendCodePoint(c);
          c = xxgetc();
          if (!isDigit(c)) {
            return ERROR;
          }
        }
      }
      if (c == '.') {
        if (seendot != 0) {
          break;
        }
        seendot = 1;
      }
      buffer.appendCodePoint(c);
      last = c;
    }

    /* Make certain that things are okay. */
    if (c == 'L') {
      double a = NumericLiterals.parseDouble(buffer.toString());
      int b = (int) a;
      /* We are asked to create an integer via the L, so we check that the
        double and int values are the same. If not, this is a problem and we
        will not lose information and so use the numeric value.
     */
      if (a != (double) b) {
        if (parseOptions.isGenerateCode()) {
          if (seendot == 1 && !seenexp) {
            logger.warning(String.format("integer literal %sL contains decimal; using numeric value", buffer.toString()));
          } else {
            logger.warning(String.format("non-integer value %s qualified with L; using numeric value", buffer));
          }
        }
        asNumeric = 1;
        seenexp = true;
      }
    }

    if (c == 'i') {
      yylval = parseOptions.isGenerateCode() ? mkComplex(buffer.toString()) : Null.INSTANCE;
      
    } else if (c == 'L' && asNumeric == 0) {
      if (parseOptions.isGenerateCode() && seendot == 1 && !seenexp) {
        logger.warning(String.format("integer literal %sL contains unnecessary decimal point", buffer.toString()));
      }
      double a = NumericLiterals.parseDouble(buffer);
      int b = (int) a;
      yylval = parseOptions.isGenerateCode() ? new IntArrayVector(b) : Null.INSTANCE;
      
    } else {
      if (c != 'L') {
        xxungetc(c);
      }
      yylval = parseOptions.isGenerateCode() ?
          new DoubleArrayVector(NumericLiterals.parseDouble(buffer)) : Null.INSTANCE;
    }

    return NUM_CONST;
  }

  private int skipSpace() {
    int c;
    do {
      c = xxgetc();
    } while (c == ' ' || c == '\t' || c == '\f' || c == 0xa0 /* Unicode non-breaking space */);
    return c;
  }

  private SEXP mkComplex(String s) {
    SEXP t = Null.INSTANCE;
    double f = NumericLiterals.parseDouble(s);

    if(parseOptions.isGenerateCode()) {
      t = new ComplexArrayVector(new Complex(0, f));
    }

    return t;
  }


  private static class CTEXT {
    private StringBuffer buffer = new StringBuffer();

    public void push(int c) {
      buffer.appendCodePoint(c);
    }

    public void pop() {
      buffer.setLength(buffer.length() - 1);
    }

    public String toString() {
      return buffer.toString();
      
    }
  }

  /**
   * @param c
   * @param forSymbol true when parsing backticked symbols
   * @return
   */
  private int consumeStringValue(int c, boolean forSymbol) {
    int quote = c;
    int have_warned = 0;
    CTEXT ctext = new CTEXT();
    StringBuffer stext = new StringBuffer();

    while ((c = xxgetc()) != R_EOF && c != quote) {
      ctext.push(c);
      if (c == '\n') {
        xxungetc(c);
        /* Fix by Mark Bravington to allow multiline strings
           * by pretending we've seen a backslash. Was:
           * return ERROR;
           */
        c = '\\';
      }
      if (c == '\\') {
        c = xxgetc();
        ctext.push(c);
        if ('0' <= c && c <= '8') {
          int octal = c - '0';
          if ('0' <= (c = xxgetc()) && c <= '8') {
            ctext.push(c);
            octal = 8 * octal + c - '0';
            if ('0' <= (c = xxgetc()) && c <= '8') {
              ctext.push(c);
              octal = 8 * octal + c - '0';
            } else {
              xxungetc(c);
              ctext.pop();
            }
          } else {
            xxungetc(c);
            ctext.pop();
          }
          c = octal;
        } else if (c == 'x') {
          int val = 0;
          int i;
          int ext;
          for (i = 0; i < 2; i++) {
            c = xxgetc();
            ctext.push(c);
            if (c >= '0' && c <= '9') {
              ext = c - '0';
            } else if (c >= 'A' && c <= 'F') {
              ext = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
              ext = c - 'a' + 10;
            } else {
              xxungetc(c);
              ctext.pop();
              if (i == 0) { /* was just \x */
                if (parseOptions.isGenerateCode() && parseOptions.isWarnEscapes()) {
                  have_warned++;
                  logger.warning("'\\x' used without hex digits");
                }
                val = 'x';
              }
              break;
            }
            val = 16 * val + ext;
          }
          c = val;
        } else if (c == 'u') {
          int val = 0;
          int i;
          int ext;
          boolean delim = false;

          if (forSymbol) {
            throw new RLexException(String.format("\\uxxxx sequences not supported inside backticks (line %d)", reader.getColumnNumber()));
          }
          if ((c = xxgetc()) == '{') {
            delim = true;
            ctext.push(c);
          } else {
            xxungetc(c);
          }
          for (i = 0; i < 4; i++) {
            c = xxgetc();
            ctext.push(c);
            if (c >= '0' && c <= '9') {
              ext = c - '0';
            } else if ((c >= 'A') && (c <= 'F')) {
              ext = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
              ext = c - 'a' + 10;
            } else {
              xxungetc(c);
              ctext.pop();
              if (i == 0) { /* was just \x */
                if (parseOptions.isGenerateCode() && parseOptions.isWarnEscapes()) {
                  have_warned++;
                  logger.warning("\\u used without hex digits");
                }
                val = 'u';
              }
              break;
            }
            val = 16 * val + ext;
          }
          if (delim) {
            if ((c = xxgetc()) != '}') {
              throw new RLexException(String.format("invalid \\u{xxxx} sequence (line %d)",
                  reader.getLineNumber()));
            } else {
              ctext.push(c);
            }
          }
          stext.appendCodePoint(val);
          //WTEXT_PUSH(val); /* this assumes wchar_t is Unicode */
          //use_wcs = TRUE;
          continue;
        } else if (c == 'U') {
          int val = 0;
          int i;
          int ext;
          boolean delim = false;
          if (forSymbol) {
            throw new RLexException(String.format("\\Uxxxxxxxx sequences not supported inside backticks (line %d)", reader.getLineNumber()));
          }
          if ((c = xxgetc()) == '{') {
            delim = true;
            ctext.push(c);
          } else {
            xxungetc(c);
          }
          for (i = 0; i < 8; i++) {
            c = xxgetc();
            ctext.push(c);
            if (c >= '0' && c <= '9') {
              ext = c - '0';
            } else if (c >= 'A' && c <= 'F') {
              ext = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
              ext = c - 'a' + 10;
            } else {
              xxungetc(c);
              ctext.pop();
              if (i == 0) { /* was just \x */
                if (parseOptions.isGenerateCode() && parseOptions.isWarnEscapes()) {
                  have_warned++;
                  logger.warning("\\U used without hex digits");
                }
                val = 'U';
              }
              break;
            }
            val = 16 * val + ext;
          }
          if (delim) {
            if ((c = xxgetc()) != '}') {
              logger.severe(String.format("invalid \\U{xxxxxxxx} sequence (line %d)", reader.getLineNumber()));
            } else {
              ctext.push(c);
            }
          }
          ctext.push(val);
          continue;
        } else {
          switch (c) {
            case 'a':
              c = 7;
              break;
            case 'b':
              c = '\b';
              break;
            case 'f':
              c = '\f';
              break;
            case 'n':
              c = '\n';
              break;
            case 'r':
              c = '\r';
              break;
            case 't':
              c = '\t';
              break;
            case 'v':
              c = 11;
              break;
            case '\\':
              c = '\\';
              break;
            case '"':
            case '\'':
            case ' ':
            case '\n':
              break;
            default:
              if (parseOptions.isGenerateCode() && parseOptions.isWarnEscapes()) {
                have_warned++;
                logger.warning(String.format("'\\%c' is an unrecognized escape in a character string", (char) c));
              }
              break;
          }
        }
      }
      stext.appendCodePoint(c);
    }

    if (forSymbol) {
      yylval = Symbol.get(stext.toString());
      return SYMBOL;
    } else {
      yylval = StringVector.valueOf(stext.toString());
    }
    if (have_warned != 0) {
      logger.warning(String.format("unrecognized escape(s) removed from \"%s\"", ctext));
    }
    return STR_CONST;
  }

  private int consumeSpecialValue(int c) {
    StringBuffer buffer = new StringBuffer();
    buffer.appendCodePoint(c);
    while ((c = xxgetc()) != R_EOF && c != '%') {
      if (c == '\n') {
        xxungetc(c);
        return ERROR;
      }
      //buffer.append(c);
      /*
       * buffer.append(c) causes renjin to interprete the matrix production operator %*%
       * as %42%. Chaning it to buffer.appendCodePoint(c) seems to fix the error.
       */
      buffer.appendCodePoint(c);
    }
    if (c == '%') {
      buffer.appendCodePoint(c);
    }
    yylval = Symbol.get(buffer.toString());
    return SPECIAL;
  }


  private int consumeSymbolValue(int c) {

    StringBuffer buffer = new StringBuffer();

    do {
      buffer.appendCodePoint(c);
    } while ((c = xxgetc()) != R_EOF &&
        (Character.isLetterOrDigit(c) || c == '.' || c == '_'));

    xxungetc(c);

    int keyword;
    if ((keyword = lookupKeyword(buffer.toString())) != 0) {
      if (keyword == FUNCTION) {
        parseState.getFunctionSource().descend();
      }
      return keyword;
    }
    yylval = Symbol.get(buffer.toString());
    return SYMBOL;
  }


/* KeywordLookup has side effects, it sets yylval */

  private int lookupKeyword(String s) {
    int i;
    for (i = 0; i != keywords.length; i++) {
      if (keywords[i].name.equals(s)) {
        switch (keywords[i].token) {
          case NULL_CONST:
            yylval = Null.INSTANCE;
            break;
          case NUM_CONST:
            if (parseOptions.isGenerateCode()) {
              switch (i) {
                case 1:
                  yylval = new LogicalArrayVector(Logical.NA);
                  break;
                case 2:
                  yylval = new LogicalArrayVector(true);
                  break;
                case 3:
                  yylval = new LogicalArrayVector(false);
                  break;
                case 4:
                  yylval = new DoubleArrayVector(Double.POSITIVE_INFINITY);
                  break;
                case 5:
                  yylval = new DoubleArrayVector(Double.NaN);
                  break;
                case 6:
                  yylval = new IntArrayVector(IntVector.NA);
                  break;
                case 7:
                  yylval = new DoubleArrayVector(DoubleVector.NA);
                  break;
                case 8:
                  yylval = StringVector.valueOf(StringVector.NA);
                  break;
                case 9:
                  yylval = new ComplexArrayVector(new Complex(DoubleVector.NA, DoubleVector.NA));
                  break;
              }
            } else {
              yylval = Null.INSTANCE;
            }
            break;
          case FUNCTION:
          case WHILE:
          case REPEAT:
          case FOR:
          case IF:
          case NEXT:
          case BREAK:
            yylval = Symbol.get(s);
            break;
          case IN:
          case ELSE:
            break;
          case SYMBOL:
            yylval = Symbol.get(s);
            break;
        }
        return keywords[i].token;
      }
    }
    return 0;
  }


  public boolean isEof() {
    int c = xxgetc();
    xxungetc(c);
    return c == R_EOF;
  }
  
  /**
   * 
   * @return the current character pos within the stream
   */
  public int getCharacterPos() {
    return reader.getCharacterIndex();
  }
}
