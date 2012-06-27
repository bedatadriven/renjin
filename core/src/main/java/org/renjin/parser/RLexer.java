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
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

import static java.lang.Character.isDigit;
import static org.renjin.parser.RParser.*;
import static org.renjin.parser.Tokens.*;
import static org.renjin.util.CDefines.R_NilValue;


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

  private SrcRefState srcRef = new SrcRefState();

  private Position tokenBegin = new Position();
  private Position tokenEnd = new Position();

/* Private pushback, since file ungetc only guarantees one byte.
   We need up to one MBCS-worth */

  private static final int PUSHBACK_BUFSIZE = 16;
  private int pushback[] = new int[PUSHBACK_BUFSIZE];
  private int npush = 0;

  private int prevpos = 0;
  private int prevlines[] = new int[PUSHBACK_BUFSIZE];
  private int prevcols[] = new int[PUSHBACK_BUFSIZE];
  private int prevbytes[] = new int[PUSHBACK_BUFSIZE];


  private Reader reader;
  private int xxcharcount;
  private int xxcharsave;


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


  public RLexer(ParseOptions options, ParseState state, Reader reader) {
    this.reader = reader;
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


  /* Split the input stream into tokens. */
/* This is the lowest of the parsing levels. */

  private int consumeNextToken() {
    int c;

    if (savedToken != 0) {
      c = savedToken;
      yylval = savedLVal;
      savedLVal = R_NilValue;
      savedToken = 0;
      tokenBegin = savedTokenPos;
      return c;
    }
    xxcharsave = xxcharcount; /* want to be able to go back one token */

    c = skipSpace();
    if (c == '\r') {
      c = xxgetc();
      if(c != '\n') {
        xxungetc(c);
        c = '\r';
      }
    }
    if (c == '#') c = skipComment();

    tokenBegin.line = srcRef.xxlineno;
    tokenBegin.column = srcRef.xxcolno;
    tokenBegin.byteIndex = srcRef.xxbyteno;

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
        if (nextchar('=')) {
          yylval = install("<=");
          return LE;
        }
        if (nextchar('-')) {
          yylval = install("<-");
          return LEFT_ASSIGN;
        }
        if (nextchar('<')) {
          if (nextchar('-')) {
            yylval = install("<<-");
            return LEFT_ASSIGN;
          } else
            return ERROR;
        }
        yylval = install("<");
        return LT;
      case '-':
        if (nextchar('>')) {
          if (nextchar('>')) {
            yylval = install("<<-");
            return RIGHT_ASSIGN;
          } else {
            yylval = install("<-");
            return RIGHT_ASSIGN;
          }
        }
        yylval = install("-");
        return '-';
      case '>':
        if (nextchar('=')) {
          yylval = install(">=");
          return GE;
        }
        yylval = install(">");
        return GT;
      case '!':
        if (nextchar('=')) {
          yylval = install("!=");
          return NE;
        }
        yylval = install("!");
        return '!';
      case '=':
        if (nextchar('=')) {
          yylval = install("==");
          return EQ;
        }
        yylval = install("=");
        return EQ_ASSIGN;
      case ':':
        if (nextchar(':')) {
          if (nextchar(':')) {
            yylval = install(":::");
            return NS_GET_INT;
          } else {
            yylval = install("::");
            return NS_GET;
          }
        }
        if (nextchar('=')) {
          yylval = install(":=");
          return LEFT_ASSIGN;
        }
        yylval = install(":");
        return ':';
      case '&':
        if (nextchar('&')) {
          yylval = install("&&");
          return AND2;
        }
        yylval = install("&");
        return AND;
      case '|':
        if (nextchar('|')) {
          yylval = install("||");
          return OR2;
        }
        yylval = install("|");
        return OR;
      case LBRACE:
        yylval = install("{");
        return c;
      case RBRACE:
        return c;
      case '(':
        yylval = install("(");
        return c;
      case ')':
        return c;
      case '[':
        if (nextchar('[')) {
          yylval = install("[[");
          return LBB;
        }
        yylval = install("[");
        return c;
      case ']':
        return c;
      case '?':
        yylval = install("?");
        return c;
      case '*':
        /* Replace ** by ^.  This has been here since 1998, but is
         undocumented (at least in the obvious places).  It is in
         the index of the Blue Book with a reference to p. 431, the
         help for 'Deprecated'.  S-PLUS 6.2 still allowed this, so
         presumably it was for compatibility with S. */
        if (nextchar('*')) {
          c = '^';
        }
        yylval = install(codePointToString(c));
        return c;
      case '+':
      case '/':
      case '^':
      case '~':
      case '$':
      case '@':
        yylval = install(codePointToString(c));
        return c;
      default:
        return c;
    }
  }

  private String codePointToString(int c) {
    // TODO: this can't be the most efficient way to do this
    StringBuilder sb = new StringBuilder(1);
    sb.appendCodePoint(c);
    return sb.toString();
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

  private boolean nextchar(int expect) {
    int c = xxgetc();
    if (c == expect) {
      return true;
    } else {
      xxungetc(c);
      return false;
    }
  }

  public void yyerror(RParser.Location loc, String s) {

  }

  private SEXP install(String symbolName) {
    return Symbol.get(symbolName);
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
    tokenEnd.line = srcRef.xxlineno;
    tokenEnd.column = srcRef.xxcolno;
    tokenEnd.byteIndex = srcRef.xxbyteno;
  }


/* Note that with interactive use, EOF cannot occur inside */
/* a comment.  However, semicolons inside comments make it */
/* appear that this does happen.  For this reason we use the */
/* special assignment EndOfFile=2 to indicate that this is */
/* going on.  This is detected and dealt with in Parse1Buffer. */

  private int skipComment() {
    int c = '#', i;
    boolean maybeLine = (srcRef.xxcolno == 1);
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
    if (c == R_EOF) {
      parseState.setEndOfFile(2);
    }
    return c;
  }


  private int xxgetc() {
    int c;

    if (npush != 0) {
      c = pushback[--npush];
    } else {
      try {
        c = reader.read();
      } catch (IOException e) {
        throw new RLexException("IOException while reading", e);
      }
    }

    prevpos = (prevpos + 1) % PUSHBACK_BUFSIZE;
    prevcols[prevpos] = srcRef.xxcolno;
    prevbytes[prevpos] = srcRef.xxbyteno;
    prevlines[prevpos] = srcRef.xxlineno;

    if (c == -1) {
      parseState.setEndOfFile(1);
      return R_EOF;
    }
    // R_ParseContextLast = (R_ParseContextLast + 1) % PARSE_CONTEXT_SIZE;
    // R_ParseContext[R_ParseContextLast] = c;

    if (c == '\n') {
      srcRef.xxlineno += 1;
      srcRef.xxcolno = 0;
      srcRef.xxbyteno = 0;
    } else {
      srcRef.xxcolno++;
      srcRef.xxbyteno++;
    }
    /* only advance column for 1st byte in UTF-8 */
    // if (0x80 <= (char)c && (char)c <= 0xBF && known_to_be_utf8)
    //   ParseState.xxcolno--;

    if (c == '\t') srcRef.xxcolno = ((srcRef.xxcolno + 7) & ~7);

    R_ParseContextLine = srcRef.xxlineno;

    if (parseOptions.isKeepSource() && parseOptions.isGenerateCode()) {
      parseState.getFunctionSource().maybeAppendSourceCodePoint(c);
    }

    xxcharcount++;
    return c;
  }

  private int processLineDirective() {
    int c, tok, linenumber;
    c = skipSpace();
    if (!isDigit(c)) return (c);
    tok = consumeNumericValue(c);
    // linenumber = Integer.parseInt(yytext);   // TODO: who is filling yytext?
    c = skipSpace();
    if (c == '"')
      tok = consumeStringValue(c, false);
    if (tok == STR_CONST)
      //  setParseFilename(yylval);
      do {
        c = xxgetc();
      } while (c != '\n' && c != R_EOF);
    //ParseState.xxlineno = linenumber;
    //R_ParseContext[R_ParseContextLast] = '\0';  /* Context report shouldn't show the directive */
    return (c);
  }

  private int xxungetc(int c) {
    /* this assumes that c was the result of xxgetc; if not, some edits will be needed */
    srcRef.xxlineno = prevlines[prevpos];
    srcRef.xxbyteno = prevbytes[prevpos];
    srcRef.xxcolno = prevcols[prevpos];
    prevpos = (prevpos + PUSHBACK_BUFSIZE - 1) % PUSHBACK_BUFSIZE;

    R_ParseContextLine = srcRef.xxlineno;
    // if ( KeepSource && GenerateCode && FunctionLevel > 0 )
    // SourcePtr--;
    xxcharcount--;
    //R_ParseContext[R_ParseContextLast] = '\0';
    /* precaution as to how % is implemented for < 0 numbers */
    //  R_ParseContextLast = (R_ParseContextLast + PARSE_CONTEXT_SIZE -1) % PARSE_CONTEXT_SIZE;
    if (npush >= PUSHBACK_BUFSIZE) return EOF;
    pushback[npush++] = c;
    return c;
  }

  private void setParseFilename(SEXP newname) {
// TODO
//    if (isEnvironment(SrcRefState.SrcFile)) {
//    	SEXP oldname = findVar(install("filename"), SrcRefState.SrcFile);
//    	if (isString(oldname) && length(oldname) > 0 &&
//    	    strcmp(CHAR(STRING_ELT(oldname, 0)),
//    	           CHAR(STRING_ELT(newname, 0))) == 0) return;
//    }
//    REPROTECT(SrcRefState.SrcFile = NewEnvironment(R_NilValue, R_NilValue, R_EmptyEnv), SrcRefState.SrcFileProt);
//
//    defineVar(install("filename"), newname, SrcRefState.SrcFile);
//    setAttrib(SrcRefState.SrcFile, R_ClassSymbol, mkString("srcfile"));
//    UNPROTECT_PTR(newname);
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
      double a = ParseUtil.parseDouble(buffer.toString());
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
      yylval = parseOptions.isGenerateCode() ? mkComplex(buffer.toString()) : R_NilValue;
    } else if (c == 'L' && asNumeric == 0) {
      if (parseOptions.isGenerateCode() && seendot == 1 && !seenexp) {
        logger.warning(String.format("integer literal %sL contains unnecessary decimal point", buffer.toString()));
      }
      yylval = parseOptions.isGenerateCode() ? 
          new IntArrayVector(ParseUtil.parseInt(buffer.toString())) : R_NilValue;
    } else {
      if (c != 'L') {
        xxungetc(c);
      }
      yylval = parseOptions.isGenerateCode() ?
          new DoubleArrayVector(ParseUtil.parseDouble(buffer.toString())) : R_NilValue;
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
    double f = ParseUtil.parseDouble(s);

    if(parseOptions.isGenerateCode()) {
      t = new ComplexVector(new Complex(0, f));
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
            throw new RLexException(String.format("\\uxxxx sequences not supported inside backticks (line %d)", srcRef.xxlineno));
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
                  srcRef.xxlineno));
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
            throw new RLexException(String.format("\\Uxxxxxxxx sequences not supported inside backticks (line %d)", srcRef.xxlineno));
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
              logger.severe(String.format("invalid \\U{xxxxxxxx} sequence (line %d)", srcRef.xxlineno));
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
      yylval = install(stext.toString());
      return SYMBOL;
    } else {
      yylval = new StringVector(stext.toString());
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
    yylval = install(buffer.toString());
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
    yylval = install(buffer.toString());
    return SYMBOL;
  }


/* KeywordLookup has side effects, it sets yylval */

  private int lookupKeyword(String s) {
    int i;
    for (i = 0; i != keywords.length; i++) {
      if (keywords[i].name.equals(s)) {
        switch (keywords[i].token) {
          case NULL_CONST:
            yylval = R_NilValue;
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
                  yylval = new StringVector(StringVector.NA);
                  break;
                case 9:
                  yylval = new ComplexVector(new Complex(DoubleVector.NA, DoubleVector.NA));
                  break;
              }
            } else {
              yylval = R_NilValue;
            }
            break;
          case FUNCTION:
          case WHILE:
          case REPEAT:
          case FOR:
          case IF:
          case NEXT:
          case BREAK:
            yylval = install(s);
            break;
          case IN:
          case ELSE:
            break;
          case SYMBOL:
            yylval = install(s);
            break;
        }
        return keywords[i].token;
      }
    }
    return 0;
  }
}
